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

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Ignore;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexException;
import com.googlecode.d2j.dex.ClassVisitorFactory;
import com.googlecode.d2j.dex.Dex2Asm;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.visitors.DexClassVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@Ignore
public abstract class TestUtils {

    public static void breakPoint() {
    }

    public static void checkZipFile(File zip) throws ZipException, Exception {
        ZipFile zipFile = new ZipFile(zip);
        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                StringWriter sw = new StringWriter();
                // PrintWriter pw = new PrintWriter(sw);

                try (InputStream is = zipFile.getInputStream(entry)) {
                    verify(new ClassReader(ZipUtil.toByteArray(is)));
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

    public static File dexP(List<Path> files, File distFile) throws Exception {
        Class<?> c = com.android.dx.command.Main.class;
        Method m = c.getMethod("main", String[].class);

        if (distFile == null) {
            distFile = File.createTempFile("dex", ".dex");
        }
        List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + distFile.getCanonicalPath()));
        for (Path f : files) {
            args.add(f.toAbsolutePath().toString());
        }
        m.invoke(null, new Object[] { args.toArray(new String[0]) });
        return distFile;
    }

    public static File dex(List<File> files, File distFile) throws Exception {
        Class<?> c = com.android.dx.command.Main.class;
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

    public static List<Path> listTestDexFiles() {

        Class<?> testClass = TestUtils.class;
        URL url = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class");
        Assert.assertNotNull(url);

        final String fileStr = url.getFile();
        Assert.assertNotNull(fileStr);
        String dirx = fileStr.substring(0, fileStr.length() - testClass.getName().length() - ".class".length());

        System.out.println("dirx is " + dirx);

        File file = new File(dirx, "dexes");

        return listPath(file, ".apk", ".dex", ".zip");
    }

    public static List<Path> listPath(File file, final String... exts) {
        final List<Path> list = new ArrayList<>();

        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.getFileName().toString();
                    boolean add = false;
                    for (String ext : exts) {
                        if (name.endsWith(ext)) {
                            add = true;
                            break;
                        }
                    }
                    if (add) {
                        list.add(file);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    static Field buf;
    static {
        try {
            buf = Printer.class.getDeclaredField("buf");
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        buf.setAccessible(true);
    }

    static void printAnalyzerResult(MethodNode method, Analyzer a, final PrintWriter pw)
            throws IllegalArgumentException, IllegalAccessException {
        Frame[] frames = a.getFrames();
        Textifier t = new Textifier();
        TraceMethodVisitor mv = new TraceMethodVisitor(t);
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
            pw.printf(format, j, s, buf.get(t)); // mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            pw.print(" " + buf.get(t));
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
        DexClassNode clzNode = new DexClassNode(DexConstants.ACC_PUBLIC, "L" + generateClassName + ";",
                "Ljava/lang/Object;", null);
        Method m = clz.getMethod(methodName, DexClassVisitor.class);
        if (m == null) {
            throw new java.lang.NoSuchMethodException(methodName);
        }
        m.setAccessible(true);
        if (Modifier.isStatic(m.getModifiers())) {
            m.invoke(null, clzNode);
        } else {
            m.invoke(clz.newInstance(), clzNode);
        }
        return translateAndCheck(clzNode);
    }

    public static byte[] translateAndCheck(DexFileNode fileNode, DexClassNode clzNode) throws AnalyzerException,
            IllegalAccessException {
        // 1. convert to .class
        Dex2Asm dex2Asm = new Dex2Asm() {
            @Override
            public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
                try {
                    super.convertCode(methodNode, mv);
                } catch (Exception ex) {
                    BaksmaliDumper d = new BaksmaliDumper();
                    try {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.err, "UTF-8"));
                        d.baksmaliMethod(methodNode, out);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    throw new DexException(ex, "fail convert code %s", methodNode.method);
                }
            }
        };
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitorFactory cvf = new ClassVisitorFactory() {
            @Override
            public ClassVisitor create(String classInternalName) {
                return cw;
            }
        };
        if (fileNode != null) {
            dex2Asm.convertClass(clzNode, cvf, fileNode);
        } else {
            dex2Asm.convertClass(clzNode, cvf);
        }
        byte[] data = cw.toByteArray();

        // 2. verify .class
        ClassReader cr = new ClassReader(data);
        TestUtils.verify(cr);

        // 3. convert back to dex
        CfOptions cfOptions = new CfOptions();
        cfOptions.strictNameCheck = false;
        DexOptions dexOptions = new DexOptions();

        CfTranslator.translate("", data, cfOptions, dexOptions);
        return data;
    }

    public static byte[] translateAndCheck(DexClassNode clzNode) throws AnalyzerException, IllegalAccessException {
        return translateAndCheck(null, clzNode);
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
