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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.AbstractVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.DexException;

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

    public static void checkZipFile(File zip) throws ZipException, Exception {
        ZipFile zipFile = new ZipFile(zip);
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                log.info("checking {}", entry.getName());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                InputStream is = zipFile.getInputStream(entry);
                try {
                    verify(new ClassReader(IOUtils.toByteArray(is)));
                } finally {
                    IOUtils.closeQuietly(is);
                }
                Assert.assertTrue(sw.toString(), sw.toString().length() == 0);
            }
        }
    }

    public static void verify(final ClassReader cr) throws AnalyzerException, IllegalArgumentException,
            IllegalAccessException {
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG);

        Type syperType = cn.superName == null ? null : Type.getObjectType(cn.superName);
        List methods = cn.methods;

        List interfaces = new ArrayList();
        for (Iterator i = cn.interfaces.iterator(); i.hasNext();) {
            interfaces.add(Type.getObjectType(i.next().toString()));
        }

        for (int i = 0; i < methods.size(); ++i) {
            MethodNode method = (MethodNode) methods.get(i);
            BasicVerifier verifier = new BasicVerifier();
            Analyzer a = new Analyzer(verifier);
            try {
                a.analyze(cn.name, method);
            } catch (Exception e) {
                printAnalyzerResult(method, a, new PrintWriter(System.out));
                throw new DexException("method " + method.name + " " + method.desc, e);
            }
        }
    }

    static Field buf;
    static {
        try {
            buf = AbstractVisitor.class.getDeclaredField("buf");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        buf.setAccessible(true);

    }

    static void printAnalyzerResult(MethodNode method, Analyzer a, final PrintWriter pw)
            throws IllegalArgumentException, IllegalAccessException {
        Frame[] frames = a.getFrames();
        TraceMethodVisitor mv = new TraceMethodVisitor();
        String format = "%05d %-" + (method.maxStack + method.maxLocals + 6) + "s|%s";
        for (int j = 0; j < method.instructions.size(); ++j) {
            method.instructions.get(j).accept(mv);

            StringBuffer s = new StringBuffer();
            Frame f = frames[j];
            if (f == null) {
                s.append('?');
            } else {
                for (int k = 0; k < f.getLocals(); ++k) {
                    s.append(getShortName(f.getLocal(k).toString()));
                }
                s.append(" : ");
                for (int k = 0; k < f.getStackSize(); ++k) {
                    s.append(getShortName(f.getStack(k).toString()));
                }
            }
            pw.printf(format, j, s, buf.get(mv)); // mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            pw.print(" " + buf.get(mv));
        }
        pw.println();
        pw.flush();
    }

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

    public static void breakPoint() {
    }

}