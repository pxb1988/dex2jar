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
package com.googlecode.d2j.util;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ASMifierCodeV extends DexCodeVisitor implements DexConstants {
    Out m;
    Map<DexLabel, String> labelMap = new HashMap<>();

    public ASMifierCodeV(Out m) {
        this.m = m;
    }

    @Override
    public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
        m.s("code.visitStmt2R1N(%s,%s,%s,%s);", op(op), distReg, srcReg, content);
    }

    @Override
    public void visitRegister(int total) {
        m.s("code.visitRegister(%s);", total);

    }

    @Override
    public void visitStmt3R(Op op, int a, int b, int c) {
        m.s("code.visitStmt3R(%s,%s,%s,%s);", op(op), a, b, c);

    }

    @Override
    public void visitStmt2R(Op op, int a, int b) {
        m.s("code.visitStmt2R(%s,%s,%s);", op(op), a, b);

    }

    @Override
    public void visitStmt0R(Op op) {
        m.s("code.visitStmt0R(%s);", op(op));

    }

    @Override
    public void visitStmt1R(Op op, int reg) {
        m.s("code.visitStmt1R(%s,%s);", op(op), reg);

    }

    @Override
    public void visitTypeStmt(Op op, int a, int b, String type) {
        m.s("code.visitTypeStmt(%s,%s,%s,%s);", op(op), a, b, Escape.v(type));
    }

    @Override
    public void visitConstStmt(Op op, int toReg, Object value) {
        if (value instanceof Integer) {
            m.s("code.visitConstStmt(%s,%s,%s); // int: 0x%08x  float:%f", op(op), toReg, Escape.v(value), value,
                    Float.intBitsToFloat((Integer) value));
        } else if (value instanceof Long) {
            m.s("code.visitConstStmt(%s,%s,%s); // long: 0x%016x  double:%f", op(op), toReg, Escape.v(value), value,
                    Double.longBitsToDouble((Long) value));
        } else {
            m.s("code.visitConstStmt(%s,%s,%s);", op(op), toReg, Escape.v(value));
        }

    }

    @Override
    public void visitFieldStmt(Op op, int fromOrToReg, int objReg, Field field) {
        m.s("code.visitFieldStmt(%s,%s,%s,%s);", op(op), fromOrToReg, objReg, Escape.v(field));
    }

    @Override
    public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
        m.s("code.visitFilledNewArrayStmt(%s,%s,%s);", op(op), Escape.v(args), Escape.v(type));
    }

    int i = 0;

    public String v(DexLabel[] labels) {
        StringBuilder sb = new StringBuilder("new DexLabel[]{");
        boolean first = true;
        for (DexLabel dexLabel : labels) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(dexLabel));
        }
        return sb.append("}").toString();
    }

    private Object v(DexLabel l) {
        String name = labelMap.get(l);
        if (name == null) {
            name = "L" + i++;
            m.s("DexLabel %s=new DexLabel();", name);
            labelMap.put(l, name);
        }
        return name;
    }

    String op(Op op) {
        return op.name();
    }

    @Override
    public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
        m.s("code.visitJumpStmt(%s,%s,%s,%s);", op(op), a, b, v(label));
    }

    @Override
    public void visitMethodStmt(Op op, int[] args, Method method) {
        m.s("code.visitMethodStmt(%s,%s,%s);", op(op), Escape.v(args), Escape.v(method));
    }

    @Override
    public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
        m.s("code.visitSparseSwitchStmt(%s,%s,%s,%s);", op(op), ra, Escape.v(cases), v(labels));
    }

    @Override
    public void visitPackedSwitchStmt(Op op, int ra, int first_case, DexLabel[] labels) {
        m.s("code.visitSparseSwitchStmt(%s,%s,%s,%s);", op(op), ra, first_case, v(labels));
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
        m.s("code.visitTryCatch(%s,%s,%s,%s);", v(start), v(end), v(handlers), Escape.v(types));
    }

    @Override
    public void visitEnd() {
        m.s("code.visitEnd();");
    }

    @Override
    public void visitLabel(DexLabel label) {
        m.s("code.visitLabel(%s);", v(label));
    }

    @Override
    public void visitFillArrayDataStmt(Op op, int ra, Object array) {
        // FIXME
        super.visitFillArrayDataStmt(op, ra, array);
    }

    @Override
    public DexDebugVisitor visitDebug() {
        m.s("DexDebugVisitor ddv=new DexDebugVisitor(code.visitDebug());");
        return new DexDebugVisitor() {
            @Override
            public void visitParameterName(int reg, String name) {
                m.s("ddv.visitParameterName(%d,%s);", reg, Escape.v(name));
            }

            @Override
            public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
                m.s("ddv.visitStartLocal(%d,%s,%s,%s,%s);", reg, v(label), Escape.v(name), Escape.v(type),
                        Escape.v(signature));
            }

            @Override
            public void visitLineNumber(int line, DexLabel label) {
                m.s("ddv.visitLineNumber(%d,%s);", line, v(label));
            }

            @Override
            public void visitPrologue(DexLabel dexLabel) {
                m.s("ddv.visitPrologue(%s);", v(dexLabel));
            }

            @Override
            public void visitEpiogue(DexLabel dexLabel) {
                m.s("ddv.visitEpiogue(%s);", v(dexLabel));
            }

            @Override
            public void visitEndLocal(int reg, DexLabel label) {
                m.s("ddv.visitEndLocal(%d,%s);", reg, v(label));
            }

            @Override
            public void visitSetFile(String file) {
                m.s("ddv.visitSetFile(%s);", Escape.v(file));
            }

            @Override
            public void visitRestartLocal(int reg, DexLabel label) {
                m.s("ddv.visitRestartLocal(%d,%s);", reg, v(label));
            }
        };
    }
}
