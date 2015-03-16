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
package com.googlecode.d2j.smali;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class SmaliCodeVisitor extends DexCodeNode {

    public SmaliCodeVisitor(DexCodeVisitor visitor) {
        super(visitor);
    }

    @Override
    public void visitConstStmt(Op op, int ra, Object value) {
        switch (op) {
        case CONST_WIDE_16: {
            if(value instanceof Integer) {
                short v = ((Number) value).shortValue();
                super.visitConstStmt(op, ra, (long) v);
            } else {
                super.visitConstStmt(op, ra, value);
            }
        }
            break;
        case CONST_WIDE_HIGH16: {
            if(value instanceof Integer) {
                short v = ((Number) value).shortValue();
                super.visitConstStmt(op, ra, ((long) v) << 48);
            } else {
                super.visitConstStmt(op, ra, value);
            }
        }
            break;
        case CONST_WIDE_32: {
            if(value instanceof Integer) {
                int v = ((Number) value).intValue();
                super.visitConstStmt(op, ra, (long) v);
            } else {
                super.visitConstStmt(op, ra, value);
            }
        }
            break;
        case CONST_HIGH16: {
            int v = ((Number) value).intValue();
            if(0 != (v & 0xFFff0000)){
                super.visitConstStmt(op, ra, v);
            } else {
                super.visitConstStmt(op, ra, v << 16);
            }
        }
            break;
        default:
            super.visitConstStmt(op, ra, value);
            break;
        }
    }

    public static class ArrayDataStmt extends DexStmtNode {
        int length;
        byte[] objs;

        public ArrayDataStmt(int length, byte[] obj) {
            super(null);
            this.length = length;
            this.objs = obj;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
        }

    }

    public static class PackedSwitchStmt extends DexStmtNode {
        int firstCase;
        DexLabel[] labels;

        public PackedSwitchStmt(int reg, DexLabel[] labels) {
            super(null);
            this.firstCase = reg;
            this.labels = labels;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
        }
    }

    public static class SparseSwitchStmt extends DexStmtNode {
        int[] cases;
        DexLabel labels[];

        public SparseSwitchStmt(int[] cases, DexLabel[] labels) {
            super(null);
            this.cases = cases;
            this.labels = labels;
        }

        @Override
        public void accept(DexCodeVisitor cv) {
        }
    }

    private List<DexStmtNode> needCareStmts = new ArrayList<DexStmtNode>();

    @Override
    public void visitEnd() {
        if (super.visitor != null) {
            super.accept(super.visitor);
        }
        needCareStmts = null;
        stmts = null;
        tryStmts = null;
        super.visitEnd();
    }

    private void addCare(DexStmtNode stmt) {
        needCareStmts.add(stmt);
        super.add(stmt);
    }

    /* package */void dArrayData(int length, byte[] obj) {
        addCare(new ArrayDataStmt(length, obj));
    }

    void dPackedSwitch(int first, DexLabel[] labels) {
        addCare(new PackedSwitchStmt(first, labels));
    }

    /* package */void dSparseSwitch(int[] cases, DexLabel[] labels) {
        addCare(new SparseSwitchStmt(cases, labels));
    }

    int findLabelIndex(DexLabel label) {
        int labelIndex = -1;
        for (int i = 0; i < needCareStmts.size(); i++) {
            DexStmtNode s = needCareStmts.get(i);
            if (s instanceof DexLabelStmtNode) {
                DexLabelStmtNode ss = (DexLabelStmtNode) s;
                if (ss.label == label) {
                    labelIndex = i;
                }
            }
        }
        return labelIndex;
    }

    /* package */void visitF31tStmt(final Op op, final int reg, final DexLabel label) {
        add(new DexStmtNode(op) {

            @Override
            public void accept(DexCodeVisitor cv) {
                int labelIndex = findLabelIndex(label);
                if (labelIndex < 0 || labelIndex >= needCareStmts.size()) {
                    throw new RuntimeException("can't find label for " + op + " " + label);
                }

                switch (op) {
                case PACKED_SWITCH:
                    PackedSwitchStmt packedSwitchStmt = (PackedSwitchStmt) needCareStmts.get(labelIndex + 1);
                    cv.visitPackedSwitchStmt(op, reg, packedSwitchStmt.firstCase, packedSwitchStmt.labels);
                    break;
                case SPARSE_SWITCH:
                    SparseSwitchStmt sparseSwitchStmt = (SparseSwitchStmt) needCareStmts.get(labelIndex + 1);
                    cv.visitSparseSwitchStmt(op, reg, sparseSwitchStmt.cases, sparseSwitchStmt.labels);
                    break;
                case FILL_ARRAY_DATA:
                    ArrayDataStmt arrayDataStmt = (ArrayDataStmt) needCareStmts.get(labelIndex + 1);
                    Object v;
                    byte[] vs = arrayDataStmt.objs;
                    switch (arrayDataStmt.length) {
                    case 1: {
                        v = vs;
                    }
                        break;
                    case 2: {
                        short[] vs1 = new short[vs.length / 2];
                        for (int i = 0; i < vs1.length; i++) {
                            vs1[i] = (short) ((vs[i * 2] & 0xFF) | ((vs[i * 2 + 1] & 0xFF) << 8));
                        }
                        v = vs1;
                    }
                        break;
                    case 4: {
                        int[] vs1 = new int[vs.length / 4];
                        for (int i = 0; i < vs1.length; i++) {
                            int base = i * 4;
                            vs1[i] = (vs[base + 0] & 0xFF) | ((vs[base + 1] & 0xFF) << 8)
                                    | ((vs[base + 2] & 0xFF) << 16) | ((vs[base + 3] & 0xFF) << 24);
                        }
                        v = vs1;
                    }
                        break;
                    case 8: {
                        long[] vs1 = new long[vs.length / 8];
                        for (int i = 0; i < vs1.length; i++) {
                            int base = i * 8;
                            int a = ((vs[base + 0] & 0xFF) << 0) | ((vs[base + 1] & 0xFF) << 8)
                                    | ((vs[base + 2] & 0xFF) << 16) | ((vs[base + 3] & 0xFF) << 24);
                            int b = ((vs[base + 4] & 0xFF) << 0) | ((vs[base + 5] & 0xFF) << 8)
                                    | ((vs[base + 6] & 0xFF) << 16) | ((vs[base + 7] & 0xFF) << 24);
                            vs1[i] = (((long) b) << 32) | a;
                        }
                        v = vs1;
                    }
                        break;
                    default:
                        throw new RuntimeException();
                    }
                    cv.visitFillArrayDataStmt(Op.FILL_ARRAY_DATA, reg, v);
                    break;
                default:
                    throw new RuntimeException();
                }
            }
        });
    }

    @Override
    public void visitLabel(final DexLabel label) {
        addCare(new DexLabelStmtNode(label));
    }
}
