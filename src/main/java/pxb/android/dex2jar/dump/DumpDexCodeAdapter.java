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
package pxb.android.dex2jar.dump;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeAdapter;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DumpDexCodeAdapter extends DexCodeAdapter implements DexOpcodes {
    private static class TryCatch {
        public Label end;

        public Label handler;
        public Label start;
        public String type;

        public TryCatch(Label start, Label end, Label handler, String type) {
            super();
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
    }

    private static String c(String type) {
        return Type.getType(type).getClassName();
    }

    private List<Label> labels = new ArrayList<Label>();

    private Method method;

    private PrintWriter out;

    private List<TryCatch> trys = new ArrayList<TryCatch>();

    /**
     * @param dcv
     */
    public DumpDexCodeAdapter(DexCodeVisitor dcv, Method m, PrintWriter out) {
        super(dcv);
        this.method = m;
        this.out = out;
    }

    protected void info(int opcode, String format, Object... args) {
        String s = String.format(format, args);
        if (opcode < 0) {
            out.printf("%-20s|%5s|%s\n", "", "", s);
        } else {
            out.printf("%-20s|%5s|%s\n", DexOpcodeDump.dump(opcode), "", s);
        }
    }

    protected int labelIndex(Label label) {
        int i = labels.indexOf(label);
        if (i > -1)
            return i;
        labels.add(label);
        return labels.indexOf(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitArrayInsn(int, int, int, int)
     */
    @Override
    public void visitArrayInsn(int opcode, int value, int array, int index) {
        switch (opcode) {
        case OP_APUT:
        case OP_APUT_BOOLEAN:
        case OP_APUT_BYTE:
        case OP_APUT_CHAR:
        case OP_APUT_OBJECT:
        case OP_APUT_SHORT:
        case OP_APUT_WIDE:
            info(opcode, "v%d[v%d]=v%d", array, index, value);
            break;
        case OP_AGET:
        case OP_AGET_BOOLEAN:
        case OP_AGET_BYTE:
        case OP_AGET_CHAR:
        case OP_AGET_OBJECT:
        case OP_AGET_SHORT:
        case OP_AGET_WIDE:
            info(opcode, "v%d=v%d[v%d]", value, array, index);
            break;
        }
        super.visitArrayInsn(opcode, value, array, index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitArrayInsn(int, java.lang.String, int, int)
     */
    @Override
    public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
        String type_show = Type.getType(type).getElementType().getClassName();
        info(opcode, "v%d=new %s[v%d]", saveToReg, type_show, demReg);
        super.visitArrayInsn(opcode, type, saveToReg, demReg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitEnd()
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFieldInsn(int, pxb.android.dex2jar.Field, int, int)
     */
    @Override
    public void visitFieldInsn(int opcode, Field field, int regFromOrTo, int owner_reg) {
        switch (opcode) {
        case OP_IGET_OBJECT:
        case OP_IGET_BOOLEAN:
        case OP_IGET_BYTE:
        case OP_IGET_SHORT:
        case OP_IGET:
        case OP_IGET_WIDE:
            info(opcode, "v%d=v%d.%s  //%s", regFromOrTo, owner_reg, field.getName(), field);
            break;
        case OP_IPUT_OBJECT:
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BYTE:
        case OP_IPUT_SHORT:
        case OP_IPUT:
        case OP_IPUT_WIDE:
            info(opcode, "v%d.%s=v%d  //%s", owner_reg, field.getName(), regFromOrTo, field);
            break;
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_BYTE:
        case OP_SPUT_CHAR:
        case OP_SPUT_SHORT:
        case OP_SPUT_WIDE:
        case OP_SPUT:
            info(opcode, "%s.%s=v%d  //%s", c(field.getOwner()), field.getName(), regFromOrTo, field);
            break;
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_BYTE:
        case OP_SGET_CHAR:
        case OP_SGET_SHORT:
        case OP_SGET_WIDE:
        case OP_SGET:
            info(opcode, "v%d=%s.%s  //%s", regFromOrTo, c(field.getOwner()), field.getName(), field);
            break;
        }

        super.visitFieldInsn(opcode, field, regFromOrTo, owner_reg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFillArrayInsn(int, int, int, int, java.lang.Object[])
     */
    @Override
    public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {

        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(',').append(value);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        info(opcode, "v%d[0..%d]=[%s]", reg, initLength - 1, sb.toString());

        super.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitFilledNewArrayIns(int, java.lang.String, int[])
     */
    @Override
    public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
        info(opcode, "TEMP=new %s[%d]", Type.getType(type).getElementType().getClassName(), regs.length);
        for (int i = 0; i < regs.length; i++) {
            info(opcode, "TEMP[%d]=v%d", i, regs[i]);
        }
        super.visitFilledNewArrayIns(opcode, type, regs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int, int)
     */
    @Override
    public void visitInInsn(int opcode, int saveToReg, int opReg) {
        switch (opcode) {
        case OP_AND_INT_2ADDR:
        case OP_AND_LONG_2ADDR:
            info(opcode, "v%d &= v%d", saveToReg, opReg);
            break;
        case OP_OR_INT_2ADDR:
        case OP_OR_LONG_2ADDR:
            info(opcode, "v%d |= v%d", saveToReg, opReg);
            break;
        case OP_XOR_INT_2ADDR:
        case OP_XOR_LONG_2ADDR:
            info(opcode, "v%d ^= v%d", saveToReg, opReg);
            break;
        case OP_MUL_LONG_2ADDR:
        case OP_MUL_INT_2ADDR:
        case OP_MUL_FLOAT_2ADDR:
        case OP_MUL_DOUBLE_2ADDR:
            info(opcode, "v%d *= v%d", saveToReg, opReg);
            break;
        case OP_SUB_INT_2ADDR:
        case OP_SUB_LONG_2ADDR:
        case OP_SUB_FLOAT_2ADDR:
        case OP_SUB_DOUBLE_2ADDR:
            info(opcode, "v%d -= v%d", saveToReg, opReg);
            break;
        case OP_REM_INT_2ADDR:
        case OP_REM_LONG_2ADDR:
            info(opcode, "v%d %%= v%d", saveToReg, opReg);
            break;
        case OP_DIV_INT_2ADDR:
        case OP_DIV_LONG_2ADDR:
        case OP_DIV_FLOAT_2ADDR:
        case OP_DIV_DOUBLE_2ADDR:
            info(opcode, "v%d /= v%d", saveToReg, opReg);
            break;
        case OP_ADD_INT_2ADDR:
        case OP_ADD_LONG_2ADDR:
        case OP_ADD_FLOAT_2ADDR:
        case OP_ADD_DOUBLE_2ADDR:
            info(opcode, "v%d += v%d", saveToReg, opReg);
            break;
        case OP_NEG_INT:
        case OP_NEG_DOUBLE:
        case OP_NEG_FLOAT:
        case OP_NEG_LONG:
            info(opcode, "v%d = ~v%d", saveToReg, opReg);
            break;
        case OP_MOVE_OBJECT:
        case OP_MOVE:
        case OP_MOVE_WIDE:
        case OP_MOVE_OBJECT_FROM16:
        case OP_MOVE_FROM16:
        case OP_MOVE_WIDE_FROM16:
            info(opcode, "v%d = v%d", saveToReg, opReg);
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
        super.visitInInsn(opcode, saveToReg, opReg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInInsn(int, int, int, int)
     */
    @Override
    public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
        switch (opcode) {
        case OP_AND_INT:
        case OP_AND_LONG:
            info(opcode, "v%d = v%d & v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_OR_INT:
        case OP_OR_LONG:
            info(opcode, "v%d = v%d | v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_XOR_INT:
        case OP_XOR_LONG:
            info(opcode, "v%d = v%d ^ v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_CMP_LONG:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_MUL_INT:
        case OP_MUL_LONG:
        case OP_MUL_FLOAT:
        case OP_MUL_DOUBLE:
            info(opcode, "v%d = v%d * v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_DIV_INT:
        case OP_DIV_LONG:
        case OP_DIV_FLOAT:
        case OP_DIV_DOUBLE:
            info(opcode, "v%d = v%d / v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_ADD_INT:
        case OP_ADD_LONG:
        case OP_ADD_FLOAT:
        case OP_ADD_DOUBLE:
            info(opcode, "v%d = v%d + v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_SUB_INT:
        case OP_SUB_DOUBLE:
        case OP_SUB_FLOAT:
        case OP_SUB_LONG:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_REM_LONG:
        case OP_REM_INT:
        case OP_REM_FLOAT:
        case OP_REM_DOUBLE:
            info(opcode, "v%d = v%d %% v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_CMPL_DOUBLE:
        case OP_CMPL_FLOAT:
            info(opcode, "v%d = v%d - v%d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_CMPG_DOUBLE:
        case OP_CMPG_FLOAT:
            info(opcode, "v%d = v%d - v%d", saveToReg, opValueOrReg, opReg);
            break;
        case OP_MUL_INT_LIT16:
            info(opcode, "v%d = v%d * %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_DIV_INT_LIT16:
            info(opcode, "v%d = v%d / %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_REM_INT_LIT16:
            info(opcode, "v%d = v%d %% %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_ADD_INT_LIT16:
            info(opcode, "v%d = v%d + %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_AND_INT_LIT16:
            info(opcode, "v%d = v%d & %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_OR_INT_LIT16:
            info(opcode, "v%d = v%d | %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_XOR_INT_LIT16:
            info(opcode, "v%d = v%d ^ %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_AND_INT_LIT8:
            info(opcode, "v%d = v%d & %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_ADD_INT_LIT8:
            info(opcode, "v%d = v%d + %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_REM_INT_LIT8:
            info(opcode, "v%d = v%d %% %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_DIV_INT_LIT8:
            info(opcode, "v%d = v%d / %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_MUL_INT_LIT8:
            info(opcode, "v%d = v%d * %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_SHR_INT_LIT8:
            info(opcode, "v%d = v%d >> %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_SHL_INT_LIT8:
            info(opcode, "v%d = v%d << %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_USHR_INT_LIT8:
            info(opcode, "v%d = v%d >>> %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_OR_INT_LIT8:
            info(opcode, "v%d = v%d | %d", saveToReg, opReg, opValueOrReg);
            break;
        case OP_XOR_INT_LIT8:
            info(opcode, "v%d = v%d ^ %d", saveToReg, opReg, opValueOrReg);
            break;
        }
        super.visitInInsn(opcode, saveToReg, opReg, opValueOrReg);
    }

    @Override
    public void visitInitLocal(int... args) {
        int i = 0;
        if ((method.getAccessFlags() & Opcodes.ACC_STATIC) == 0) {
            int reg = args[i++];
            String type = Type.getType(method.getOwner()).getClassName();
            out.printf("%20s:v%d   //%s\n", "this", reg, type);
        }
        for (String type : method.getType().getParameterTypes()) {
            int reg = args[i++];
            type = Type.getType(type).getClassName();
            out.printf("%20s:v%d   //%s\n", "", reg, type);
        }
        super.visitInitLocal(args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitInsn(int)
     */
    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
        case OP_RETURN_VOID:
            info(opcode, "return");
            break;
        }
        super.visitInsn(opcode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int)
     */
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        switch (opcode) {
        case OP_GOTO:
        case OP_GOTO_16:
            info(opcode, "goto L%s", labelIndex(label));
            break;
        }
        super.visitJumpInsn(opcode, label);
    }

    public void visitJumpInsn(int opcode, Label label, int reg) {
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
        super.visitJumpInsn(opcode, label, reg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitJumpInsn(int, int, int, int)
     */
    @Override
    public void visitJumpInsn(int opcode, Label label, int reg1, int reg2) {
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
        super.visitJumpInsn(opcode, label, reg1, reg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLabel(int)
     */
    @Override
    public void visitLabel(Label label) {
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
        super.visitLabel(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLdcInsn(int, java.lang.Object, int)
     */
    @Override
    public void visitLdcInsn(int opcode, Object value, int reg) {
        if (value instanceof String)
            info(opcode, "v%d=\"%s\"", reg, value);
        else if (value instanceof Type) {
            info(opcode, "v%d=%s.class", reg, ((Type) value).getClassName());
        } else if (value instanceof Integer) {
            info(opcode, "v%d=0x%08x  // int:%d   float:%f", reg, value, value, Float.intBitsToFloat((Integer) value));
        } else if (value instanceof Long) {
            info(opcode, "v%d=0x%016x  // long:%d   double:%f", reg, value, value, Double.longBitsToDouble((Long) value));
        } else {
            info(opcode, "v%d=%s  //", reg, value);
        }
        super.visitLdcInsn(opcode, value, reg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLineNumber(int, int)
     */
    @Override
    public void visitLineNumber(int line, Label label) {
        super.visitLineNumber(line, label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLocalVariable(java.lang .String, java.lang.String,
     * java.lang.String, int, int, int)
     */
    @Override
    public void visitLocalVariable(String name, String type, String signature, Label start, Label end, int reg) {
        super.visitLocalVariable(name, type, signature, start, end, reg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitLookupSwitchInsn(int, int, int, int[], int[])
     */
    @Override
    public void visitLookupSwitchInsn(int opcode, int reg, Label label, int[] cases, Label[] label2) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < cases.length; i++) {
            info(-1, "case %d: goto L%s", cases[i], labelIndex(label2[i]));
        }
        info(-1, "default: goto L%s", labelIndex(label));
        super.visitLookupSwitchInsn(opcode, reg, label, cases, label2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitMethodInsn(int, pxb.android.dex2jar.Method, int[])
     */
    @Override
    public void visitMethodInsn(int opcode, Method method, int[] regs) {

        switch (opcode) {
        case OP_INVOKE_STATIC: {
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < method.getType().getParameterTypes().length; j++) {
                sb.append('v').append(regs[i++]).append(',');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (method.getType().getReturnType().equals("V")) {
                info(opcode, "%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(), method.toString());
            } else {
                info(opcode, "TEMP=%s.%s(%s)  //%s", c(method.getOwner()), method.getName(), sb.toString(), method.toString());

            }
        }
            break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_SUPER: {
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < method.getType().getParameterTypes().length; j++) {
                sb.append(',').append('v').append(regs[i++]);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            if (method.getType().getReturnType().equals("V")) {
                info(opcode, "v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());
            } else {
                info(opcode, "TEMP=v%d.%s(%s)  //%s", regs[0], method.getName(), sb.toString(), method.toString());

            }
        }
            break;
        }
        super.visitMethodInsn(opcode, method, regs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTableSwitchInsn(int, int, int, int, int, int[])
     */
    @Override
    public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, Label label, Label[] labels) {
        info(opcode, "switch(v%d)", reg);
        for (int i = 0; i < labels.length; i++) {
            info(opcode, "case %d: goto L%s", first_case + i, labelIndex(labels[i]));
        }
        info(opcode, "default: goto L%s", labelIndex(label));

        super.visitTableSwitchInsn(opcode, reg, first_case, last_case, label, labels);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTryCatch(int, int, int, java.lang.String)
     */
    @Override
    public void visitTryCatch(Label start, Label end, Label handler, String type) {
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
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int, java.lang.String, int)
     */
    @Override
    public void visitTypeInsn(int opcode, String type, int toReg) {
        switch (opcode) {
        case OP_NEW_INSTANCE:
            info(opcode, "v%d=NEW %s", toReg, type);
            break;
        case OP_CONST_CLASS:
            info(opcode, "v%d=%s.class", toReg, Type.getType(type).getClassName());
            break;
        case OP_CHECK_CAST:
            info(opcode, "v%d=(%s) v%d", toReg, Type.getType(type).getClassName(), toReg);
            break;
        }
        super.visitTypeInsn(opcode, type, toReg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitTypeInsn(int, java.lang.String, int, int)
     */
    @Override
    public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {
        switch (opcode) {
        case OP_INSTANCE_OF:
            info(opcode, "v%d=v%d instanceof %s", toReg, fromReg, Type.getType(type).getClassName());
            break;
        }
        super.visitTypeInsn(opcode, type, toReg, fromReg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexCodeAdapter#visitVarInsn(int, int)
     */
    @Override
    public void visitVarInsn(int opcode, int reg) {
        switch (opcode) {
        case OP_MOVE_RESULT_OBJECT:
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_EXCEPTION:
            info(opcode, "v%d=TEMP", reg);
            break;
        case OP_THROW:
            info(opcode, "throw v%d", reg);
            break;
        case OP_RETURN_OBJECT:
        case OP_RETURN:
        case OP_RETURN_WIDE:
            info(opcode, "return v%d", reg);
            break;
        case OP_MONITOR_ENTER:
            info(opcode, "lock v%d", reg);
            break;
        case OP_MONITOR_EXIT:
            info(opcode, "unlock v%d", reg);
            break;
        }
        super.visitVarInsn(opcode, reg);
    }

}
