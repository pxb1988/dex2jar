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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.AbstractVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@Ignore
public abstract class TestUtils {

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

    public static void breakPoint() {
    }

    public static void checkZipFile(File zip) throws ZipException, Exception {
        ZipFile zipFile = new ZipFile(zip);
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                StringWriter sw = new StringWriter();
                // PrintWriter pw = new PrintWriter(sw);
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

    public static File dex(File file, File distFile) throws Exception {
        return dex(new File[] { file }, distFile);
    }

    public static File dex(File[] files) throws Exception {
        return dex(files, null);
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

        if (distFile == null) {
            distFile = File.createTempFile("dex", ".dex");
        }
        List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + distFile.getCanonicalPath()));
        for (File f : files) {
            args.add(f.getCanonicalPath());
        }
        m.invoke(null, new Object[] { args.toArray(new String[0]) });
        return distFile;
    }

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

    public static Collection<File> listTestDexFiles() {
        return listTestDexFiles(false);
    }

    /**
     * construct a DexFileReader and set apiLevel if possible
     * 
     * @param f
     * @return
     * @throws IOException
     */
    public static DexFileReader initDexFileReader(File f) throws IOException {
        DexFileReader r = new DexFileReader(f);
        if (r.isOdex()) {
            try {
                r.setApiLevel(Integer.parseInt(f.getParentFile().getName()));
            } catch (Exception ignore) {
            }
        }
        return r;
    }

    public static Collection<File> listTestDexFiles(boolean withOdex) {
        File file = new File("target/test-classes/dexes");
        List<File> list = new ArrayList<File>();
        if (file.exists()) {
            list.addAll(FileUtils.listFiles(file, new String[] { "dex", "zip", "apk", "odex" }, false));
        }
        if (withOdex) {
            list = new ArrayList<File>();
            file = new File("target/test-classes/odexes");
            if (file.exists()) {
                list.addAll(FileUtils.listFiles(file, new String[] { "odex" }, true));
            }
        }
        return list;
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

    public static void verify(final ClassReader cr) throws AnalyzerException, IllegalArgumentException,
            IllegalAccessException {
        try {
            verify(cr, new PrintWriter(new OutputStreamWriter(System.out, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void verify(final ClassReader cr, PrintWriter out) throws AnalyzerException,
            IllegalArgumentException, IllegalAccessException {
        ClassNode cn = new ClassNode();
        cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG);

        List methods = cn.methods;

        for (int i = 0; i < methods.size(); ++i) {
            MethodNode method = (MethodNode) methods.get(i);

            List tryCatchBlocks = method.tryCatchBlocks;
            for (int j = 0; j < tryCatchBlocks.size(); j++) {
                TryCatchBlockNode tcn = (TryCatchBlockNode) tryCatchBlocks.get(j);
                if (tcn.start.equals(tcn.end)) {
                    throw new DexException("try/catch block %d in %s has same start(%s) and end(%s)", j, method.name,
                            tcn.start.getLabel(), tcn.end.getLabel());
                }
            }

            BasicVerifier verifier = new BasicVerifier();
            Analyzer a = new Analyzer(verifier);
            try {
                a.analyze(cn.name, method);
            } catch (Exception e) {
                out.println(cr.getClassName() + "." + method.name + method.desc);
                printAnalyzerResult(method, a, out);
                e.printStackTrace(out);
                out.flush();
                throw new DexException("method " + method.name + " " + method.desc, e);
            }
        }
    }

    public static byte[] testDexASMifier(Class<?> clz, String methodName) throws Exception {
        return testDexASMifier(clz, methodName, "xxxx/" + methodName);
    }

    public static byte[] testDexASMifier(Class<?> clz, String methodName, String generateClassName) throws Exception {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, generateClassName, null, "java/lang/Object", null);
        EmptyVisitor em = new EmptyVisitor() {
            public DexMethodVisitor visitMethod(int accessFlags, com.googlecode.dex2jar.Method method) {
                return new V3MethodAdapter(accessFlags, method, null, V3.OPTIMIZE_SYNCHRONIZED | V3.TOPOLOGICAL_SORT) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        methodNode.accept(cw);
                    }
                };
            }

            @Override
            public DexFieldVisitor visitField(int accessFlags, com.googlecode.dex2jar.Field field, Object value) {
                FieldVisitor fv = cw.visitField(accessFlags, field.getName(), field.getType(), null, value);
                fv.visitEnd();
                return null;
            }
        };
        Method m = clz.getMethod(methodName, DexClassVisitor.class);
        if (m == null) {
            throw new java.lang.NoSuchMethodException(methodName);
        }
        m.setAccessible(true);
        m.invoke(null, em);
        byte[] data = cw.toByteArray();
        ClassReader cr = new ClassReader(data);
        TestUtils.verify(cr);
        return data;
    }

    public static Class<?> defineClass(String type, byte[] data) {
        return new CL().xxxDefine(type, data);
    }

    static class CL extends ClassLoader {
        public Class<?> xxxDefine(String type, byte[] data) {
            return super.defineClass(type, data, 0, data.length);
        }
    }
}