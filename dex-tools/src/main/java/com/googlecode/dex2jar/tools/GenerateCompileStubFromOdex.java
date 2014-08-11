package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.dex.ClassVisitorFactory;
import com.googlecode.d2j.dex.Dex2Asm;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@BaseCmd.Syntax(cmd = "d2j-generate-stub-from-odex", syntax = "[options] <odex0> [odex1 ... odexN]", desc = "Genenerate no-code jar from odex")
public class GenerateCompileStubFromOdex extends BaseCmd {
    private static final int MAGIC_ODEX = 0x0A796564 & 0x00FFFFFF;// hex for 'dey ', ignore the 0A
    private static final int MAGIC_DEX = 0x0A786564 & 0x00FFFFFF;// hex for 'dex ', ignore the 0A

    public static void main(String... args) {
        new GenerateCompileStubFromOdex().doMain(args);
    }

    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is stub.jar", argName = "out-jar-file")
    private Path output;
    @Opt(opt = "npri", longOpt = "no-private", description = "", hasArg = false)
    private boolean noPrivate;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            throw new HelpException("no odex");
        }
        if (output == null) {
            output = new File("stub.jar").toPath();
        }

        try (FileSystem fs = createZip(output)) {
            Path out = fs.getPath("/");
            for (String x : remainingArgs) {
                System.err.println("process " + x + " ...");
                ByteBuffer bs = ByteBuffer.wrap(Files.readAllBytes(new File(x).toPath()))
                        .order(ByteOrder.LITTLE_ENDIAN);
                int magic = bs.getInt(0) & 0x00FFFFFF;
                if (magic == MAGIC_ODEX) {
                    int offset = bs.getInt(8);
                    int length = bs.getInt(12);
                    bs.position(offset);
                    ByteBuffer n = (ByteBuffer) bs.slice().limit(length);
                    doDex(n, out);
                } else if (magic == MAGIC_DEX) {
                    doDex(bs, out);
                } else {
                    throw new RuntimeException("file " + x + " is not an dex or odex");
                }

            }
        }
    }

    private void doDex(ByteBuffer bs, final Path out) {
        DexFileReader reader = new DexFileReader(bs);
        DexFileNode fileNode = new DexFileNode();
        reader.accept(fileNode, DexFileReader.SKIP_CODE);
        Dex2Asm dex2Asm = new Dex2Asm();
        dex2Asm.convertDex(fileNode, new ClassVisitorFactory() {
            @Override
            public ClassVisitor create(final String classInternalName) {
                final Path target = out.resolve(classInternalName + ".class");
                if (Files.exists(target)) {
                    System.err.println("class " + classInternalName + " is already exists, skipping.");
                    return null;
                }
                return new ClassVisitor(Opcodes.ASM4, new ClassWriter(ClassWriter.COMPUTE_MAXS)) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        ClassWriter cw = (ClassWriter) cv;
                        byte[] data = cw.toByteArray();
                        try {
                            BaseCmd.createParentDirectories(target);
                            Files.write(target, data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                        if (noPrivate && 0 != (access & Opcodes.ACC_PRIVATE)) {
                            return null;
                        }
                        return super.visitField(access, name, desc, signature, value);
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                            String[] exceptions) {
                        if (noPrivate && 0 != (access & Opcodes.ACC_PRIVATE)) {
                            return null;
                        }
                        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                        if (0 != ((Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT) & access)) {
                            return mv;
                        }
                        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                        mv.visitInsn(Opcodes.DUP);
                        mv.visitLdcInsn("stub");
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>",
                                "(Ljava/lang/String;)V");
                        mv.visitInsn(Opcodes.ATHROW);
                        return mv;
                    }
                };
            }
        });
    }
}
