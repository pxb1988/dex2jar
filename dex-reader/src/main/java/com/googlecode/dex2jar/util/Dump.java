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
package com.googlecode.dex2jar.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.ProxyOutputStream;

import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Dump extends EmptyVisitor {
    public interface WriterManager {
        PrintWriter get(String name);
    }

    public static void doData(DexFileReader dexFileReader, File destJar) throws IOException {
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destJar)));
        dexFileReader.accept(new Dump(new WriterManager() {

            public PrintWriter get(String name) {
                try {
                    String s = name.replace('.', '/') + ".dump.txt";
                    ZipEntry zipEntry = new ZipEntry(s);
                    zos.putNextEntry(zipEntry);
                    return new PrintWriter(new ProxyOutputStream(zos) {
                        @Override
                        public void close() throws IOException {
                            zos.closeEntry();
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        zos.finish();
        zos.close();
    }

    public static void doData(byte[] data, File destJar) throws IOException {
        doData(new DexFileReader(data), destJar);
    }

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + "_dump.jar"));
    }

    public static void doFile(File srcDex, File destJar) throws IOException {
        doData(DexFileReader.readDex(srcDex), destJar);
    }

    public static String getAccDes(int acc) {
        StringBuilder sb = new StringBuilder();
        if ((acc & DexOpcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((acc & DexOpcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((acc & DexOpcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((acc & DexOpcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((acc & DexOpcodes.ACC_ABSTRACT) != 0 && (acc & DexOpcodes.ACC_INTERFACE) == 0) {
            sb.append("abstract ");
        }
        if ((acc & DexOpcodes.ACC_ANNOTATION) != 0) {
            sb.append("annotation ");
        }
        if ((acc & DexOpcodes.ACC_BRIDGE) != 0) {
            sb.append("bridge ");
        }
        if ((acc & DexOpcodes.ACC_ENUM) != 0) {
            sb.append("enum ");
        }
        if ((acc & DexOpcodes.ACC_FINAL) != 0) {
            sb.append("final ");
        }
        if ((acc & DexOpcodes.ACC_INTERFACE) != 0) {
            sb.append("interace ");
        }
        if ((acc & DexOpcodes.ACC_NATIVE) != 0) {
            sb.append("native ");
        }
        if ((acc & DexOpcodes.ACC_STRICT) != 0) {
            sb.append("strict ");
        }
        if ((acc & DexOpcodes.ACC_SYNCHRONIZED) != 0) {
            sb.append("synchronized ");
        }
        if ((acc & DexOpcodes.ACC_TRANSIENT) != 0) {
            sb.append("transient ");
        }
        if ((acc & DexOpcodes.ACC_VARARGS) != 0) {
            sb.append("varargs ");
        }
        if ((acc & DexOpcodes.ACC_VOLATILE) != 0) {
            sb.append("volatile ");
        }
        return sb.toString();
    }

    public static void main(String... args) throws IOException {
        if (args.length < 2) {
            System.out.println("Dump in.dexORapk out.dump.jar");
            return;
        }
        doFile(new File(args[0]), new File(args[1]));
    }

    private int class_count = 0;

    private PrintWriter out;

    private WriterManager writerManager;

    /**
     * @param dfv
     */
    public Dump(WriterManager writerManager) {
        super();
        this.writerManager = writerManager;
    }

    public static String toJavaClass(String desc) {
        switch (desc.charAt(0)) {
        case 'L':
            return desc.substring(1, desc.length() - 1).replace('/', '.');
        case 'B':
            return "byte";
        case 'S':
            return "short";
        case 'C':
            return "char";

        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'F':
            return "float";
        case 'D':
            return "double";
        case '[':
            return toJavaClass(desc.substring(1)) + "[]";
        }
        return desc;
    }

    StringBuilder deps = new StringBuilder();

    @Override
    public void visitDepedence(String name, byte[] checksum) {
        deps.append("dep: " + name + ", checksum: ");
        for (int i = 0; i < checksum.length; i++) {
            deps.append(String.format("%02x", checksum[i]));
        }
        deps.append("\n");
    }

    public void visitEnd() {
        if (deps.length() > 0) {
            PrintWriter out = writerManager.get("depedence");
            out.print(deps.toString());
            out.flush();
            out.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {

        String javaClassName = toJavaClass(className);
        out = writerManager.get(javaClassName);
        out.printf("//class:%04d  access:0x%04x\n", class_count++, access_flags);
        out.print(getAccDes(access_flags));
        if ((access_flags & DexOpcodes.ACC_INTERFACE) == 0) {
            out.print("class ");
        }
        out.print(javaClassName);

        if (superClass != null) {
            if (!"Ljava/lang/Object;".equals(superClass)) {
                out.print(" extends ");
                out.print(toJavaClass(superClass));
            }
        }
        if (interfaceNames != null && interfaceNames.length > 0) {
            out.print(" implements ");
            out.print(toJavaClass(interfaceNames[0]));
            for (int i = 1; i < interfaceNames.length; i++) {
                out.print(',');
                out.print(toJavaClass(interfaceNames[i]));
            }
        }
        out.println();
        return new EmptyVisitor() {

            int field_count = 0;

            int method_count = 0;

            @Override
            public void visitEnd() {
                out.flush();
                out.close();
                out = null;
                super.visitEnd();
            }

            public DexFieldVisitor visitField(int accesFlags, Field field, Object value) {
                out.printf("//field:%04d  access:0x%04x\n", field_count++, accesFlags);
                out.printf("//%s\n", field);
                out.printf("%s %s %s", getAccDes(accesFlags), toJavaClass(field.getType()), field.getName());
                if (value != null) {
                    out.print('=');
                    out.print(value);
                }
                out.println(';');

                return null;
            }

            public DexMethodVisitor visitMethod(final int accesFlags, final Method method) {
                out.println();
                out.printf("//method:%04d  access:0x%04x\n", method_count++, accesFlags);
                out.printf("//%s\n", method);

                out.printf("%s%s %s(", getAccDes(accesFlags), toJavaClass(method.getReturnType()), method.getName());
                String ps[] = method.getParameterTypes();
                if (ps != null && ps.length > 0) {
                    out.print(toJavaClass(ps[0]));
                    for (int i = 1; i < ps.length; i++) {
                        out.print(',');
                        out.print(toJavaClass(ps[i]));
                    }
                }
                out.println(')');

                return new EmptyVisitor() {
                    public DexCodeVisitor visitCode() {
                        return new DumpDexCodeAdapter((accesFlags & DexOpcodes.ACC_STATIC) != 0, method, out);
                    }
                };
            }

        };
    }
}
