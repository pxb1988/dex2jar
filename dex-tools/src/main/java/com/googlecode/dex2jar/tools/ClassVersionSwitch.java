package com.googlecode.dex2jar.tools;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public final class ClassVersionSwitch {

    private ClassVersionSwitch() {
        throw new UnsupportedOperationException();
    }

    public static void main(String... args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: clz-version-switch version old.jar new.jar");
            System.exit(1);
        }
        int version = Integer.parseInt(args[0]);
        if (version < 1 || version >= Constants.JAVA_VERSIONS.length) {
            throw new RuntimeException("version not supported yet!");
        }
        File old = new File(args[1]);
        File n = new File(args[2]);
        byte[] buff = new byte[1024 * 50];
        final int jVersion = Constants.JAVA_VERSIONS[version];
        try (ZipFile zip = new ZipFile(old); ZipOutputStream zos =
                new ZipOutputStream(Files.newOutputStream(n.toPath()))) {

            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = e.nextElement();
                zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                if (!zipEntry.isDirectory()) {
                    try (InputStream is = zip.getInputStream(zipEntry)) {
                        if (zipEntry.getName().endsWith(".class")) {
                            ClassReader cr = new ClassReader(is);
                            ClassWriter cw = new ClassWriter(0);
                            ClassVisitor cv = new ClassVisitor(Constants.ASM_VERSION, cw) {
                                @Override
                                public void visit(int version, int access, String name, String signature,
                                                  String superName, String[] interfaces) {
                                    super.visit(jVersion, access, name, signature, superName, interfaces);
                                }
                            };
                            cr.accept(cv, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);
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
