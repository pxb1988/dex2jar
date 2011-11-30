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
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public abstract class AbstractDumpDexCodeAdapter extends EmptyVisitor {

    protected static final String[] causes = new String[] { "no-error", "generic-error", "no-such-class",
            "no-such-field", "no-such-method", "illegal-class-access", "illegal-field-access", "illegal-method-access",
            "class-change-error", "instantiation-error" };

    protected static String toJavaClass(String type) {
        return Dump.toJavaClass(type);
    }

    protected abstract void info(int opcode, String format, Object... args);

    protected abstract String labelToString(DexLabel label);

    @Override
    public void visitArrayStmt(int opcode, int value, int array, int index, int xt) {
        switch (opcode) {
        case OP_APUT:
            info(opcode, "v%d[v%d]=v%d", array, index, value);
            break;
        case OP_AGET:
            info(opcode, "v%d=v%d[v%d]", value, array, index);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitBinopLitXStmt(int, int, int, int)
     */
    @Override
    public void visitBinopLitXStmt(int opcode, int saveToReg, int opReg, int value) {
        switch (opcode) {
        case OP_AND_INT_LIT_X:
            info(opcode, "v%d = v%d & %d", saveToReg, opReg, value);
            break;
        case OP_ADD_INT_LIT_X:
            info(opcode, "v%d = v%d + %d", saveToReg, opReg, value);
            break;
        case OP_REM_INT_LIT_X:
            info(opcode, "v%d = v%d %% %d", saveToReg, opReg, value);
            break;
        case OP_DIV_INT_LIT_X:
            info(opcode, "v%d = v%d / %d", saveToReg, opReg, value);
            break;
        case OP_MUL_INT_LIT_X:
            info(opcode, "v%d = v%d * %d", saveToReg, opReg, value);
            break;
        case OP_SHR_INT_LIT_X:
            info(opcode, "v%d = v%d >> %d", saveToReg, opReg, value);
            break;
        case OP_SHL_INT_LIT_X:
            info(opcode, "v%d = v%d << %d", saveToReg, opReg, value);
            break;
        case OP_USHR_INT_LIT_X:
            info(opcode, "v%d = v%d >>> %d", saveToReg, opReg, value);
            break;
        case OP_OR_INT_LIT_X:
            info(opcode, "v%d = v%d | %d", saveToReg, opReg, value);
            break;
        case OP_XOR_INT_LIT_X:
            info(opcode, "v%d = v%d ^ %d", saveToReg, opReg, value);
            break;
        }
    }

    @Override
    public void visitBinopStmt(int opcode, int saveToReg, int opReg, int opReg2, int xt) {
        switch (opcode) {
        case OP_AND:
            info(opcode, "v%d = v%d & v%d", saveToReg, opReg, opReg2);
            break;
        case OP_OR:
            info(opcode, "v%d = v%d | v%d", saveToReg, opReg, opReg2);
            break;
        case OP_XOR:
            info(opcode, "v%d = v%d ^ v%d", saveToReg, opReg, opReg2);
            break;
        case OP_SUB:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opReg2);
            break;
        case OP_MUL:
            info(opcode, "v%d = v%d * v%d", saveToReg, opReg, opReg2);
            break;
        case OP_DIV:
            info(opcode, "v%d = v%d / v%d", saveToReg, opReg, opReg2);
            break;
        case OP_ADD:
            info(opcode, "v%d = v%d + v%d", saveToReg, opReg, opReg2);
            break;
        case OP_REM:
            info(opcode, "v%d = v%d %% v%d", saveToReg, opReg, opReg2);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int, java.lang.String, int, int)
     */
    @Override
    public void visitClassStmt(int opcode, int toReg, int fromReg, String type) {
        switch (opcode) {
        case OP_INSTANCE_OF:
            info(opcode, "v%d=v%d instanceof %s", toReg, fromReg, Dump.toJavaClass(type));
            break;
        case OP_NEW_ARRAY:
            info(opcode, "v%d=new %s[v%d]", toReg, Dump.toJavaClass(type), fromReg);
            break;

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int, java.lang.String, int)
     */
    @Override
    public void visitClassStmt(int opcode, int toReg, String type) {
        switch (opcode) {
        case OP_NEW_INSTANCE:
            info(opcode, "v%d=NEW %s", toReg, type);
            break;
        case OP_CHECK_CAST:
            info(opcode, "v%d=(%s) v%d", toReg, Dump.toJavaClass(type), toReg);
            break;
        }
    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {
        switch (opcode) {
        case OP_CMPL:
            info(opcode, "v%d = v%d cmpl v%d ", distReg, bB, cC);
            break;
        case OP_CMPG:
            info(opcode, "v%d = v%d cmpg v%d", distReg, bB, cC);
            break;
        case OP_CMP:
            info(opcode, "v%d = v%d cmp v%d", distReg, bB, cC);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLdcInsn(int, java.lang.Object, int)
     */
    @Override
    public void visitConstStmt(int opcode, int reg, Object value, int xt) {
        switch (opcode) {
        case OP_CONST:
            if (xt == TYPE_SINGLE) {
                info(opcode, "v%d=0x%08x  // int:%d   float:%f", reg, value, value,
                        Float.intBitsToFloat((Integer) value));
            } else {
                info(opcode, "v%d=0x%016x  // long:%d   double:%f", reg, value, value,
                        Double.longBitsToDouble((Long) value));
            }
            break;
        case OP_CONST_STRING:
            info(opcode, "v%d=\"%s\"", reg, value);
            break;
        case OP_CONST_CLASS:
            info(opcode, "v%d=%s.class", reg, value);
            break;
        default:
            info(opcode, "v%d=%s  //", reg, value);
            break;
        }

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {
        switch (opcode) {
        case OP_SPUT:
            info(opcode, "%s.%s=v%d  //%s", toJavaClass(field.getOwner()), field.getName(), fromOrToReg, field);
            break;
        case OP_SGET:
            info(opcode, "v%d=%s.%s  //%s", fromOrToReg, toJavaClass(field.getOwner()), field.getName(), field);
            break;
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int regFromOrTo, int owner_reg, Field field, int xt) {
        switch (opcode) {
        case OP_IGET:
            info(opcode, "v%d=v%d.%s  //%s", regFromOrTo, owner_reg, field.getName(), field);
            break;
        case OP_IPUT:
            info(opcode, "v%d.%s=v%d  //%s", owner_reg, field.getName(), regFromOrTo, field);
            break;
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt) {
        switch (opcode) {
        case OP_IGET_QUICK:
            info(opcode, "Q+ v%d=v%d.fieldoff+%04x", fromOrToReg, objReg, fieldoff);
            break;
        case OP_IPUT_QUICK:
            info(opcode, "Q+ v%d.fieldoff+%04x=v%d", objReg, fieldoff, fromOrToReg);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitFillArrayInsn(int, int, int, int, java.lang.Object[])
     */
    @Override
    public void visitFillArrayStmt(int opcode, int reg, int elemWidth, int initLength, Object[] values) {

        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(',').append(value);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        info(opcode, "v%d[0..%d]=[%s]", reg, initLength - 1, sb.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitFilledNewArrayIns(int, java.lang.String, int[])
     */
    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] regs, String type) {
        info(opcode, "TEMP=new %s[%d]", Dump.toJavaClass(type.substring(1)), regs.length);
        for (int i = 0; i < regs.length; i++) {
            info(opcode, "TEMP[%d]=v%d", i, regs[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int)
     */
    @Override
    public void visitJumpStmt(int opcode, DexLabel label) {
        switch (opcode) {
        case OP_GOTO:
            info(opcode, "goto %s", labelToString(label));
            break;
        }
    }

    public void visitJumpStmt(int opcode, int reg, DexLabel label) {
        switch (opcode) {
        case OP_IF_EQZ:
            info(opcode, "if v%d == 0 goto %s", reg, labelToString(label));
            break;
        case OP_IF_NEZ:
            info(opcode, "if v%d != 0 goto %s", reg, labelToString(label));
            break;
        case OP_IF_LTZ:
            info(opcode, "if v%d <  0 goto %s", reg, labelToString(label));
            break;
        case OP_IF_GEZ:
            info(opcode, "if v%d >= 0 goto %s", reg, labelToString(label));
            break;
        case OP_IF_GTZ:
            info(opcode, "if v%d >  0 goto %s", reg, labelToString(label));
            break;
        case OP_IF_LEZ:
            info(opcode, "if v%d <= 0 goto %s", reg, labelToString(label));
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int, int, int)
     */
    @Override
    public void visitJumpStmt(int opcode, int reg1, int reg2, DexLabel label) {
        switch (opcode) {
        case OP_IF_EQ:
            info(opcode, "if v%d == v%d goto %s", reg1, reg2, labelToString(label));
            break;
        case OP_IF_NE:
            info(opcode, "if v%d != v%d goto %s", reg1, reg2, labelToString(label));
            break;
        case OP_IF_LT:
            info(opcode, "if v%d <  v%d goto %s", reg1, reg2, labelToString(label));
            break;
        case OP_IF_GE:
            info(opcode, "if v%d >= v%d goto %s", reg1, reg2, labelToString(label));
            break;
        case OP_IF_GT:
            info(opcode, "if v%d >  v%d goto %s", reg1, reg2, labelToString(label));
            break;
        case OP_IF_LE:
            info(opcode, "if v%d <= v%d goto %s", reg1, reg2, labelToString(label));
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLookupSwitchInsn(int, int, int, int[], int[])
     */
    @Override
    public void visitLookupSwitchStmt(int opcode, int reg, DexLabel label, int[] cases, DexLabel[] label2) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < cases.length; i++) {
            info(-1, "case %d: goto %s", cases[i], labelToString(label2[i]));
        }
        info(-1, "default: goto %s", labelToString(label));
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, int a) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < args.length; j++) {
            sb.append('v').append(args[j]).append(',');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        switch (opcode) {
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_VIRTUAL_QUICK:
            info(opcode, "Q+ TEMP=taboff+%04x(%s)", a, sb.toString());
            break;
        case OP_EXECUTE_INLINE:
            info(opcode, "Q+ TEMP=inline+%04x(%s)", a, sb.toString());
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitMethodInsn(int, com.googlecode.dex2jar.Method, int[])
     */
    @Override
    public void visitMethodStmt(int opcode, int[] regs, Method method) {

        switch (opcode) {
        case OP_INVOKE_STATIC: {
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < method.getParameterTypes().length; j++) {
                sb.append('v').append(regs[i++]).append(',');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (method.getReturnType().equals("V")) {
                info(opcode, "%s.%s(%s)  //%s", toJavaClass(method.getOwner()), method.getName(), sb.toString(),
                        method.toString());
            } else {
                info(opcode, "TEMP=%s.%s(%s)  //%s", toJavaClass(method.getOwner()), method.getName(), sb.toString(),
                        method.toString());

            }
        }
            break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_SUPER: {
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < method.getParameterTypes().length; j++) {
                sb.append(',').append('v').append(regs[i++]);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            if (method.getReturnType().equals("V")) {
                info(opcode, "v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());
            } else {
                info(opcode, "TEMP=v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());
            }
        }
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitMonitorStmt(int, int)
     */
    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_MONITOR_ENTER:
            info(opcode, "lock v%d", reg);
            break;
        case OP_MONITOR_EXIT:
            info(opcode, "unlock v%d", reg);
            break;
        }
    }

    @Override
    public void visitMoveStmt(int opcode, int reg, int xt) {
        switch (opcode) {
        case OP_MOVE_RESULT:
            info(opcode, "v%d=TEMP", reg);
            break;
        case OP_MOVE_EXCEPTION:
            info(opcode, "v%d=@Exception", reg);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitMoveStmt(int, int, int)
     */
    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {
        switch (opcode) {
        case OP_MOVE:
            info(opcode, "v%d = v%d", toReg, fromReg);
            break;
        }
    }

    public void visitReturnStmt(int opcode) {
        switch (opcode) {
        case OP_RETURN_VOID:
            info(opcode, "return");
            break;
        }
    }

    @Override
    public void visitReturnStmt(int opcode, int reg, int xt) {
        switch (opcode) {
        case OP_RETURN:
            info(opcode, "return v%d", reg);
            break;
        case OP_THROW:
            info(opcode, "throw v%d", reg);
            break;
        }
    }

    @Override
    public void visitReturnStmt(int opcode, int cause, Object ref) {
        String c = cause >= causes.length ? "unknown" : causes[cause];
        info(opcode, "Q+ throw new VerificationError(%s)", c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTableSwitchInsn(int, int, int, int, int, int[])
     */
    @Override
    public void visitTableSwitchStmt(int opcode, int reg, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < labels.length; i++) {
            info(opcode, "case %d: goto %s", first_case + i, labelToString(labels[i]));
        }
        info(opcode, "default: goto %s", labelToString(label));
    }

    @Override
    public void visitUnopStmt(int opcode, int saveToReg, int opReg, int xt) {
        switch (opcode) {
        case OP_NEG:
            info(opcode, "v%d = ~v%d", saveToReg, opReg);
            break;
        case OP_NOT:
            info(opcode, "v%d = !v%d", saveToReg, opReg);
            break;
        case OP_ARRAY_LENGTH:
            info(opcode, "v%d = v%d.length", saveToReg, opReg);
            break;
        }
    }

    @Override
    public void visitUnopStmt(int opcode, int saveToReg, int opReg, int xta, int xtb) {
        switch (opcode) {
        case OP_X_TO_Y:
            switch (xtb) {
            case TYPE_BOOLEAN:
                info(opcode, "v%d = (boolean)v%d", saveToReg, opReg);
                break;
            case TYPE_BYTE:
                info(opcode, "v%d = (byte)v%d", saveToReg, opReg);
                break;
            case TYPE_CHAR:
                info(opcode, "v%d = (char)v%d", saveToReg, opReg);
                break;
            case TYPE_DOUBLE:
                info(opcode, "v%d = (double)v%d", saveToReg, opReg);
                break;
            case TYPE_FLOAT:
                info(opcode, "v%d = (float)v%d", saveToReg, opReg);
                break;
            case TYPE_INT:
                info(opcode, "v%d = (int)v%d", saveToReg, opReg);
                break;
            case TYPE_LONG:
                info(opcode, "v%d = (long)v%d", saveToReg, opReg);
                break;
            case TYPE_SHORT:
                info(opcode, "v%d = (short)v%d", saveToReg, opReg);
                break;
            }
            break;
        }
    }
}
