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
package com.googlecode.dex2jar.util;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.OdexCodeVisitor;

/**
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ASMifierCodeV implements OdexCodeVisitor, DexOpcodes {
    Out m;

    public ASMifierCodeV(Out m) {
        this.m = m;
    }

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg, int xt) {
        m.s("code.visitArrayStmt(%s,%s,%s,%s,%s);", op(opcode), formOrToReg, arrayReg, indexReg, xt);
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int content) {
        m.s("code.visitBinopLitXStmt(%s,%s,%s,%s);", op(opcode), distReg, srcReg, content);
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt) {
        m.s("code.visitBinopStmt(%s,%s,%s,%s,%s);", op(opcode), toReg, r1, r2, xt);
    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        m.s("code.visitClassStmt(%s,%s,%s,%s);", op(opcode), a, b, Escape.v(type));
    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {
        m.s("code.visitClassStmt(%s,%s,%s);", op(opcode), saveTo, Escape.v(type));
    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {
        m.s("code.visitCmpStmt(%s,%s,%s,%s,%s);", op(opcode), distReg, bB, cC, xt);
    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value, int xt) {
        if (value instanceof Integer) {
            m.s("code.visitConstStmt(%s,%s,%s,%s); // int: 0x%08x  float:%f", op(opcode), toReg, Escape.v(value), xt,
                    value, Float.intBitsToFloat((Integer) value));
        } else if (value instanceof Long) {
            m.s("code.visitConstStmt(%s,%s,%s,%s); // long: 0x%016x  double:%f", op(opcode), toReg, Escape.v(value),
                    xt, value, Double.longBitsToDouble((Long) value));
        } else {
            m.s("code.visitConstStmt(%s,%s,%s,%s);", op(opcode), toReg, Escape.v(value), xt);
        }

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {
        m.s("code.visitFieldStmt(%s,%s,%s,%s);", op(opcode), fromOrToReg, Escape.v(field), xt);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt) {
        m.s("code.visitFieldStmt(%s,%s,%s,%s,%s);", op(opcode), fromOrToReg, objReg, Escape.v(field), xt);
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        m.s("code.visitFillArrayStmt(%s,%s,%s,%s,%s);", op(opcode), aA, elemWidth, initLength, Escape.v(values));
    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        m.s("code.visitFilledNewArrayStmt(%s,%s,%s);", op(opcode), Escape.v(args), Escape.v(type));
    }

    int i = 0;

    private Object v(DexLabel l) {
        if (l.info == null) {

            l.info = "L" + i++;
            m.s("DexLabel %s=new DexLabel();", l.info);
        }
        return l.info;
    }

    String op(int op) {
        return "OP_" + DexOpcodeDump.dump(op);
    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, DexLabel label) {
        m.s("code.visitJumpStmt(%s,%s,%s,%s);", op(opcode), a, b, v(label));
    }

    @Override
    public void visitJumpStmt(int opcode, int reg, DexLabel label) {
        m.s("code.visitJumpStmt(%s,%s,%s);", op(opcode), reg, v(label));
    }

    @Override
    public void visitJumpStmt(int opcode, DexLabel label) {
        m.s("code.visitJumpStmt(%s,%s);", op(opcode), v(label));
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (DexLabel dexLabel : labels) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(dexLabel));
        }

        m.s("code.visitLookupSwitchStmt(%s,%s,%s,%s,new DexLabel[]{%s});", op(opcode), aA, v(label), Escape.v(cases),
                sb.toString());
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        m.s("code.visitMethodStmt(%s,%s,%s);", op(opcode), Escape.v(args), Escape.v(method));
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        m.s("code.visitMonitorStmt(%s,%s);", op(opcode), reg);
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int xt) {
        m.s("code.visitMoveStmt(%s,%s,%s);", op(opcode), toReg, xt);
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {
        m.s("code.visitMoveStmt(%s,%s,%s,%s);", op(opcode), toReg, fromReg, xt);
    }

    @Override
    public void visitReturnStmt(int opcode) {
        m.s("code.visitReturnStmt(%s);", op(opcode));
    }

    @Override
    public void visitReturnStmt(int opcode, int reg, int xt) {
        m.s("code.visitReturnStmt(%s,%s,%s);", op(opcode), reg, xt);
    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (DexLabel dexLabel : labels) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(dexLabel));
        }
        m.s("code.visitTableSwitchStmt(%s,%s,%s,%s,%s,new DexLabel[]{%s});", op(opcode), aA, v(label), first_case,
                last_case, sb.toString());
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xt) {
        m.s("code.visitUnopStmt(%s,%s,%s,%s);", op(opcode), toReg, fromReg, xt);
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb) {
        m.s("code.visitUnopStmt(%s,%s,%s,%s,%s);", op(opcode), toReg, fromReg, xta, xtb);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
        m.s("code.visitTryCatch(%s,%s,%s,%s);", v(start), v(end), v(handler), Escape.v(type));
    }

    @Override
    public void visitArguments(int total, int[] args) {
        m.s("code.visitArguments(%s,%s);", total, Escape.v(args));
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
    public void visitLineNumber(int line, DexLabel label) {
        m.s("code.visitLineNumber(%s,%s);", line, v(label));
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {
        m.s("code.visitLocalVariable(%s,%s,%s,%s,%s,%s);", Escape.v(name), Escape.v(type), Escape.v(signature),
                v(start), v(end), reg);
    }

    @Override
    public void visitReturnStmt(int opcode, int cause, Object ref) {
        m.s("((OdexCodeVisitor)code).visitReturnStmt(%s,%s,%s);", op(opcode), cause, Escape.v(ref));
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, int a) {
        m.s("((OdexCodeVisitor)code).visitMethodStmt(%s,%s,%s);", op(opcode), Escape.v(args), a);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt) {
        m.s("((OdexCodeVisitor)code).visitFieldStmt(%s,%s,%s,%s,%s);", op(opcode), fromOrToReg, objReg, fieldoff, xt);
    }
}
