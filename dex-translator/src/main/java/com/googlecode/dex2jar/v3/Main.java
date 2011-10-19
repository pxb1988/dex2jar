/*
 * Copyright (c) 2009-2011 Panxiaobo
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.Version;
import com.googlecode.dex2jar.reader.DexFileReader;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void doData(byte[] data, File destJar) throws IOException {
        final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(destJar));

        DexFileReader reader = new DexFileReader(data);
        V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
        reader.accept(afa);
        reader.accept(new V3(afa.getAccessFlagsMap(), afa.getInnerNameMap(), afa.getExtraMember(), new ClassVisitorFactory() {
            public ClassVisitor create(final String name) {
                return new ClassWriter(ClassWriter.COMPUTE_MAXS) {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.objectweb.asm.ClassWriter#visitEnd()
                     */
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        try {
                            byte[] data = this.toByteArray();
                            ZipEntry entry = new ZipEntry(name + ".class");
                            zos.putNextEntry(entry);
                            zos.write(data);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
        }));
        zos.finish();
        zos.close();
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), FilenameUtils.getBaseName(srcDex.getName()) + "_dex2jar.jar"));
    }

    public static void doFile(File srcDex, File distJar) throws IOException {
        doData(readClasses(srcDex), distJar);
    }

    /**
     * @param args
     */
    public static void main(String... args) {
        log.info("version:" + Version.getVersionString());
        if (args.length == 0) {
            System.err.println("dex2jar file1.dexORapk file2.dexORapk ...");
            return;
        }
        String jreVersion = System.getProperty("java.specification.version");
        if (jreVersion.compareTo("1.6") < 0) {
            System.err.println("A JRE version >=1.6 is required");
            return;
        }

        log.debug("DexFileReader.ContinueOnException = true;");
        DexFileReader.ContinueOnException = true;

        boolean containsError = false;

        for (String file : args) {
            File dex = new File(file);
            final File gen = new File(dex.getParentFile(),FilenameUtils.getBaseName(file) + "_dex2jar.jar");
            log.info("dex2jar {} -> {}", dex, gen);
            try {
                doFile(dex, gen);
            } catch (Exception e) {
                containsError = true;
                niceExceptionMessage(log, new DexException(e, "while process file: [%s]", dex), 0);
            }
        }
        log.info("Done.");
        System.exit(containsError ? -1 : 0);
    }

    public static void niceExceptionMessage(Logger log, Throwable t, int deep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deep + 1; i++) {
            sb.append(".");
        }
        sb.append(' ');
        if (t instanceof DexException) {
            sb.append(t.getMessage());
            log.error(sb.toString());
            if (t.getCause() != null) {
                niceExceptionMessage(log, t.getCause(), deep + 1);
            }
        } else {
            if (t != null) {
                log.error(sb.append("ROOT cause:").toString(), t);
            }
        }
    }

    public static byte[] readClasses(File srcDex) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(srcDex);
        // checkMagic
        if ("dex".equals(new String(data, 0, 3))) {// dex
            return data;
        } else if ("PK".equals(new String(data, 0, 2))) {// ZIP
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                if (entry.getName().equals("classes.dex")) {
                    return IOUtils.toByteArray(zis);
                }
            }
        }
        throw new RuntimeException("the src file not a .dex file or a zip file");
    }

}
