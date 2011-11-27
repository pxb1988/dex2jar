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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexOpcodeDump;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DumpDexCodeAdapter extends EmptyVisitor implements DexOpcodes {
    private static class TryCatch {
        public DexLabel end;

        public DexLabel handler;
        public DexLabel start;
        public String type;

        public TryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
            super();
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
    }

    private static String c(String type) {
        return Dump.toJavaClass(type);
    }

    private List<DexLabel> labels = new ArrayList<DexLabel>();

    private Method method;

    private PrintWriter out;

    private List<TryCatch> trys = new ArrayList<TryCatch>();

    private boolean isStatic;

    /**
     * @param dcv
     */
    public DumpDexCodeAdapter( boolean isStatic, Method m, PrintWriter out) {
        this.method = m;
        this.out = out;
        this.isStatic = isStatic;
    }

    protected void info(int opcode, String format, Object... args) {
        String s = String.format(format, args);
        if (opcode < 0) {
            out.printf("%-20s|%5s|%s\n", "", "", s);
        } else {
            out.printf("%-20s|%5s|%s\n", DexOpcodeDump.dump(opcode), "", s);
        }
    }

    protected int labelIndex(DexLabel label) {
        int i = labels.indexOf(label);
        if (i > -1)
            return i;
        labels.add(label);
        return labels.indexOf(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitArrayInsn(int, int, int, int)
     */
    @Override
    public void visitArrayStmt(int opcode, int value, int array, int index) {
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
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitEnd()
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitFieldInsn(int, com.googlecode.dex2jar.Field, int, int)
     */
    @Override
    public void visitFieldStmt(int opcode, int regFromOrTo, int owner_reg, Field field) {
        switch (opcode) {
        case OP_IGET:
            info(opcode, "v%d=v%d.%s  //%s", regFromOrTo, owner_reg, field.getName(), field);
            break;
        case OP_IPUT:
            info(opcode, "v%d.%s=v%d  //%s", owner_reg, field.getName(), regFromOrTo, field);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitFieldStmt(int, int, com.googlecode.dex2jar.Field)
     */
    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field) {
        switch (opcode) {
        case OP_SPUT:
            info(opcode, "%s.%s=v%d  //%s", c(field.getOwner()), field.getName(), fromOrToReg, field);
            break;
        case OP_SGET:
            info(opcode, "v%d=%s.%s  //%s", fromOrToReg, c(field.getOwner()), field.getName(), field);
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
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int, int)
     */
    @Override
    public void visitUnopStmt(int opcode, int saveToReg, int opReg) {
        switch (opcode) {
        case OP_NEG_INT:
        case OP_NEG_DOUBLE:
        case OP_NEG_FLOAT:
        case OP_NEG_LONG:
            info(opcode, "v%d = ~v%d", saveToReg, opReg);
            break;
        case OP_INT_TO_BYTE:
            info(opcode, "v%d = (byte)v%d", saveToReg, opReg);
            break;
        case OP_INT_TO_CHAR:
            info(opcode, "v%d = (char)v%d", saveToReg, opReg);
            break;
        case OP_INT_TO_DOUBLE:
        case OP_INT_TO_FLOAT:
        case OP_INT_TO_LONG:
            info(opcode, "v%d = v%d", saveToReg, opReg);
            break;
        case OP_INT_TO_SHORT:
            info(opcode, "v%d = (short)v%d", saveToReg, opReg);
            break;
        case OP_LONG_TO_DOUBLE:
        case OP_LONG_TO_FLOAT:
            info(opcode, "v%d = v%d", saveToReg, opReg);
            break;
        case OP_LONG_TO_INT:
            info(opcode, "v%d = (int)v%d", saveToReg, opReg);
            break;
        case OP_DOUBLE_TO_FLOAT:
            info(opcode, "v%d = (float)v%d", saveToReg, opReg);
            break;
        case OP_DOUBLE_TO_INT:
            info(opcode, "v%d = (int)v%d", saveToReg, opReg);
            break;
        case OP_DOUBLE_TO_LONG:
            info(opcode, "v%d = (long)v%d", saveToReg, opReg);
            break;
        case OP_FLOAT_TO_INT:
            info(opcode, "v%d = (int)v%d", saveToReg, opReg);
            break;
        case OP_FLOAT_TO_DOUBLE:
            info(opcode, "v%d = v%d", saveToReg, opReg);
            break;
        case OP_FLOAT_TO_LONG:
            info(opcode, "v%d = (long)v%d", saveToReg, opReg);
            break;

        case OP_ARRAY_LENGTH:
            info(opcode, "v%d = v%d.length", saveToReg, opReg);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int, int, int)
     */
    @Override
    public void visitBinopStmt(int opcode, int saveToReg, int opReg, int opReg2) {
        switch (opcode) {
        case OP_AND_INT:
        case OP_AND_LONG:
            info(opcode, "v%d = v%d & v%d", saveToReg, opReg, opReg2);
            break;
        case OP_OR_INT:
        case OP_OR_LONG:
            info(opcode, "v%d = v%d | v%d", saveToReg, opReg, opReg2);
            break;
        case OP_XOR_INT:
        case OP_XOR_LONG:
            info(opcode, "v%d = v%d ^ v%d", saveToReg, opReg, opReg2);
            break;
        case OP_CMP_LONG:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opReg2);
            break;
        case OP_MUL_INT:
        case OP_MUL_LONG:
        case OP_MUL_FLOAT:
        case OP_MUL_DOUBLE:
            info(opcode, "v%d = v%d * v%d", saveToReg, opReg, opReg2);
            break;
        case OP_DIV_INT:
        case OP_DIV_LONG:
        case OP_DIV_FLOAT:
        case OP_DIV_DOUBLE:
            info(opcode, "v%d = v%d / v%d", saveToReg, opReg, opReg2);
            break;
        case OP_ADD_INT:
        case OP_ADD_LONG:
        case OP_ADD_FLOAT:
        case OP_ADD_DOUBLE:
            info(opcode, "v%d = v%d + v%d", saveToReg, opReg, opReg2);
            break;
        case OP_SUB_INT:
        case OP_SUB_DOUBLE:
        case OP_SUB_FLOAT:
        case OP_SUB_LONG:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opReg2);
            break;
        case OP_REM_LONG:
        case OP_REM_INT:
        case OP_REM_FLOAT:
        case OP_REM_DOUBLE:
            info(opcode, "v%d = v%d %% v%d", saveToReg, opReg, opReg2);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitCmpStmt(int, int, int, int)
     */
    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC) {
        switch (opcode) {
        case OP_CMPL_DOUBLE:
        case OP_CMPL_FLOAT:
            info(opcode, "v%d = v%d cmpl v%d ", distReg, bB, cC);
            break;
        case OP_CMPG_DOUBLE:
        case OP_CMPG_FLOAT:
            info(opcode, "v%d = v%d cmpg v%d", distReg, bB, cC);
            break;
        case OP_CMP_LONG:
            info(opcode, "v%d = v%d cmp v%d", distReg, bB, cC);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitMoveStmt(int, int, int)
     */
    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg) {
        switch (opcode) {
        case OP_MOVE_OBJECT:
        case OP_MOVE:
        case OP_MOVE_WIDE:
            info(opcode, "v%d = v%d", toReg, fromReg);
            break;
        }
    }

    @Override
    public void visitArguments(int total, int[] args) {
        int i = 0;
        if (!this.isStatic) {
            int reg = args[i++];
            String type = Dump.toJavaClass(method.getOwner());
            out.printf("%20s:v%d   //%s\n", "this", reg, type);
        }
        for (String type : method.getParameterTypes()) {
            int reg = args[i++];
            type = Dump.toJavaClass(type);
            out.printf("%20s:v%d   //%s\n", "", reg, type);
        }
    }

    public void visitReturnStmt(int opcode) {
        switch (opcode) {
        case OP_RETURN_VOID:
            info(opcode, "return");
            break;
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
            info(opcode, "goto L%s", labelIndex(label));
            break;
        }
    }

    public void visitJumpStmt(int opcode, int reg, DexLabel label) {
        switch (opcode) {
        case OP_IF_EQZ:
            info(opcode, "if v%d == 0 goto L%s", reg, labelIndex(label));
            break;
        case OP_IF_NEZ:
            info(opcode, "if v%d != 0 goto L%s", reg, labelIndex(label));
            break;
        case OP_IF_LTZ:
            info(opcode, "if v%d <  0 goto L%s", reg, labelIndex(label));
            break;
        case OP_IF_GEZ:
            info(opcode, "if v%d >= 0 goto L%s", reg, labelIndex(label));
            break;
        case OP_IF_GTZ:
            info(opcode, "if v%d >  0 goto L%s", reg, labelIndex(label));
            break;
        case OP_IF_LEZ:
            info(opcode, "if v%d <= 0 goto L%s", reg, labelIndex(label));
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
            info(opcode, "if v%d == v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        case OP_IF_NE:
            info(opcode, "if v%d != v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        case OP_IF_LT:
            info(opcode, "if v%d <  v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        case OP_IF_GE:
            info(opcode, "if v%d >= v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        case OP_IF_GT:
            info(opcode, "if v%d >  v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        case OP_IF_LE:
            info(opcode, "if v%d <= v%d goto L%s", reg1, reg2, labelIndex(label));
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLabel(int)
     */
    @Override
    public void visitLabel(DexLabel label) {
        boolean find = false;
        for (TryCatch tc : trys) {
            if (label.equals(tc.end)) {
                info(-1, " } // TC_%d", trys.indexOf(tc));
                find = true;
                break;
            }

        }
        out.printf("%-20s|%5s:\n", "LABEL", "L" + labelIndex(label));
        if (!find) {
            for (TryCatch tc : trys) {
                if (label.equals(tc.start)) {
                    info(-1, "try { // TC_%d ", trys.indexOf(tc));
                    break;
                }
                if (label.equals(tc.handler)) {
                    String t = tc.type;
                    info(-1, "catch(%s) // TC_%d", t == null ? "all" : t, trys.indexOf(tc));
                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLdcInsn(int, java.lang.Object, int)
     */
    @Override
    public void visitConstStmt(int opcode, int reg, Object value) {
        switch (opcode) {
        case OP_CONST:
            info(opcode, "v%d=0x%08x  // int:%d   float:%f", reg, value, value, Float.intBitsToFloat((Integer) value));
            break;
        case OP_CONST_WIDE:
            info(opcode, "v%d=0x%016x  // long:%d   double:%f", reg, value, value,
                    Double.longBitsToDouble((Long) value));
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

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitLookupSwitchInsn(int, int, int, int[], int[])
     */
    @Override
    public void visitLookupSwitchStmt(int opcode, int reg, DexLabel label, int[] cases, DexLabel[] label2) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < cases.length; i++) {
            info(-1, "case %d: goto L%s", cases[i], labelIndex(label2[i]));
        }
        info(-1, "default: goto L%s", labelIndex(label));
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
                info(opcode, "%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(),
                        method.toString());
            } else {
                info(opcode, "TEMP=%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(),
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
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTableSwitchInsn(int, int, int, int, int, int[])
     */
    @Override
    public void visitTableSwitchStmt(int opcode, int reg, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < labels.length; i++) {
            info(opcode, "case %d: goto L%s", first_case + i, labelIndex(labels[i]));
        }
        info(opcode, "default: goto L%s", labelIndex(label));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitTryCatch(int, int, int, java.lang.String)
     */
    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
        TryCatch tc = new TryCatch(start, end, handler, type);
        trys.add(tc);
        int id = trys.indexOf(tc);
        if (type == null) {
            out.printf("TR_%d L%s ~ L%s > L%s all\n", id, labelIndex(start), labelIndex(end), labelIndex(handler));
        } else {
            out.printf("TR_%d L%s ~ L%s > L%s %s\n", id, labelIndex(start), labelIndex(end), labelIndex(handler), type);
        }
        super.visitTryCatch(start, end, handler, type);
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
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitVarInsn(int, int)
     */
    @Override
    public void visitMoveStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_MOVE_RESULT_OBJECT:
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_EXCEPTION:
            info(opcode, "v%d=TEMP", reg);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeAdapter#visitReturnStmt(int, int)
     */
    @Override
    public void visitReturnStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_RETURN:
            info(opcode, "return v%d", reg);
            break;
        case OP_THROW:
            info(opcode, "throw v%d", reg);
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

}
