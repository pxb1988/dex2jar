/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.node;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

public class DexCodeNode extends DexCodeVisitor {

    public List<DexStmtNode> stmts = new ArrayList<DexStmtNode>();
    public List<DexStmtNode> tryStmts = null;
    public DexDebugNode debugNode;
    public int totalRegister = -1;

    public DexCodeNode() {
        super();
    }

    public DexCodeNode(DexCodeVisitor visitor) {
        super(visitor);
    }

    public void accept(DexCodeVisitor v) {
        if (tryStmts != null) {
            for (DexStmtNode n : tryStmts) {
                n.accept(v);
            }
        }
        if (debugNode != null) {
            DexDebugVisitor ddv = v.visitDebug();
            if (ddv != null) {
                debugNode.accept(ddv);
                ddv.visitEnd();
            }
        }
        if (totalRegister >= 0) {
            v.visitRegister(this.totalRegister);
        }
        for (DexStmtNode n : stmts) {
            n.accept(v);
        }
    }

    public void accept(DexMethodVisitor v) {
        DexCodeVisitor cv = v.visitCode();
        if (cv != null) {
            accept(cv);
            cv.visitEnd();
        }
    }

    protected void add(DexStmtNode stmt) {
        stmts.add(stmt);
    }

    @Override
    public void visitConstStmt(final Op op, final int ra, final Object value) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitConstStmt(op, ra, value);
            }
        });
    }

    @Override
    public void visitFillArrayDataStmt(final Op op, final int ra, final Object array) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitFillArrayDataStmt(op, ra, array);
            }
        });
    }

    @Override
    public void visitFieldStmt(final Op op, final int a, final int b, final Field field) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitFieldStmt(op, a, b, field);
            }
        });
    }

    @Override
    public void visitFilledNewArrayStmt(final Op op, final int[] args, final String type) {
        add(new FilledNewArrayStmtNode(op, args, type));
    }

    @Override
    public void visitJumpStmt(final Op op, final int a, final int b, final DexLabel label) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitJumpStmt(op, a, b, label);
            }
        });
    }

    @Override
    public void visitLabel(final DexLabel label) {
        add(new DexLabelStmtNode(label));
    }

    @Override
    public void visitMethodStmt(final Op op, final int[] args, final Method method) {
        add(new MethodStmtNode(op, args, method));
    }

    @Override
    public void visitPackedSwitchStmt(final Op op, final int aA, final int first_case, final DexLabel[] labels) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitPackedSwitchStmt(op, aA, first_case, labels);
            }
        });
    }

    @Override
    public void visitRegister(final int total) {
        this.totalRegister = total;
    }

    @Override
    public void visitSparseSwitchStmt(final Op op, final int ra, final int[] cases, final DexLabel[] labels) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitSparseSwitchStmt(op, ra, cases, labels);
            }
        });
    }

    @Override
    public void visitStmt0R(final Op op) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitStmt0R(op);
            }
        });
    }

    @Override
    public void visitStmt1R(final Op op, final int reg) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitStmt1R(op, reg);
            }
        });
    }

    @Override
    public void visitStmt2R(final Op op, final int a, final int b) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitStmt2R(op, a, b);
            }
        });
    }

    @Override
    public void visitStmt2R1N(final Op op, final int distReg, final int srcReg, final int content) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitStmt2R1N(op, distReg, srcReg, content);
            }
        });
    }

    @Override
    public void visitStmt3R(final Op op, final int a, final int b, final int c) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitStmt3R(op, a, b, c);
            }
        });
    }

    @Override
    public void visitTryCatch(final DexLabel start, final DexLabel end, final DexLabel[] handler, final String[] type) {
        if (tryStmts == null) {
            tryStmts = new ArrayList<>(3);
        }
        tryStmts.add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitTryCatch(start, end, handler, type);
            }
        });
    }

    @Override
    public void visitTypeStmt(final Op op, final int a, final int b, final String type) {
        add(new DexStmtNode() {

            @Override
            public void accept(DexCodeVisitor cv) {
                cv.visitTypeStmt(op, a, b, type);
            }
        });
    }

    public static class DexLabelStmtNode extends DexStmtNode {
        public DexLabel label;

        public DexLabelStmtNode(DexLabel label) {
            this.label = label;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
            cv.visitLabel(label);
        }
    }

    public static abstract class DexStmtNode {
        public abstract void accept(DexCodeVisitor cv);
    }

    public static class MethodStmtNode extends DexStmtNode {

        public final Op op;
        public final int[] args;
        public final Method method;

        public MethodStmtNode(Op op, int[] args, Method method) {
            this.op = op;
            this.args = args;
            this.method = method;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
            cv.visitMethodStmt(op, args, method);
        }
    }

    @Override
    public DexDebugVisitor visitDebug() {
        DexDebugNode dexDebugNode = new DexDebugNode();
        this.debugNode = dexDebugNode;
        return dexDebugNode;
    }

    public static class FilledNewArrayStmtNode extends DexStmtNode {

        public final Op op;
        public final int[] args;
        public final String type;

        public FilledNewArrayStmtNode(Op op, int[] args, String type) {
            this.op = op;
            this.args = args;
            this.type = type;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
            cv.visitFilledNewArrayStmt(op, args, type);
        }
    }
}
