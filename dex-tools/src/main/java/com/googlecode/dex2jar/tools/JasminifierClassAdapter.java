/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.googlecode.dex2jar.tools;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MemberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.util.AbstractVisitor;

/**
 * <b>get from asm example</b>
 * <p>
 * A {@link ClassVisitor} that prints a disassembled view of the classes it visits in Jasmin assembler format. This
 * class visitor can be used alone (see the {@link #main main} method) to disassemble a class. It can also be used in
 * the middle of class visitor chain to trace the class that is visited at a given point in this chain. This may be
 * uselful for debugging purposes.
 * <p>
 * The trace printed when visiting the <tt>Hello</tt> class is the following:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * .bytecode 45.3
 * .class public Hello
 * .super java/lang/Object
 * 
 * .method public <init>()V
 * aload 0
 * invokespecial java/lang/Object/<init>()V
 * return
 * .limit locals 1
 * .limit stack 1
 * .end method
 * 
 * .method public static main([Ljava/lang/String;)V
 * getstatic java/lang/System/out Ljava/io/PrintStream;
 * ldc "hello"
 * invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
 * return
 * .limit locals 2
 * .limit stack 2
 * .end method
 * </pre>
 * 
 * </blockquote> where <tt>Hello</tt> is defined by:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * public class Hello {
 * 
 *     public static void main(String[] args) {
 *         System.out.println(&quot;hello&quot;);
 *     }
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Eric Bruneton
 */
public class JasminifierClassAdapter extends ClassAdapter {

    /**
     * The print writer to be used to print the class.
     */
    protected PrintWriter pw;

    /**
     * The label names. This map associate String values to Label keys.
     */
    protected final Map labelNames;

    /**
     * Prints a disassembled view of the given class in Jasmin assembler format to the standard output.
     * <p>
     * Usage: JasminifierClassAdapter [-debug] &lt;fully qualified class name or class file name &gt;
     * 
     * @param args
     *            the command line arguments.
     * 
     * @throws Exception
     *             if the class cannot be found, or if an IO exception occurs.
     */
    public static void main(final String[] args) throws Exception {
        int i = 0;
        int flags = ClassReader.SKIP_DEBUG;

        boolean ok = true;
        if (args.length < 1 || args.length > 2) {
            ok = false;
        }
        if (ok && "-debug".equals(args[0])) {
            i = 1;
            flags = 0;
            if (args.length != 2) {
                ok = false;
            }
        }
        if (!ok) {
            System.err.println("Prints a disassembled view of the given class.");
            System.err.println("Usage: JasminifierClassAdapter [-debug] "
                    + "<fully qualified class name or class file name>");
            return;
        }
        ClassReader cr;
        if (args[i].endsWith(".class") || args[i].indexOf('\\') > -1 || args[i].indexOf('/') > -1) {
            cr = new ClassReader(new FileInputStream(args[i]));
        } else {
            cr = new ClassReader(args[i]);
        }
        cr.accept(new JasminifierClassAdapter(new PrintWriter(System.out, true), null), flags
                | ClassReader.EXPAND_FRAMES);
    }

    /**
     * Constructs a new {@link JasminifierClassAdapter}.
     * 
     * @param pw
     *            the print writer to be used to print the class.
     * @param cv
     *            the {@link ClassVisitor} to which this visitor delegates calls. May be <tt>null</tt>.
     */
    public JasminifierClassAdapter(final PrintWriter pw, final ClassVisitor cv) {
        super(new ClassNode() {
            public void visitEnd() {
                if (cv != null) {
                    accept(cv);
                }
            }
        });
        this.pw = pw;
        labelNames = new HashMap();
    }

    public void visitEnd() {
        ClassNode cn = (ClassNode) cv;
        pw.print(".bytecode ");
        pw.print(cn.version & 0xFFFF);
        pw.print('.');
        pw.println(cn.version >>> 16);
        println(".source ", cn.sourceFile);
        pw.print(".class");
        pw.print(access(cn.access));
        pw.print(' ');
        pw.println(cn.name);
        if (cn.superName == null) { // TODO Jasmin bug workaround
            println(".super ", "java/lang/Object");
        } else {
            println(".super ", cn.superName);
        }
        for (int i = 0; i < cn.interfaces.size(); ++i) {
            println(".implements ", (String) cn.interfaces.get(i));
        }
        if (cn.signature != null)
            println(".signature ", '"' + cn.signature + '"');
        if (cn.outerClass != null) {
            pw.print(".enclosing method ");
            pw.print(cn.outerClass);
            if (cn.outerMethod != null) {
                pw.print('/');
                pw.print(cn.outerMethod);
                pw.println(cn.outerMethodDesc);
            } else {
                pw.println();
            }
        }
        if ((cn.access & Opcodes.ACC_DEPRECATED) != 0) {
            pw.println(".deprecated");
        }
        printAnnotations(cn);
        println(".debug ", cn.sourceDebug == null ? null : '"' + cn.sourceDebug + '"');

        for (int i = 0; i < cn.innerClasses.size(); ++i) {
            InnerClassNode in = (InnerClassNode) cn.innerClasses.get(i);
            pw.print(".inner class");
            pw.print(access(in.access));
            if (in.innerName != null) {
                pw.print(' ');
                pw.print(in.innerName);
            }
            if (in.name != null) {
                pw.print(" inner ");
                pw.print(in.name);
            }
            if (in.outerName != null) {
                pw.print(" outer ");
                pw.print(in.outerName);
            }
            pw.println();
        }

        for (int i = 0; i < cn.fields.size(); ++i) {
            FieldNode fn = (FieldNode) cn.fields.get(i);
            boolean annotations = false;
            if (fn.visibleAnnotations != null && fn.visibleAnnotations.size() > 0) {
                annotations = true;
            }
            if (fn.invisibleAnnotations != null && fn.invisibleAnnotations.size() > 0) {
                annotations = true;
            }
            boolean deprecated = (fn.access & Opcodes.ACC_DEPRECATED) != 0;
            pw.print("\n.field");
            pw.print(access(fn.access));
            pw.print(" '");
            pw.print(fn.name);
            pw.print("' ");
            pw.print(fn.desc);
            if (fn.signature != null && (!deprecated && !annotations)) {
                pw.print(" signature \"");
                pw.print(fn.signature);
                pw.print("\"");
            }
            if (fn.value instanceof String) {
                StringBuffer buf = new StringBuffer();
                AbstractVisitor.appendString(buf, (String) fn.value);
                pw.print(" = ");
                pw.print(buf.toString());
            } else if (fn.value != null) {
                pw.print(" = ");
                print(fn.value);
                pw.println();
            }
            pw.println();
            if (fn.signature != null && (deprecated || annotations)) {
                pw.print(".signature \"");
                pw.print(fn.signature);
                pw.println("\"");
            }
            if (deprecated) {
                pw.println(".deprecated");
            }
            printAnnotations(fn);
            if (deprecated || annotations) {
                pw.println(".end field");
            }
        }

        for (int i = 0; i < cn.methods.size(); ++i) {
            MethodNode mn = (MethodNode) cn.methods.get(i);
            pw.print("\n.method");
            pw.print(access(mn.access));
            pw.print(' ');
            pw.print(mn.name);
            pw.println(mn.desc);
            if (mn.signature != null) {
                pw.print(".signature \"");
                pw.print(mn.signature);
                pw.println("\"");
            }
            if (mn.annotationDefault != null) {
                pw.println(".annotation default");
                printAnnotationValue(mn.annotationDefault);
                pw.println(".end annotation");
            }
            printAnnotations(mn);
            if (mn.visibleParameterAnnotations != null) {
                for (int j = 0; j < mn.visibleParameterAnnotations.length; ++j) {
                    List l = mn.visibleParameterAnnotations[j];
                    if (l != null) {
                        for (int k = 0; k < l.size(); ++k) {
                            printAnnotation((AnnotationNode) l.get(k), 1, j + 1);
                        }
                    }
                }
            }
            if (mn.invisibleParameterAnnotations != null) {
                for (int j = 0; j < mn.invisibleParameterAnnotations.length; ++j) {
                    List l = mn.invisibleParameterAnnotations[j];
                    if (l != null) {
                        for (int k = 0; k < l.size(); ++k) {
                            printAnnotation((AnnotationNode) l.get(k), 2, j + 1);
                        }
                    }
                }
            }
            for (int j = 0; j < mn.exceptions.size(); ++j) {
                println(".throws ", (String) mn.exceptions.get(j));
            }
            if ((mn.access & Opcodes.ACC_DEPRECATED) != 0) {
                pw.println(".deprecated");
            }
            if (mn.instructions.size() > 0) {
                labelNames.clear();
                for (int j = 0; j < mn.tryCatchBlocks.size(); ++j) {
                    TryCatchBlockNode tcb = (TryCatchBlockNode) mn.tryCatchBlocks.get(j);
                    pw.print(".catch ");
                    pw.print(tcb.type);
                    pw.print(" from ");
                    print(tcb.start);
                    pw.print(" to ");
                    print(tcb.end);
                    pw.print(" using ");
                    print(tcb.handler);
                    pw.println();
                }
                for (int j = 0; j < mn.instructions.size(); ++j) {
                    AbstractInsnNode in = mn.instructions.get(j);
                    in.accept(new EmptyVisitor() {

                        public void visitFrame(int type, int local, Object[] locals, int stack, Object[] stacks) {
                            if (type != Opcodes.F_FULL && type != Opcodes.F_NEW) {
                                throw new RuntimeException("Compressed frames unsupported, use EXPAND_FRAMES option");
                            }
                            pw.println(".stack");
                            for (int i = 0; i < local; ++i) {
                                pw.print("locals ");
                                printFrameType(locals[i]);
                                pw.println();
                            }
                            for (int i = 0; i < stack; ++i) {
                                pw.print("stack ");
                                printFrameType(stacks[i]);
                                pw.println();
                            }
                            pw.println(".end stack");
                        }

                        public void visitInsn(int opcode) {
                            print(opcode);
                            pw.println();
                        }

                        public void visitIntInsn(int opcode, int operand) {
                            print(opcode);
                            if (opcode == Opcodes.NEWARRAY) {
                                switch (operand) {
                                case Opcodes.T_BOOLEAN:
                                    pw.println(" boolean");
                                    break;
                                case Opcodes.T_CHAR:
                                    pw.println(" char");
                                    break;
                                case Opcodes.T_FLOAT:
                                    pw.println(" float");
                                    break;
                                case Opcodes.T_DOUBLE:
                                    pw.println(" double");
                                    break;
                                case Opcodes.T_BYTE:
                                    pw.println(" byte");
                                    break;
                                case Opcodes.T_SHORT:
                                    pw.println(" short");
                                    break;
                                case Opcodes.T_INT:
                                    pw.println(" int");
                                    break;
                                case Opcodes.T_LONG:
                                default:
                                    pw.println(" long");
                                    break;
                                }
                            } else {
                                pw.print(' ');
                                pw.println(operand);
                            }
                        }

                        public void visitVarInsn(int opcode, int var) {
                            print(opcode);
                            pw.print(' ');
                            pw.println(var);
                        }

                        public void visitTypeInsn(int opcode, String type) {
                            print(opcode);
                            pw.print(' ');
                            pw.println(type);
                        }

                        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                            print(opcode);
                            pw.print(' ');
                            pw.print(owner);
                            pw.print('/');
                            pw.print(name);
                            pw.print(' ');
                            pw.println(desc);
                        }

                        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                            print(opcode);
                            pw.print(' ');
                            pw.print(owner);
                            pw.print('/');
                            pw.print(name);
                            pw.print(desc);
                            if (opcode == Opcodes.INVOKEINTERFACE) {
                                pw.print(' ');
                                pw.print((Type.getArgumentsAndReturnSizes(desc) >> 2) - 1);
                            }
                            pw.println();
                        }

                        public void visitJumpInsn(int opcode, Label label) {
                            print(opcode);
                            pw.print(' ');
                            print(label);
                            pw.println();
                        }

                        public void visitLabel(Label label) {
                            print(label);
                            pw.println(':');
                        }

                        public void visitLdcInsn(Object cst) {
                            pw.print("ldc ");
                            if (cst instanceof Type) {
                                pw.print(((Type) cst).getInternalName());
                            } else {
                                print(cst);
                            }
                            pw.println();
                        }

                        public void visitIincInsn(int var, int increment) {
                            pw.print("iinc ");
                            pw.print(var);
                            pw.print(' ');
                            pw.println(increment);
                        }

                        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
                            pw.print("tableswitch ");
                            pw.println(min);
                            for (int i = 0; i < labels.length; ++i) {
                                print(labels[i]);
                                pw.println();
                            }
                            pw.print("default : ");
                            print(dflt);
                            pw.println();
                        }

                        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                            if (keys.length == 0) {
                                pw.print("goto "); // TODO Jasmin bug
                                                   // workaround
                                print(dflt);
                                pw.println();
                                return;
                            }
                            pw.println("lookupswitch");
                            for (int i = 0; i < keys.length; ++i) {
                                pw.print(keys[i]);
                                pw.print(" : ");
                                print(labels[i]);
                                pw.println();
                            }
                            pw.print("default : ");
                            print(dflt);
                            pw.println();
                        }

                        public void visitMultiANewArrayInsn(String desc, int dims) {
                            pw.print("multianewarray ");
                            pw.print(desc);
                            pw.print(' ');
                            pw.println(dims);
                        }

                        public void visitLineNumber(int line, Label start) {
                            pw.print(".line ");
                            pw.println(line);
                        }
                    });
                }
                for (int j = 0; j < mn.localVariables.size(); ++j) {
                    LocalVariableNode lv = (LocalVariableNode) mn.localVariables.get(j);
                    pw.print(".var ");
                    pw.print(lv.index);
                    pw.print(" is '");
                    pw.print(lv.name);
                    pw.print("' ");
                    pw.print(lv.desc);
                    if (lv.signature != null) {
                        pw.print(" signature \"");
                        pw.print(lv.signature);
                        pw.print("\"");
                    }
                    pw.print(" from ");
                    print(lv.start);
                    pw.print(" to ");
                    print(lv.end);
                    pw.println();
                }
                println(".limit locals ", Integer.toString(mn.maxLocals));
                println(".limit stack ", Integer.toString(mn.maxStack));
            }
            pw.println(".end method");
        }
        super.visitEnd();
    }

    protected void println(final String directive, final String arg) {
        if (arg != null) {
            pw.print(directive);
            pw.println(arg);
        }
    }

    protected String access(final int access) {
        StringBuffer b = new StringBuffer();
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            b.append(" public");
        }
        if ((access & Opcodes.ACC_PRIVATE) != 0) {
            b.append(" private");
        }
        if ((access & Opcodes.ACC_PROTECTED) != 0) {
            b.append(" protected");
        }
        if ((access & Opcodes.ACC_STATIC) != 0) {
            b.append(" static");
        }
        if ((access & Opcodes.ACC_FINAL) != 0) {
            b.append(" final");
        }
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
            b.append(" synchronized");
        }
        if ((access & Opcodes.ACC_VOLATILE) != 0) {
            b.append(" volatile");
        }
        if ((access & Opcodes.ACC_TRANSIENT) != 0) {
            b.append(" transient");
        }
        if ((access & Opcodes.ACC_NATIVE) != 0) {
            b.append(" native");
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            b.append(" abstract");
        }
        if ((access & Opcodes.ACC_STRICT) != 0) {
            b.append(" fpstrict");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            b.append(" synthetic");
        }
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            b.append(" interface");
        }
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            b.append(" annotation");
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            b.append(" enum");
        }
        return b.toString();
    }

    protected void print(final int opcode) {
        pw.print(AbstractVisitor.OPCODES[opcode].toLowerCase());
    }

    protected void print(final Object cst) {
        if (cst instanceof String) {
            StringBuffer buf = new StringBuffer();
            AbstractVisitor.appendString(buf, (String) cst);
            pw.print(buf.toString());
        } else if (cst instanceof Float) {
            Float f = (Float) cst;
            if (f.isNaN() || f.isInfinite()) {
                pw.print("0.0"); // TODO Jasmin bug workaround
            } else {
                pw.print(f);
            }
        } else if (cst instanceof Double) {
            Double d = (Double) cst;
            if (d.isNaN() || d.isInfinite()) {
                pw.print("0.0"); // TODO Jasmin bug workaround
            } else {
                pw.print(d);
            }
        } else {
            pw.print(cst);
        }
    }

    protected void print(final Label l) {
        String name = (String) labelNames.get(l);
        if (name == null) {
            name = "L" + labelNames.size();
            labelNames.put(l, name);
        }
        pw.print(name);
    }

    protected void print(final LabelNode l) {
        print(l.getLabel());
    }

    protected void printAnnotations(final MemberNode n) {
        if (n.visibleAnnotations != null) {
            for (int j = 0; j < n.visibleAnnotations.size(); ++j) {
                printAnnotation((AnnotationNode) n.visibleAnnotations.get(j), 1, -1);
            }
        }
        if (n.invisibleAnnotations != null) {
            for (int j = 0; j < n.invisibleAnnotations.size(); ++j) {
                printAnnotation((AnnotationNode) n.invisibleAnnotations.get(j), 2, -1);
            }
        }
    }

    protected void printAnnotation(final AnnotationNode n, final int visible, final int param) {
        pw.print(".annotation ");
        if (visible > 0) {
            if (param == -1) {
                pw.print(visible == 1 ? "visible " : "invisible ");
            } else {
                pw.print(visible == 1 ? "visibleparam " : "invisibleparam ");
                pw.print(param);
                pw.print(' ');
            }
            pw.print(n.desc);
        }
        pw.println();
        if (n.values != null) {
            for (int i = 0; i < n.values.size(); i += 2) {
                pw.print(n.values.get(i));
                pw.print(' ');
                printAnnotationValue(n.values.get(i + 1));
            }
        }
        pw.println(".end annotation");
    }

    protected void printAnnotationValue(final Object value) {
        if (value instanceof String[]) {
            pw.print("e ");
            pw.print(((String[]) value)[0]);
            pw.print(" = ");
            print(((String[]) value)[1]);
            pw.println();
        } else if (value instanceof AnnotationNode) {
            pw.print("@ ");
            pw.print(((AnnotationNode) value).desc);
            pw.print(" = ");
            printAnnotation((AnnotationNode) value, 0, -1);
        } else if (value instanceof byte[]) {
            pw.print("[B = ");
            byte[] v = (byte[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(v[i]);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof boolean[]) {
            pw.print("[Z = ");
            boolean[] v = (boolean[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(v[i] ? '1' : '0');
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof short[]) {
            pw.print("[S = ");
            short[] v = (short[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(v[i]);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof char[]) {
            pw.print("[C = ");
            char[] v = (char[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(new Integer(v[i]));
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof int[]) {
            pw.print("[I = ");
            int[] v = (int[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(v[i]);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof long[]) {
            pw.print("[J = ");
            long[] v = (long[]) value;
            for (int i = 0; i < v.length; i++) {
                pw.print(v[i]);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof float[]) {
            pw.print("[F = ");
            float[] v = (float[]) value;
            for (int i = 0; i < v.length; i++) {
                print(new Float(v[i]));
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof double[]) {
            pw.print("[D = ");
            double[] v = (double[]) value;
            for (int i = 0; i < v.length; i++) {
                print(new Double(v[i]));
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof List) {
            List l = (List) value;
            if (l.size() > 0) {
                Object o = l.get(0);
                if (o instanceof String[]) {
                    pw.print("[e ");
                    pw.print(((String[]) o)[0]);
                    pw.print(" = ");
                } else if (o instanceof AnnotationNode) {
                    pw.print("[& ");
                    pw.print(((AnnotationNode) o).desc);
                    pw.print(" = ");
                    pw.print("[@ = ");
                } else if (o instanceof String) {
                    pw.print("[s = ");
                } else if (o instanceof Byte) {
                    pw.print("[B = ");
                } else if (o instanceof Boolean) {
                    pw.print("[Z = ");
                } else if (o instanceof Character) {
                    pw.print("[C = ");
                } else if (o instanceof Short) {
                    pw.print("[S = ");
                } else if (o instanceof Type) {
                    pw.print("[c = ");
                } else if (o instanceof Integer) {
                    pw.print("[I = ");
                } else if (o instanceof Float) {
                    pw.print("[F = ");
                } else if (o instanceof Long) {
                    pw.print("[J = ");
                } else if (o instanceof Double) {
                    pw.print("[D = ");
                }
                for (int j = 0; j < l.size(); ++j) {
                    printAnnotationArrayValue(l.get(j));
                    pw.print(' ');
                }
            } else {
                pw.print("; empty array annotation value");
            }
            pw.println();
        } else if (value instanceof String) {
            pw.print("s = ");
            print(value);
            pw.println();
        } else if (value instanceof Byte) {
            pw.print("B = ");
            pw.println(((Byte) value).intValue());
        } else if (value instanceof Boolean) {
            pw.print("Z = ");
            pw.println(((Boolean) value).booleanValue() ? 1 : 0);
        } else if (value instanceof Character) {
            pw.print("C = ");
            pw.println(new Integer(((Character) value).charValue()));
        } else if (value instanceof Short) {
            pw.print("S = ");
            pw.println(((Short) value).intValue());
        } else if (value instanceof Type) {
            pw.print("c = ");
            pw.println(((Type) value).getDescriptor());
        } else if (value instanceof Integer) {
            pw.print("I = ");
            print(value);
            pw.println();
        } else if (value instanceof Float) {
            pw.print("F = ");
            print(value);
            pw.println();
        } else if (value instanceof Long) {
            pw.print("J = ");
            print(value);
            pw.println();
        } else if (value instanceof Double) {
            pw.print("D = ");
            print(value);
            pw.println();
        } else {
            throw new RuntimeException();
        }
    }

    protected void printAnnotationArrayValue(final Object value) {
        if (value instanceof String[]) {
            print(((String[]) value)[1]);
        } else if (value instanceof AnnotationNode) {
            printAnnotation((AnnotationNode) value, 0, -1);
        } else if (value instanceof String) {
            print(value);
        } else if (value instanceof Byte) {
            pw.print(((Byte) value).intValue());
        } else if (value instanceof Boolean) {
            pw.print(((Boolean) value).booleanValue() ? 1 : 0);
        } else if (value instanceof Character) {
            pw.print(new Integer(((Character) value).charValue()));
        } else if (value instanceof Short) {
            pw.print(((Short) value).intValue());
        } else if (value instanceof Type) {
            pw.print(((Type) value).getDescriptor());
        } else {
            print(value);
        }
    }

    protected void printFrameType(final Object type) {
        if (type == Opcodes.TOP) {
            pw.print("Top");
        } else if (type == Opcodes.INTEGER) {
            pw.print("Integer");
        } else if (type == Opcodes.FLOAT) {
            pw.print("Float");
        } else if (type == Opcodes.LONG) {
            pw.print("Long");
        } else if (type == Opcodes.DOUBLE) {
            pw.print("Double");
        } else if (type == Opcodes.NULL) {
            pw.print("Null");
        } else if (type == Opcodes.UNINITIALIZED_THIS) {
            pw.print("UninitializedThis");
        } else if (type instanceof Label) {
            pw.print("Uninitialized ");
            print((Label) type);
        } else {
            pw.print("Object ");
            pw.print(type);
        }
    }
}
