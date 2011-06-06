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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public abstract class TestUtils {

    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    public static File dex(File file, File distFile) throws Exception {
        return dex(new File[] { file }, distFile);
    }

    public static File dex(File[] files, File distFile) throws Exception {
        return dex(Arrays.asList(files), distFile);
    }

    public static File dex(List<File> files, File distFile) throws Exception {
        String dxJar = "src/test/resources/dx.jar";
        File dxFile = new File(dxJar);
        if (!dxFile.exists()) {
            throw new RuntimeException("dx.jar文件不存在");
        }
        URLClassLoader cl = new URLClassLoader(new URL[] { dxFile.toURI().toURL() });
        Class<?> c = cl.loadClass("com.android.dx.command.Main");
        Method m = c.getMethod("main", String[].class);

        if (distFile == null)
            distFile = File.createTempFile("dex", ".dex");
        List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + distFile.getCanonicalPath()));
        for (File f : files) {
            args.add(f.getCanonicalPath());
        }
        m.invoke(null, new Object[] { args.toArray(new String[0]) });
        return distFile;
    }

    public static File dex(File[] files) throws Exception {
        return dex(files, null);
    }

    public static void checkZipFile(File zip) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(zip);
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                log.info("check file:{}", entry.getName());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                InputStream is = zipFile.getInputStream(entry);
                try {
                    CheckClassAdapter.verify(new ClassReader(IOUtils.toByteArray(is)), false, pw);
                } finally {
                    IOUtils.closeQuietly(is);
                }
                Assert.assertTrue(sw.toString(), sw.toString().length() == 0);
            }
        }
    }
}