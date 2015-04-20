package com.googlecode.dex2jar.tools;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ClassVersionSwitch {
    static final int jVersions[] = new int[]{
            0,
            Opcodes.V1_1,
            Opcodes.V1_2,
            Opcodes.V1_3,
            Opcodes.V1_4,
            Opcodes.V1_5,
            Opcodes.V1_6,
            Opcodes.V1_7,
            52,
            53,
    };

    public static void main(String... args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: clz-version-switch version old.jar new.jar");
            System.exit(1);
        }
        int version = Integer.parseInt(args[0]);
        if (version < 1 || version > 9) {
            throw new RuntimeException("version not support yet!");
        }
        File old = new File(args[1]);
        File n = new File(args[2]);
        byte[] buff = new byte[1024 * 50];
        final int jVersion = jVersions[version];
        try (ZipFile zip = new ZipFile(old); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(n));) {

            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry zipEntry = e.nextElement();
                zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                if (!zipEntry.isDirectory()) {
                    try (InputStream is = zip.getInputStream(zipEntry)) {
                        if (zipEntry.getName().endsWith(".class")) {
                            ClassReader cr = new ClassReader(is);
                            ClassWriter cw = new ClassWriter(0);
                            ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {
                                @Override
                                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                    super.visit(jVersion, access, name, signature, superName, interfaces);
                                }
                            };
                            cr.accept(cv, ClassReader.EXPAND_FRAMES|ClassReader.SKIP_FRAMES);
                            zos.write(cw.toByteArray());
                        } else {
                            for (int c = is.read(buff); c > 0; c = is.read(buff)) {
                                zos.write(buff, 0, c);
                            }
                        }
                    }

                }
                zos.closeEntry();
            }
        }
    }
}
