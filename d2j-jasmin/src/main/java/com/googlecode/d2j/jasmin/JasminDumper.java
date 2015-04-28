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
package com.googlecode.d2j.jasmin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import java.io.PrintWriter;
import java.util.*;

/**
 * <b>get from asm example</b>
 * <p>
 * Disassembled view of the classes in Jasmin assembler format.
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
public class JasminDumper implements Opcodes {
    private static Set<String> ACCESS_KWS = new HashSet<String>(Arrays
            .asList("abstract", "private", "protected", "public", "enum", "final", "interface", "static", "strictfp", "native", "super"));

    public JasminDumper(PrintWriter pw) {
        this.pw = pw;
    }

    /**
     * The print writer to be used to print the class.
     */
    protected PrintWriter pw;

    /**
     * The label names. This map associate String values to Label keys.
     */
    protected final Map<Label, String> labelNames = new HashMap<>();

    static void printIdAfterAccess(PrintWriter out, String id) {
        if (ACCESS_KWS.contains(id)) {
            out.print("\"");
            out.print(id);
            out.print("\"");
        } else {
            out.print(id);
        }
    }

    public void dump(ClassNode cn) {
        labelNames.clear();
        pw.print(".bytecode ");
        pw.print(cn.version & 0xFFFF);
        pw.print('.');
        pw.println(cn.version >>> 16);
        println(".source ", cn.sourceFile);
        pw.print(".class");
        pw.print(access_clz(cn.access));
        pw.print(' ');
        printIdAfterAccess(pw, cn.name);
        pw.println();
        if (cn.superName != null) {
            pw.print(".super ");
            printIdAfterAccess(pw, cn.superName);
            pw.println();
        }
        for (String itf : cn.interfaces) {
            pw.print(".implements ");
            printIdAfterAccess(pw, itf);
            pw.println();
        }
        if (cn.signature != null) {
            println(".signature ", '"' + cn.signature + '"');
        }
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
        if (cn.visibleAnnotations != null) {
            for (AnnotationNode an : cn.visibleAnnotations) {
                printAnnotation(an, 1, -1);
            }
        }
        if (cn.invisibleAnnotations != null) {
            for (AnnotationNode an : cn.invisibleAnnotations) {
                printAnnotation(an, 2, -1);
            }
        }

        println(".debug ", cn.sourceDebug == null ? null : '"' + cn.sourceDebug + '"');

        for (InnerClassNode in : cn.innerClasses) {
            pw.print(".inner class");
            pw.print(access_clz(in.access & (~Opcodes.ACC_SUPER)));
            if (in.innerName != null) {
                pw.print(' ');
                printIdAfterAccess(pw, in.innerName);
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

        for (FieldNode fn : cn.fields) {
            boolean annotations = false;
            if (fn.visibleAnnotations != null && fn.visibleAnnotations.size() > 0) {
                annotations = true;
            }
            if (fn.invisibleAnnotations != null && fn.invisibleAnnotations.size() > 0) {
                annotations = true;
            }
            boolean deprecated = (fn.access & Opcodes.ACC_DEPRECATED) != 0;
            pw.print("\n.field");
            pw.print(access_fld(fn.access));
            pw.print(' ');
            printIdAfterAccess(pw,fn.name);
            pw.print(' ');
            pw.print(fn.desc);
            if (fn.value instanceof String) {
                StringBuffer buf = new StringBuffer();
                Printer.appendString(buf, (String) fn.value);
                pw.print(" = ");
                pw.print(buf.toString());
            } else if (fn.value != null) {
                pw.print(" = ");
                print(fn.value);
            }
            pw.println();
            if (fn.signature != null) {
                pw.print(".signature \"");
                pw.print(fn.signature);
                pw.println("\"");
            }
            if (deprecated) {
                pw.println(".deprecated");
            }
            if (fn.visibleAnnotations != null) {
                for (AnnotationNode an : fn.visibleAnnotations) {
                    printAnnotation(an, 1, -1);
                }
            }
            if (fn.invisibleAnnotations != null) {
                for (AnnotationNode an : fn.invisibleAnnotations) {
                    printAnnotation(an, 2, -1);
                }
            }
            if (fn.signature != null || deprecated || annotations) {
                pw.println(".end field");
            }
        }

        for (MethodNode mn : cn.methods) {
            pw.print("\n.method");
            pw.print(access_mtd(mn.access));
            pw.print(' ');
            printIdAfterAccess(pw, mn.name);
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
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    printAnnotation(an, 1, -1);
                }
            }
            if (mn.invisibleAnnotations != null) {
                for (AnnotationNode an : mn.invisibleAnnotations) {
                    printAnnotation(an, 2, -1);
                }
            }
            if (mn.visibleParameterAnnotations != null) {
                for (int j = 0; j < mn.visibleParameterAnnotations.length; ++j) {
                    List<AnnotationNode> pas = mn.visibleParameterAnnotations[j];
                    if (pas != null) {
                        for (AnnotationNode an : pas) {
                            printAnnotation(an, 1, j + 1);
                        }
                    }
                }
            }
            if (mn.invisibleParameterAnnotations != null) {
                for (int j = 0; j < mn.invisibleParameterAnnotations.length; ++j) {
                    List<AnnotationNode> pas = mn.invisibleParameterAnnotations[j];
                    if (pas != null) {
                        for (AnnotationNode an : pas) {
                            printAnnotation(an, 2, j + 1);
                        }
                    }
                }
            }
            for (String ex : mn.exceptions) {
                println(".throws ", ex);
            }
            if ((mn.access & Opcodes.ACC_DEPRECATED) != 0) {
                pw.println(".deprecated");
            }
            if (mn.instructions != null && mn.instructions.size() > 0) {
                labelNames.clear();
                if (mn.tryCatchBlocks != null) {
                    for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                        pw.print(".catch ");
                        pw.print(tcb.type == null ? "all" : "all".equals(tcb.type) ? "\\u0097ll" : tcb.type);
                        pw.print(" from ");
                        print(tcb.start);
                        pw.print(" to ");
                        print(tcb.end);
                        pw.print(" using ");
                        print(tcb.handler);
                        pw.println();
                    }
                }
                for (int j = 0; j < mn.instructions.size(); ++j) {
                    AbstractInsnNode in = mn.instructions.get(j);
                    if (in.getType() != AbstractInsnNode.LINE && in.getType() != AbstractInsnNode.FRAME) {
                       if(in.getType()==AbstractInsnNode.LABEL){
                           pw.print("  ");
                       }else {
                           pw.print("    ");
                       }
                    }
                    in.accept(new MethodVisitor(ASM4) {

                        @Override
                        public void visitInsn(int opcode) {
                            print(opcode);
                            pw.println();
                        }

                        @Override
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

                        @Override
                        public void visitVarInsn(int opcode, int var) {
                            print(opcode);
                            pw.print(' ');
                            pw.println(var);
                        }

                        @Override
                        public void visitTypeInsn(int opcode, String type) {
                            print(opcode);
                            pw.print(' ');
                            pw.println(type);
                        }

                        @Override
                        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                            print(opcode);
                            pw.print(' ');
                            pw.print(owner);
                            pw.print('/');
                            pw.print(name);
                            pw.print(' ');
                            pw.println(desc);
                        }

                        @Override
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

                        @Override
                        public void visitJumpInsn(int opcode, Label label) {
                            print(opcode);
                            pw.print(' ');
                            print(label);
                            pw.println();
                        }

                        @Override
                        public void visitLabel(Label label) {
                            print(label);
                            pw.println(':');
                        }

                        @Override
                        public void visitLdcInsn(Object cst) {

                            if (cst instanceof Integer || cst instanceof Float) {
                                pw.print("ldc_w ");
                                print(cst);
                            } else if (cst instanceof Long || cst instanceof Double) {
                                pw.print("ldc2_w ");
                                print(cst);
                            } else {
                                pw.print("ldc ");
                                if (cst instanceof Type) {
                                    pw.print(((Type) cst).getInternalName());
                                } else {
                                    print(cst);
                                }
                            }
                            pw.println();

                        }

                        @Override
                        public void visitIincInsn(int var, int increment) {
                            pw.print("iinc ");
                            pw.print(var);
                            pw.print(' ');
                            pw.println(increment);
                        }

                        @Override
                        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
                            pw.print("tableswitch ");
                            pw.println(min);
                            for (Label label : labels) {
                                pw.print("      ");
                                print(label);
                                pw.println();
                            }
                            pw.print("      default : ");
                            print(dflt);
                            pw.println();
                        }

                        @Override
                        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                            pw.println("lookupswitch");
                            for (int i = 0; i < keys.length; ++i) {
                                pw.print("      ");
                                pw.print(keys[i]);
                                pw.print(" : ");
                                print(labels[i]);
                                pw.println();
                            }
                            pw.print("      default : ");
                            print(dflt);
                            pw.println();
                        }

                        @Override
                        public void visitMultiANewArrayInsn(String desc, int dims) {
                            pw.print("multianewarray ");
                            pw.print(desc);
                            pw.print(' ');
                            pw.println(dims);
                        }

                        @Override
                        public void visitLineNumber(int line, Label start) {
                            pw.print(".line ");
                            pw.println(line);
                        }
                    });
                }
                if (mn.localVariables != null) {
                    for (LocalVariableNode lv : mn.localVariables) {
                        pw.print("  .var ");
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
                }
                println("  .limit locals ", Integer.toString(mn.maxLocals));
                println("  .limit stack ", Integer.toString(mn.maxStack));
            }
            pw.println(".end method");
        }
    }

    protected void println(final String directive, final String arg) {
        if (arg != null) {
            pw.print(directive);
            pw.println(arg);
        }
    }
    protected String access_clz(final int access) {
        StringBuilder b = new StringBuilder();
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            b.append(" public");
        }
        if ((access & Opcodes.ACC_PRIVATE) != 0) {
            b.append(" private");
        }
        if ((access & Opcodes.ACC_PROTECTED) != 0) {
            b.append(" protected");
        }
        if ((access & Opcodes.ACC_FINAL) != 0) {
            b.append(" final");
        }
        if ((access & Opcodes.ACC_SUPER) != 0) {
            b.append(" super");
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            b.append(" abstract");
        }
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            b.append(" interface");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            b.append(" synthetic");
        }
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            b.append(" annotation");
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            b.append(" enum");
        }
        return b.toString();
    }
    protected String access_fld(final int access) {
        StringBuilder b = new StringBuilder();
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
        if ((access & Opcodes.ACC_VOLATILE) != 0) {
            b.append(" volatile");
        }
        if ((access & Opcodes.ACC_TRANSIENT) != 0) {
            b.append(" transient");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            b.append(" synthetic");
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            b.append(" enum");
        }
        return b.toString();
    }
    protected String access_mtd(final int access) {
        StringBuilder b = new StringBuilder();
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
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            b.append(" bridge");
        }
        if ((access & Opcodes.ACC_VARARGS) != 0) {
            b.append(" varargs");
        }
        if ((access & Opcodes.ACC_NATIVE) != 0) {
            b.append(" native");
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            b.append(" abstract");
        }
        if ((access & Opcodes.ACC_STRICT) != 0) {
            b.append(" strict");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            b.append(" synthetic");
        }
        return b.toString();
    }

    protected void print(final int opcode) {
        pw.print(Printer.OPCODES[opcode].toLowerCase());
    }

    protected void print(final Object cst) {
        if (cst instanceof String) {
            StringBuffer buf = new StringBuffer();
            Printer.appendString(buf, (String) cst);
            pw.print(buf.toString());
        } else if (cst instanceof Float) {
            Float f = (Float) cst;
            if (!f.isNaN() && !f.isInfinite()) {
                pw.print(cst + "F");
            } else if (f.isNaN()) {
                pw.print("floatnan");
            } else {
                double v = f;
                if ((v == Float.POSITIVE_INFINITY)) {
                    pw.print("+floatinfinity");
                } else {
                    pw.print("-floatinfinity");
                }
            }
        } else if (cst instanceof Double) {
            Double d = (Double) cst;

            if (!d.isNaN() && !d.isInfinite()) {
                pw.print(cst + "D");
            } else if (d.isNaN()) {
                pw.print("doublenan");
            } else {
                double v = d;
                if ((v == Double.POSITIVE_INFINITY)) {
                    pw.print("+doubleinfinity");
                } else {
                    pw.print("-doubleinfinity");
                }
            }
        } else if (cst instanceof Long) {
            pw.print(cst + "L");
        } else {
            pw.print(cst);
        }
    }

    protected void print(final Label l) {
        String name = labelNames.get(l);
        if (name == null) {
            name = "L" + labelNames.size();
            labelNames.put(l, name);
        }
        pw.print(name);
    }

    protected void print(final LabelNode l) {
        print(l.getLabel());
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
            pw.print(((String[]) value)[1]);
            pw.println();
        } else if (value instanceof AnnotationNode) {
            pw.print("@ ");
            pw.print(((AnnotationNode) value).desc);
            pw.print(" = ");
            printAnnotation((AnnotationNode) value, 0, -1);
        } else if (value instanceof byte[]) {
            pw.print("[B = ");
            byte[] v = (byte[]) value;
            for (byte element : v) {
                pw.print(element);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof boolean[]) {
            pw.print("[Z = ");
            boolean[] v = (boolean[]) value;
            for (boolean element : v) {
                pw.print(element ? '1' : '0');
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof short[]) {
            pw.print("[S = ");
            short[] v = (short[]) value;
            for (short element : v) {
                pw.print(element);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof char[]) {
            pw.print("[C = ");
            char[] v = (char[]) value;
            for (char element : v) {
                pw.print(new Integer(element));
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof int[]) {
            pw.print("[I = ");
            int[] v = (int[]) value;
            for (int element : v) {
                pw.print(element);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof long[]) {
            pw.print("[J = ");
            long[] v = (long[]) value;
            for (long element : v) {
                pw.print(element);
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof float[]) {
            pw.print("[F = ");
            float[] v = (float[]) value;
            for (float element : v) {
                print(new Float(element));
                pw.print(' ');
            }
            pw.println();
        } else if (value instanceof double[]) {
            pw.print("[D = ");
            double[] v = (double[]) value;
            for (double element : v) {
                print(new Double(element));
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
                for (Object aL : l) {
                    printAnnotationArrayValue(aL);
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
