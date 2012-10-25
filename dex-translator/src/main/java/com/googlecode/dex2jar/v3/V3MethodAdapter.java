/*
 * Copyright (c) 2009-2012 Panxiaobo
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.asm.LdcOptimizeAdapter;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.ArrayNullPointerTransformer;
import com.googlecode.dex2jar.ir.ts.EndRemover;
import com.googlecode.dex2jar.ir.ts.ExceptionHandlerCurrectTransformer;
import com.googlecode.dex2jar.ir.ts.FixVar;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.TopologicalSort;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.ir.ts.ZeroTransformer;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class V3MethodAdapter implements DexMethodVisitor, Opcodes {
    protected static Transformer topologicalSort = new TopologicalSort();
    private static final Logger log = Logger.getLogger(V3MethodAdapter.class.getName());
    protected static Transformer[] tses = new Transformer[] { new EndRemover(),
            new ExceptionHandlerCurrectTransformer(), new ZeroTransformer(), new ArrayNullPointerTransformer(),
            new FixVar(), new LocalSplit(), new LocalRemove(), new LocalType() };
    static {
        log.log(Level.CONFIG, "InsnList.check=false");
        // Optimize Tree Analyzer
        InsnList.check = false;
    }

    /**
     * index LabelStmt for debug
     * 
     * @param list
     */
    protected static void indexLabelStmt4Debug(StmtList list) {
        int labelIndex = 0;
        for (Stmt stmt : list) {
            if (stmt.st == ST.LABEL) {
                ((LabelStmt) stmt).displayName = "L" + labelIndex++;
            }
        }
    }

    protected int accessFlags;
    protected DexExceptionHandler exceptionHandler;
    protected IrMethod irMethod;
    final protected Method method;
    final protected MethodNode methodNode = new MethodNode();
    protected Annotation signatureAnnotation;
    protected Annotation throwsAnnotation;
    protected int config;

    public V3MethodAdapter(int accessFlags, Method method, DexExceptionHandler exceptionHandler) {
        this(accessFlags, method, exceptionHandler, 0);
    }

    public V3MethodAdapter(int accessFlags, Method method, DexExceptionHandler exceptionHandler, int config) {
        super();
        this.method = method;
        // clear ACC_DECLARED_SYNCHRONIZED and ACC_CONSTRUCTOR from method flags
        final int cleanFlag = ~((DexOpcodes.ACC_DECLARED_SYNCHRONIZED | DexOpcodes.ACC_CONSTRUCTOR));
        this.accessFlags = accessFlags & cleanFlag;
        this.exceptionHandler = exceptionHandler;
        this.config = config;
        // issue 88, the desc must set before visitParameterAnnotation
        methodNode.desc = method.getDesc();
    }

    protected void build() {
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
        methodNode.localVariables = new ArrayList<Object>();
    }

    protected void debug_dump(MethodNode methodNode) {
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
    @Override
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
    @Override
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
    @Override
    public void visitEnd() {
        build();
        if (irMethod != null) {
            try {
                if (irMethod.stmts.getSize() > 1) {
                    if (V3.DEBUG) {
                        indexLabelStmt4Debug(irMethod.stmts);
                        for (Transformer ts : tses) {
                            ts.transform(irMethod);
                            indexLabelStmt4Debug(irMethod.stmts);
                        }
                        if (0 != (config & V3.TOPOLOGICAL_SORT)) {
                            topologicalSort.transform(irMethod);
                        }
                        indexLabelStmt4Debug(irMethod.stmts);
                    } else {
                        for (Transformer ts : tses) {
                            ts.transform(irMethod);
                        }
                        if (0 != (config & V3.TOPOLOGICAL_SORT)) {
                            topologicalSort.transform(irMethod);
                        }
                    }

                }
                if (0 != (config & V3.PRINT_IR)) {
                    indexLabelStmt4Debug(irMethod.stmts);
                    System.out.println(irMethod);
                }
                new IrMethod2AsmMethod(config).convert(irMethod, new LdcOptimizeAdapter(methodNode));
            } catch (Exception e) {
                if (this.exceptionHandler == null) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                } else {
                    this.exceptionHandler.handleMethodTranslateException(method, irMethod, methodNode, e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParameterAnnotation (int)
     */
    @Override
    public DexAnnotationAble visitParameterAnnotation(final int index) {
        return new DexAnnotationAble() {
            @Override
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
