/*
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.v3;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.reader.DexFileReader;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Main {

    public static void doData(byte[] data, File destJar) throws IOException {
        doData(data, destJar, true);
    }

    public static void doData(byte[] data, File destJar, boolean handleException) throws IOException {

        DexFileReader reader = new DexFileReader(data);
        DexExceptionHandlerImpl handler = handleException ? new DexExceptionHandlerImpl() : null;

        Dex2jar.from(reader).withExceptionHandler(handler).to(destJar);

        if (handleException) {
            Map<Method, Exception> exceptions = handler.getExceptions();
            if (exceptions != null && exceptions.size() > 0) {
                File errorFile = new File(destJar.getParentFile(), FilenameUtils.getBaseName(destJar.getName())
                        + ".error.zip");
                handler.dumpException(reader, errorFile);
                System.err.println("Detail Error Information in File " + errorFile);
                System.err
                        .println("Please report this file to http://code.google.com/p/dex2jar/issues/entry if possible.");
            }
        }
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), FilenameUtils.getBaseName(srcDex.getName()) + "_dex2jar.jar"));
    }

    public static void doFile(File srcDex, File distJar) throws IOException {
        doData(DexFileReader.readDex(srcDex), distJar);
    }

    public static void main(String... args) {
        System.err.println("this cmd is deprecated, use the d2j-dex2jar if possible");
        System.out.println("dex2jar version: translator-" + Main.class.getPackage().getImplementationVersion());
        if (args.length == 0) {
            System.err.println("dex2jar file1.dexORapk file2.dexORapk ...");
            return;
        }
        String jreVersion = System.getProperty("java.specification.version");
        if (jreVersion.compareTo("1.6") < 0) {
            System.err.println("A JRE version >=1.6 is required");
            return;
        }

        boolean containsError = false;

        for (String file : args) {
            File dex = new File(file);
            final File gen = new File(dex.getParentFile(), FilenameUtils.getBaseName(file) + "_dex2jar.jar");
            System.out.println("dex2jar " + dex + " -> " + gen);
            try {
                doFile(dex, gen);
            } catch (Exception e) {
                containsError = true;
                niceExceptionMessage(new DexException(e, "while process file: [%s]", dex), 0);
            }
        }
        System.out.println("Done.");
        System.exit(containsError ? -1 : 0);
    }

    public static void niceExceptionMessage(Throwable t, int deep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deep + 1; i++) {
            sb.append(".");
        }
        sb.append(' ');
        if (t instanceof DexException) {
            sb.append(t.getMessage());
            System.err.println(sb.toString());
            if (t.getCause() != null) {
                niceExceptionMessage(t.getCause(), deep + 1);
            }
        } else {
            if (t != null) {
                System.err.println(sb.append("ROOT cause:").toString());
                t.printStackTrace(System.err);
            }
        }
    }

}
