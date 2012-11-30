package p.rn.owner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

public class ReOwner {
    public byte[] reOwnerOne(String className, InputStream is) throws IOException {
        return reOwnerOne(className, IOUtils.toByteArray(is));
    }

    public byte[] reOwnerOne(String className, byte[] data) throws IOException {
        if (ignores.contains(className)) {
            return data;
        }
        ClassWriter cw = new ClassWriter(0);
        new ClassReader(data).accept(new RemappingClassAdapter(new ClassAdapter(cw) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (mv == null)
                    return null;
                return new MethodAdapter(mv) {

                    @Override
                    public void visitMethodInsn(final int opcode, final String owner, final String name,
                            final String desc) {

                        String nOwner = mz.get(owner + "." + name);
                        String nName = name;
                        if (nOwner == null) {
                            nOwner = mz.get(owner + "." + name + desc.substring(0, desc.lastIndexOf(')') + 1));
                        }
                        if (nOwner == null) {
                            nOwner = owner;
                        } else {
                            int index = nOwner.indexOf('.');
                            if (index > 0) {
                                nName = nOwner.substring(index + 1);
                                nOwner = nOwner.substring(0, index);
                            }
                        }
                        super.visitMethodInsn(opcode, nOwner, nName, desc);

                    }

                };
            }

        }, new SimpleRemapper(clz)), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    public void reOwner(File from, File to) throws IOException {

        final OutHandler zo = FileOut.create(to);
        try {
            new FileWalker().withStreamHandler(new StreamHandler() {

                public void handle(boolean isDir, String name, StreamOpener current, Object nameObject)
                        throws IOException {
                    if (!isDir) {
                        if (name.endsWith(".class")) {
                            String clzName = name.substring(0, name.length() - ".class".length());
                            if (ignores.contains(clzName)) {
                                zo.write(isDir, name, current.get(), nameObject);
                            } else {
                                zo.write(isDir, name, reOwnerOne(clzName, current.get()), nameObject);
                            }
                        } else {
                            if (name.startsWith("META-INF/")) {
                                if (name.equals(JarFile.MANIFEST_NAME)) {
                                    Manifest mf = new Manifest(current.get());
                                    mf.getMainAttributes().put(new Name("X-NOTICE"), "Modified");
                                    mf.getEntries().clear();

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    mf.write(baos);
                                    baos.flush();
                                    zo.write(isDir, name, baos.toByteArray(), nameObject);
                                } else if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".SF")) {
                                    // ignored
                                } else {
                                    zo.write(isDir, name, current.get(), nameObject);
                                }
                            } else {
                                zo.write(isDir, name, current.get(), nameObject);
                            }
                        }
                    }
                }
            }).walk(from);
        } finally {
            IOUtils.closeQuietly(zo);
        }
    }

    private Map<String, String> mz = new HashMap<String, String>();
    private Set<String> ignores = new HashSet<String>();
    private Map<String, String> clz = new HashMap<String, String>();

    public ReOwner withConfig(File config) throws IOException {
        if (config != null) {
            InputStream is = null;
            try {
                is = FileUtils.openInputStream(config);
                return withConfig(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return this;
    }

    public ReOwner withConfig(InputStream is) throws IOException {

        if (is != null) {
            for (String ln : IOUtils.readLines(is, "UTF-8")) {
                if ("".equals(ln) || ln.startsWith("#")) {
                    continue;
                }
                switch (ln.charAt(0)) {
                case 'i':
                case 'I':
                    ignores.add(ln.substring(2));
                    break;
                case 'c':
                case 'C':
                    int index = ln.lastIndexOf('=');
                    if (index > 0) {
                        String key = ln.substring(2, index);
                        String value = ln.substring(index + 1);
                        clz.put(key, value);
                        ignores.add(value);
                    }
                    break;
                case 'R':
                case 'r':
                    index = ln.lastIndexOf('=');
                    if (index > 0) {
                        String key = ln.substring(2, index);
                        String value = ln.substring(index + 1);
                        index = key.lastIndexOf(')');
                        if (index > 0) {
                            mz.put(key.substring(0, index + 1), value);
                        } else {
                            mz.put(key, value);
                        }
                        index = value.indexOf('.');
                        if (index > 0) {
                            ignores.add(value.substring(0, index - 1));
                        } else {
                            ignores.add(value);
                        }
                    }
                    break;
                }

            }
        }
        return this;
    }

}
