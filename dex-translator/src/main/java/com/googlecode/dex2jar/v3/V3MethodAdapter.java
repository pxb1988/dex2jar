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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.asm.LdcOptimizeAdapter;
import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3MethodAdapter implements DexMethodVisitor, Opcodes {
    private static Transformer endremove = new EndRemover();
    private static final Logger log = LoggerFactory.getLogger(V3MethodAdapter.class);
    private static Transformer[] tses = new Transformer[] { new LocalSplit(), new LocalRemove(), new LocalType(),
            new LocalCurrect() };
    static {
        log.debug("InsnList.check=false");
        // Optimize Tree Analyzer
        InsnList.check = false;
    }

    /**
     * index LabelStmt for debug
     * 
     * @param list
     */
    public static void indexLabelStmt4Debug(StmtList list) {
        int labelIndex = 0;
        for (Stmt stmt : list) {
            if (stmt.st == ST.LABEL) {
                ((LabelStmt) stmt).displayName = "L" + labelIndex++;
            }
        }
    }

    final protected Method method;

    final protected MethodNode methodNode = new MethodNode();

    final protected int accessFlags;
    Map<Method, Exception> exceptions;

    /**
     * @param accessFlags
     * @param method
     */
    public V3MethodAdapter(int accessFlags, Method method, Map<Method, Exception> exceptions) {
        super();
        this.method = method;
        this.accessFlags = accessFlags;
        this.exceptions = exceptions;
    }

    Annotation throwsAnnotation;
    Annotation signatureAnnotation;
    IrMethod irMethod;

    private void build() {
        List<String> exceptions = new ArrayList<String>();
        String signature = null;
        if (this.throwsAnnotation != null) {
            for (Item item : this.throwsAnnotation.items) {
                if (item.name.equals("value")) {
                    Annotation values = (Annotation) item.value;
                    for (Item i : values.items) {
                        exceptions.add(((Type) i.value).getInternalName());
                    }
                }
            }
        }
        if (this.signatureAnnotation != null) {
            for (Item item : this.signatureAnnotation.items) {
                if (item.name.equals("value")) {
                    Annotation values = (Annotation) item.value;
                    StringBuilder sb = new StringBuilder();
                    for (Item i : values.items) {
                        sb.append(i.value.toString());
                    }
                    signature = sb.toString();
                }
            }
        }

        MethodNode methodNode = this.methodNode;
        methodNode.access = this.accessFlags;
        methodNode.name = method.getName();
        methodNode.desc = method.getDesc();
        methodNode.signature = signature;
        methodNode.exceptions = exceptions;
        methodNode.tryCatchBlocks = new ArrayList<Object>();
    }

    void dump(MethodNode methodNode) {
        TraceMethodVisitor tmv = new TraceMethodVisitor();
        methodNode.instructions.accept(tmv);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object o : tmv.text) {
            sb.append(i++).append(o);
        }
        System.out.println(sb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java .lang .String, boolean)
     */
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
        if (name.equals("Ldalvik/annotation/Signature;")) {
            this.signatureAnnotation = new Annotation(name, visible);
            return new V3AnnAdapter(this.signatureAnnotation);
        } else if (name.equals("Ldalvik/annotation/Throws;")) {
            this.throwsAnnotation = new Annotation(name, visible);
            return new V3AnnAdapter(this.throwsAnnotation);
        } else {
            AnnotationVisitor av = methodNode.visitAnnotation(name, visible);
            if (av != null) {
                return new Dex2AsmAnnotationAdapter(av);
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {
        IrMethod irMethod = new IrMethod();
        irMethod.access = accessFlags;
        irMethod.args = Type.getArgumentTypes(method.getDesc());
        irMethod.ret = Type.getType(method.getReturnType());
        irMethod.owner = Type.getType(method.getOwner());
        irMethod.name = method.getName();
        this.irMethod = irMethod;
        return new V3CodeAdapter(this.accessFlags, irMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    public void visitEnd() {
        build();
        if (irMethod != null) {
            try {
                if (irMethod.stmts.getSize() > 1) {
                    // indexLabelStmt4Debug(irMethod.stmts);
                    endremove.transform(irMethod);

                    for (Transformer ts : tses) {
                        ts.transform(irMethod);
                    }
                }
                new IrMethod2AsmMethod().convert(irMethod, new LdcOptimizeAdapter(methodNode));
            } catch (Exception e) {
                if (this.exceptions == null) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
                this.exceptions.put(method, e);// record the exception

                // replace the generated code with
                // 'return new RuntimeException("Generated by Dex2jar, and Some Exception Caught : xxxxxxxxxxxxx");'
                StringWriter s = new StringWriter();
                e.printStackTrace(new PrintWriter(s));
                String msg = s.toString();
                methodNode.instructions.clear();
                methodNode.tryCatchBlocks.clear();
                irMethod.traps.clear();
                irMethod.stmts.clear();
                irMethod.stmts.add(Stmts.nThrow(Exprs.nInvokeNew(
                        new Value[] { Constant.nString("Generated by Dex2jar, and Some Exception Caught :" + msg), },
                        new Type[] { Type.getType(String.class) }, Type.getType(RuntimeException.class))));
                new IrMethod2AsmMethod().convert(irMethod, methodNode);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParameterAnnotation (int)
     */
    public DexAnnotationAble visitParameterAnnotation(final int index) {
        return new DexAnnotationAble() {
            public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
                AnnotationVisitor av = methodNode.visitParameterAnnotation(index, name, visible);
                if (av != null) {
                    return new Dex2AsmAnnotationAdapter(av);
                }
                return null;
            }
        };
    }

}
