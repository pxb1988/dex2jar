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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.util.ASMifierCodeV;
import com.googlecode.dex2jar.util.Escape;
import com.googlecode.dex2jar.util.Out;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Main {

    public static void doData(byte[] data, File destJar) throws IOException {
        doData(data, destJar, new HashMap<Method, Exception>());
    }

    public static void doData(byte[] data, File destJar, final Map<Method, Exception> exceptions) throws IOException {

        DexFileReader reader = new DexFileReader(data);
        if (reader.isOdex()) {
            throw new DexException("dex-translator not support translate an odex file,"
                    + " please refere smali http://code.google.com/p/smali/ to convert odex to dex");
        }

        V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
        reader.accept(afa, DexFileReader.SKIP_CODE | DexFileReader.SKIP_DEBUG);

        final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(destJar));
        try {
            reader.accept(new V3(afa.getAccessFlagsMap(), afa.getInnerNameMap(), afa.getExtraMember(), exceptions,
                    new ClassVisitorFactory() {
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
                                        e.printStackTrace(System.err);
                                    }
                                }
                            };
                        }
                    }), DexFileReader.SKIP_DEBUG);
            zos.finish();
        } catch (Exception e) {
            if (exceptions == null) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
            e.printStackTrace(System.err);
        } finally {
            zos.close();
        }

        if (exceptions != null && exceptions.size() > 0) {
            printErrorFile(reader, exceptions, destJar);
        }
    }

    static private void printErrorFile(DexFileReader reader, final Map<Method, Exception> exceptions, File destJar)
            throws IOException {
        for (Map.Entry<Method, Exception> e : exceptions.entrySet()) {
            System.err.println("Error:" + e.getKey().toString() + "->" + e.getValue().getMessage());
        }
        File errorFile = new File(destJar.getParentFile(), FilenameUtils.getBaseName(destJar.getName()) + ".error.zip");
        final ZipOutputStream errorZipOutputStream = new ZipOutputStream(FileUtils.openOutputStream(errorFile));
        errorZipOutputStream.putNextEntry(new ZipEntry("summary.txt"));
        final PrintWriter fw = new PrintWriter(new OutputStreamWriter(errorZipOutputStream, "UTF-8"));
        fw.println(getVersionString());
        fw.println("there are " + exceptions.size() + " error methods");
        fw.flush();
        errorZipOutputStream.closeEntry();
        final Out out = new Out() {

            @Override
            public void s(String format, Object... arg) {
                fw.println(String.format(format, arg));
            }

            @Override
            public void s(String s) {
                fw.println(s);
            }

            @Override
            public void push() {

            }

            @Override
            public void pop() {

            }
        };
        final int[] count = new int[] { 0 };
        reader.accept(new EmptyVisitor() {

            @Override
            public DexClassVisitor visit(int accessFlags, String className, String superClass, String[] interfaceNames) {
                return new EmptyVisitor() {

                    @Override
                    public DexMethodVisitor visitMethod(final int accessFlags, final Method method) {
                        if (exceptions.containsKey(method)) {
                            return new EmptyVisitor() {

                                @Override
                                public DexCodeVisitor visitCode() {
                                    try {
                                        errorZipOutputStream.putNextEntry(new ZipEntry("t" + count[0]++ + ".txt"));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    Exception exception = exceptions.get(method);
                                    exception.printStackTrace(fw);
                                    out.s("");
                                    out.s("DexMethodVisitor mv=cv.visitMethod(%s, %s);", Escape.methodAcc(accessFlags),
                                            Escape.v(method));
                                    out.s("DexCodeVisitor code = mv.visitCode();");
                                    return new ASMifierCodeV(out);
                                }

                                @Override
                                public void visitEnd() {
                                    out.s("mv.visitEnd();");
                                    fw.flush();
                                    try {
                                        errorZipOutputStream.closeEntry();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            };
                        }
                        return null;
                    }

                };
            }

        }, DexFileReader.SKIP_DEBUG);
        errorZipOutputStream.close();
        System.err.println("Detail Error Information in File " + errorFile);
        System.err.println("Please report this file to http://code.google.com/p/dex2jar/issues/entry if possible.");
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), FilenameUtils.getBaseName(srcDex.getName()) + "_dex2jar.jar"));
    }

    public static void doFile(File srcDex, File distJar) throws IOException {
        doData(DexFileReader.readDex(srcDex), distJar);
    }

    public static String getVersionString() {
        return "dex2jar version: reader-" + DexFileReader.class.getPackage().getImplementationVersion()
                + ", translator-" + Main.class.getPackage().getImplementationVersion() + ", ir-"
                + ET.class.getPackage().getImplementationVersion();
    }

    /**
     * @param args
     */
    public static void main(String... args) {
        System.out.println(getVersionString());
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
