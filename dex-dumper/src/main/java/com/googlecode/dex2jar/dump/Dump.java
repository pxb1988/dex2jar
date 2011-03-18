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
package com.googlecode.dex2jar.dump;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyOutputStream;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.visitors.DexClassAdapter;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;
import com.googlecode.dex2jar.visitors.DexMethodAdapter;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Dump implements DexFileVisitor {
    public interface WriterManager {
        PrintWriter get(String name);
    }

    public static void doData(byte[] data, File destJar) throws IOException {
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destJar)));
        new DexFileReader(data).accept(new Dump(new EmptyVisitor(), new WriterManager() {

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

    public static void doFile(File srcDex) throws IOException {
        doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + ".dump.jar"));
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

    public static String getAccDes(int acc) {
        StringBuilder sb = new StringBuilder();
        if ((acc & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((acc & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((acc & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((acc & Opcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((acc & Opcodes.ACC_ABSTRACT) != 0 && (acc & Opcodes.ACC_INTERFACE) == 0) {
            sb.append("abstract ");
        }
        if ((acc & Opcodes.ACC_ANNOTATION) != 0) {
            sb.append("annotation ");
        }
        if ((acc & Opcodes.ACC_BRIDGE) != 0) {
            sb.append("bridge ");
        }
        if ((acc & Opcodes.ACC_DEPRECATED) != 0) {
            sb.append("deprecated ");
        }
        if ((acc & Opcodes.ACC_ENUM) != 0) {
            sb.append("enum ");
        }
        if ((acc & Opcodes.ACC_FINAL) != 0) {
            sb.append("final ");
        }
        if ((acc & Opcodes.ACC_INTERFACE) != 0) {
            sb.append("interace ");
        }
        if ((acc & Opcodes.ACC_NATIVE) != 0) {
            sb.append("native ");
        }
        if ((acc & Opcodes.ACC_STRICT) != 0) {
            sb.append("strict ");
        }
        if ((acc & Opcodes.ACC_SYNCHRONIZED) != 0) {
            sb.append("synchronized ");
        }
        if ((acc & Opcodes.ACC_TRANSIENT) != 0) {
            sb.append("transient ");
        }
        if ((acc & Opcodes.ACC_VARARGS) != 0) {
            sb.append("varargs ");
        }
        if ((acc & Opcodes.ACC_VOLATILE) != 0) {
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

    private DexFileVisitor dfv;

    private PrintWriter out;

    private WriterManager writerManager;

    /**
     * @param dfv
     */
    public Dump(DexFileVisitor dfv, WriterManager writerManager) {
        super();
        this.dfv = dfv;
        this.writerManager = writerManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {

        String javaClassName = Type.getType(className).getClassName();
        out = writerManager.get(javaClassName);
        out.printf("//class:%04d  access:0x%04x\n", class_count++, access_flags);
        out.print(getAccDes(access_flags));
        if ((access_flags & Opcodes.ACC_INTERFACE) == 0) {
            out.print("class ");
        }
        out.print(javaClassName);

        if (superClass != null) {
            if (!"Ljava/lang/Object;".equals(superClass)) {
                out.print(" extends ");
                out.print(Type.getType(superClass).getClassName());
            }
        }
        if (interfaceNames != null && interfaceNames.length > 0) {
            out.print(" implements ");
            out.print(Type.getType(interfaceNames[0]).getClassName());
            for (int i = 1; i < interfaceNames.length; i++) {
                out.print(',');
                out.print(Type.getType(interfaceNames[i]).getClassName());
            }
        }
        out.println();
        DexClassVisitor dcv = dfv.visit(access_flags, className, superClass, interfaceNames);
        if (dcv == null)
            return null;
        return new DexClassAdapter(dcv) {

            int field_count = 0;

            int method_count = 0;

            @Override
            public void visitEnd() {
                out.flush();
                out.close();
                out = null;
                super.visitEnd();
            }

            public DexFieldVisitor visitField(Field field, Object value) {
                out.printf("//field:%04d  access:0x%04x\n", field_count++, field.getAccessFlags());
                out.printf("//%s\n", field);
                out.printf("%s %s %s", getAccDes(field.getAccessFlags()), Type.getType(field.getType()).getClassName(), field.getName());
                if (value != null) {
                    out.print('=');
                    out.print(value);
                }
                out.println(';');

                return dcv.visitField(field, value);
            }

            public DexMethodVisitor visitMethod(final Method method) {
                out.println();
                out.printf("//method:%04d  access:0x%04x\n", method_count++, method.getAccessFlags());
                out.printf("//%s\n", method);

                out.printf("%s%s %s(", getAccDes(method.getAccessFlags()), Type.getType(method.getType().getReturnType()).getClassName(), method.getName());
                String ps[] = method.getType().getParameterTypes();
                if (ps != null && ps.length > 0) {
                    out.print(Type.getType(ps[0]).getClassName());
                    for (int i = 1; i < ps.length; i++) {
                        out.print(',');
                        out.print(Type.getType(ps[i]).getClassName());
                    }
                }
                out.println(')');

                DexMethodVisitor dmv = dcv.visitMethod(method);
                if (dmv == null) {
                    return null;
                }
                return new DexMethodAdapter(dmv) {
                    public DexCodeVisitor visitCode() {
                        DexCodeVisitor dcv = mv.visitCode();
                        if (dcv == null)
                            return null;
                        return new DumpDexCodeAdapter(dcv, method, out);
                    }
                };
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {
        dfv.visitEnd();
    }

}
