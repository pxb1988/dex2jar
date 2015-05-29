/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
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

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexDebugNode;
import com.googlecode.d2j.reader.InstructionFormat;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.util.Out;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;

import java.util.*;

/*package*/class BaksmaliCodeDumper extends DexCodeVisitor {
    private boolean useParameterRegisters;
    private boolean useLocals;
    private int nextLabelNumber;
    private Out out;
    final int startParamR;
    final Set<DexLabel> usedLabel;
    final Map<DexLabel, List<DexDebugNode.DexDebugOpNode>> debugLabelMap;

    public BaksmaliCodeDumper(Out out, boolean useParameterRegisters, boolean useLocals, int nextLabelNumber,
            int startParamR, Set<DexLabel> usedLabel, Map<DexLabel, List<DexDebugNode.DexDebugOpNode>> debugLabelMap) {
        super();
        this.out = out;
        this.useParameterRegisters = useParameterRegisters;
        this.useLocals = useLocals;
        this.nextLabelNumber = nextLabelNumber;
        this.startParamR = startParamR;
        this.usedLabel = usedLabel;
        this.debugLabelMap = debugLabelMap;
    }

    class PackedSwitchStmt {
        int first_case;

        DexLabel[] labels;

        public PackedSwitchStmt(int first_case, DexLabel[] labels) {
            super();
            this.first_case = first_case;
            this.labels = labels;
        }
    }

    class SparseSwitchStmt {
        int[] cases;

        DexLabel[] labels;

        public SparseSwitchStmt(int[] cases, DexLabel[] labels) {
            super();
            this.cases = cases;
            this.labels = labels;
        }
    }

    List<Map.Entry<DexLabel, Object>> appendLast = new ArrayList<>();

    String reg(int rdx) {
        if (useParameterRegisters && rdx >= this.startParamR) {
            return "p" + (rdx - this.startParamR);
        }
        return "v" + rdx;
    }

    @Override
    public void visitFillArrayDataStmt(Op op, int ra, Object array) {
        DexLabel dx = new DexLabel();
        dx.displayName = "L" + nextLabelNumber++;
        usedLabel.add(dx);
        out.s("%s %s, %s", op.displayName, reg(ra), xLabel(dx));
        appendLast.add(new AbstractMap.SimpleEntry<DexLabel, Object>(dx, array));
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void visitConstStmt(Op op, int ra, Object value) {
        switch (op) {
        case CONST_WIDE_16: {
            Long v = (Long) value;
            value = (int) v.shortValue();
            break;
        }
        case CONST_WIDE_HIGH16: {
            Long v = (Long) value;
            value = (int) ((short) (v >> 48));
            break;
        }
        case CONST_WIDE_32: {
            Long v = (Long) value;
            value = (int) v.intValue();
            break;
        }
        case CONST_HIGH16: {
            Integer v = (Integer) value;
            value = (int) v.intValue() >> 16;
            break;
        }
        }
        out.s("%s %s, %s", op.displayName, reg(ra), BaksmaliDumper.escapeValue(value));
        super.visitConstStmt(op, ra, value);
    }

    @Override
    public void visitEnd() {
        for (Map.Entry<DexLabel, Object> e : this.appendLast) {
            visitLabel(e.getKey());
            Object v = e.getValue();
            if (v instanceof SparseSwitchStmt) {
                SparseSwitchStmt ss = (SparseSwitchStmt) v;
                out.s(".sparse-switch");
                out.push();
                for (int i = 0; i < ss.cases.length; i++) {
                    out.s("%d -> %s", ss.cases[i], xLabel(ss.labels[i]));
                }
                out.pop();
                out.s(".end sparse-switch");
            } else if (v instanceof PackedSwitchStmt) {
                PackedSwitchStmt ps = (PackedSwitchStmt) v;
                out.s(".packed-switch %d", ps.first_case);
                out.push();
                for (DexLabel label : ps.labels) {
                    out.s(xLabel(label));
                }
                out.pop();
                out.s(".end packed-switch");
            } else {
                Object array = e.getValue();
                if (array instanceof byte[]) {
                    out.s(".array-data 1");
                    out.push();
                    byte[] vs = (byte[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        out.s(BaksmaliDumper.escapeValue(vs[i]));
                    }
                    out.pop();
                    out.s(".end array-data");
                } else if (array instanceof short[]) {
                    out.s(".array-data 2");
                    out.push();
                    short[] vs = (short[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        short a = vs[i];
                        out.s("%s %s", BaksmaliDumper.escapeValue((byte) (a & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 8))));
                    }
                    out.pop();
                    out.s(".end array-data");
                } else if (array instanceof int[]) {
                    out.s(".array-data 4");
                    out.push();
                    int[] vs = (int[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        int a = vs[i];
                        out.s("%s %s %s %s", BaksmaliDumper.escapeValue((byte) (a & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 24))));
                    }
                    out.pop();
                    out.s(".end array-data");
                } else if (array instanceof float[]) {
                    out.s(".array-data 4");
                    out.push();
                    float[] vs = (float[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        int a = Float.floatToIntBits(vs[i]);
                        out.s("%s %s %s %s", BaksmaliDumper.escapeValue((byte) (a & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 24))));
                    }
                    out.pop();
                    out.s(".end array-data");
                } else if (array instanceof long[]) {
                    out.s(".array-data 8");
                    out.push();
                    long[] vs = (long[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        long ttt = vs[i];
                        int a = (int) ttt;
                        int b = (int) (ttt >>> 32);
                        out.s("%s %s %s %s %s %s %s %s", BaksmaliDumper.escapeValue((byte) (a & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 24))),
                                BaksmaliDumper.escapeValue((byte) (b & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 24))));
                    }
                    out.pop();
                    out.s(".end array-data");
                } else if (array instanceof double[]) {
                    out.s(".array-data 8");
                    out.push();
                    double[] vs = (double[]) array;
                    for (int i = 0; i < vs.length; i++) {
                        long ttt = Double.doubleToLongBits(vs[i]);
                        int a = (int) ttt;
                        int b = (int) (ttt >>> 32);
                        out.s("%s %s %s %s %s %s %s %s", BaksmaliDumper.escapeValue((byte) (a & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (a >> 24))),
                                BaksmaliDumper.escapeValue((byte) (b & 0xFF)),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 8))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 16))),
                                BaksmaliDumper.escapeValue((byte) (0xFF & (b >> 24))));
                    }
                    out.pop();
                    out.s(".end array-data");
                }
            }
        }
    }

    @Override
    public void visitFieldStmt(Op op, int a, int b, Field field) {
        if (op.format == InstructionFormat.kFmt22c) {// iget,iput
            out.s("%s %s, %s, %s->%s:%s", op.displayName, reg(a), reg(b), BaksmaliDumper.escapeType(field.getOwner()),
                    BaksmaliDumper.escapeId(field.getName()), BaksmaliDumper.escapeType(field.getType()));
        } else {
            out.s("%s %s, %s->%s:%s", op.displayName, reg(a), BaksmaliDumper.escapeType(field.getOwner()),
                    BaksmaliDumper.escapeId(field.getName()), BaksmaliDumper.escapeType(field.getType()));
        }
    }

    @Override
    public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
        if (args.length > 0) {
            if (op.format == InstructionFormat.kFmt3rc) { // invoke-x/range
                out.s("%s { %d .. %d }, %s", op.displayName, reg(args[0]), reg(args[args.length - 1]),
                        BaksmaliDumper.escapeType(type));
            } else {
                StringBuilder buff = new StringBuilder();
                boolean first = true;
                for (int i : args) {
                    if (first) {
                        first = false;
                    } else {
                        buff.append(", ");
                    }
                    buff.append(reg(i));
                }
                out.s("%s { %s }, %s", op.displayName, buff, BaksmaliDumper.escapeType(type));
            }
        } else {
            out.s("%s { }, %s", op.displayName, BaksmaliDumper.escapeType(type));
        }

    }

    @Override
    public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
        if (op.format == InstructionFormat.kFmt21t || op.format == InstructionFormat.kFmt31t) {
            out.s(op.displayName + " " + reg(a) + ", " + xLabel(label));
        } else if (op.format == InstructionFormat.kFmt22t) {
            out.s(op.displayName + " " + reg(a) + ", " + reg(b) + ", " + xLabel(label));
        } else {
            out.s(op.displayName + " " + xLabel(label));
        }
    }

    DexDebugVisitor debugDumper = new DexDebugVisitor() {
        @Override
        public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
            super.visitStartLocal(reg, label, name, type, signature);
            if (signature == null) {
                out.s(".local %s, %s:%s", reg(reg), BaksmaliDumper.escapeValue(name), type);
            } else {
                out.s(".local %s, %s:%s, %s", reg(reg), BaksmaliDumper.escapeValue(name), type, BaksmaliDumper.escapeValue(signature));
            }
        }

        @Override
        public void visitPrologue(DexLabel dexLabel) {
            out.s(".prologue");
        }

        @Override
        public void visitEpiogue(DexLabel dexLabel) {
            out.s(".epiogue");
        }

        @Override
        public void visitLineNumber(int line, DexLabel label) {
            out.s(".line %d", line);
        }

        @Override
        public void visitEndLocal(int reg, DexLabel label) {
            out.s(".end local %s", reg(reg));
        }

        @Override
        public void visitRestartLocal(int reg, DexLabel label) {
            out.s(".restart local %s", reg(reg));
        }
    };

    @Override
    public void visitLabel(DexLabel label) {
        if (usedLabel.contains(label)) {
            out.s(xLabel(label));
        }
        List<DexDebugNode.DexDebugOpNode> dOps = debugLabelMap.get(label);
        if (dOps != null) {
            for (DexDebugNode.DexDebugOpNode dOp : dOps) {
                dOp.accept(debugDumper);
            }
        }
    }

    @Override
    final public DexDebugVisitor visitDebug() {
        return null;
    }

    @Override
    public void visitMethodStmt(Op op, int[] args, Method method) {

        if (args.length > 0) {
            if (op.format == InstructionFormat.kFmt3rc) { // invoke-x/range
                out.s("%s { %s .. %s }, %s->%s%s", op.displayName, reg(args[0]), reg(args[args.length - 1]),
                        BaksmaliDumper.escapeType(method.getOwner()), BaksmaliDumper.escapeId(method.getName()),
                        BaksmaliDumper.escapeMethodDesc(method));
            } else {
                boolean first = true;
                StringBuilder buff = new StringBuilder();
                for (int i : args) {
                    if (first) {
                        first = false;
                    } else {
                        buff.append(", ");
                    }
                    buff.append(reg(i));
                }
                out.s("%s { %s }, %s->%s%s", op.displayName, buff, BaksmaliDumper.escapeType(method.getOwner()),
                        BaksmaliDumper.escapeId(method.getName()), BaksmaliDumper.escapeMethodDesc(method));
            }
        } else {
            out.s("%s { }, %s->%s%s", op.displayName, BaksmaliDumper.escapeType(method.getOwner()),
                    BaksmaliDumper.escapeId(method.getName()), BaksmaliDumper.escapeMethodDesc(method));
        }

    }

    @Override
    public void visitPackedSwitchStmt(Op op, int ra, int first_case, DexLabel[] labels) {
        DexLabel dx = new DexLabel();
        dx.displayName = "L" + nextLabelNumber++;
        usedLabel.add(dx);
        out.s(op.displayName + " " + reg(ra) + ", " + xLabel(dx));
        appendLast.add(new AbstractMap.SimpleEntry<DexLabel, Object>(dx, new PackedSwitchStmt(first_case, labels)));
    }

    @Override
    public void visitRegister(int total) {
        if (useLocals) {
            out.s(".locals %d", startParamR);
        } else {
            out.s(".registers %d", total);
        }

    }

    @Override
    public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
        DexLabel dx = new DexLabel();
        dx.displayName = "L" + nextLabelNumber++;
        usedLabel.add(dx);
        out.s(op.displayName + " " + reg(ra) + ", " + xLabel(dx));
        appendLast.add(new AbstractMap.SimpleEntry<DexLabel, Object>(dx, new SparseSwitchStmt(cases, labels)));
    }

    @Override
    public void visitStmt0R(Op op) {
        if (op == Op.BAD_OP) {
            out.s("%s # bad op", Op.NOP.displayName);
        } else {
            out.s(op.displayName);
        }
    }

    @Override
    public void visitStmt1R(Op op, int a) {
        out.s(op.displayName + " " + reg(a));
    }

    @Override
    public void visitStmt2R(Op op, int a, int b) {
        out.s(op.displayName + " " + reg(a) + ", " + reg(b));
    }

    @Override
    public void visitStmt2R1N(Op op, int a, int b, int content) {
        out.s("%s %s, %s, %s", op.displayName, reg(a), reg(b), content);
    }

    @Override
    public void visitStmt3R(Op op, int a, int b, int c) {
        out.s("%s %s, %s, %s", op.displayName, reg(a), reg(b), reg(c));
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handler, String[] type) {
        for (int i = 0; i < type.length; i++) {
            String t = type[i];
            if (t == null) {
                out.s(".catchall { %s .. %s } %s", xLabel(start), xLabel(end), xLabel(handler[i]));
            } else {
                out.s(".catch %s { %s .. %s } %s", t, xLabel(start), xLabel(end), xLabel(handler[i]));
            }
        }

    }

    @Override
    public void visitTypeStmt(Op op, int a, int b, String type) {
        if (op.format == InstructionFormat.kFmt21c) {
            out.s("%s %s, %s", op.displayName, reg(a), BaksmaliDumper.escapeType(type));
        } else {
            out.s("%s %s, %s, %s", op.displayName, reg(a), reg(b), BaksmaliDumper.escapeType(type));
        }
    }

    String xLabel(DexLabel d) {
        return ":" + d.displayName;
    }
}
