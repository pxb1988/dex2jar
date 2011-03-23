/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.ExceptionUtils;
import com.googlecode.dex2jar.Version;
import com.googlecode.dex2jar.reader.DexFileReader;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

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
            final File gen = new File(file + ".dex2jar.jar");
            log.info("dex2jar {} -> {}", dex, gen);
            try {
                doFile(dex, gen);
            } catch (Exception e) {
                containsError = true;
                ExceptionUtils.niceExceptionMessage(log, new DexException(e, "while process file: [%s]", dex), 0);
            }
        }
        log.info("Done.");
        System.exit(containsError ? -1 : 0);
    }

    public static void doData(byte[] data, File destJar) throws IOException {
        // final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(destJar));

        DexFileReader reader = new DexFileReader(data);
        SootAccessFlagsAdapter afa = new SootAccessFlagsAdapter();
        reader.accept(afa);
        reader.accept(new SootDexFileVisitor(afa.getAccessFlagsMap()));
        // zos.finish();
        // zos.close();
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + ".dex2jar.jar"));
    }

    public static void doFile(File srcDex, File destJar) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(srcDex);
        // checkMagic
        if ("dex".equals(new String(data, 0, 3))) {// dex
            doData(data, destJar);
        } else if ("PK".equals(new String(data, 0, 2))) {// ZIP
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                if (entry.getName().equals("classes.dex")) {
                    data = IOUtils.toByteArray(zis);
                    doData(data, destJar);
                }
            }
        } else {
            throw new RuntimeException("the src file not a .dex file or a zip file");
        }

    }

}
