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

import static com.googlecode.dex2jar.ir.Constant.nClass;
import static com.googlecode.dex2jar.ir.Constant.nInt;
import static com.googlecode.dex2jar.ir.Constant.nLong;
import static com.googlecode.dex2jar.ir.Constant.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAnd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCast;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCheckCast;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCmp;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCmpg;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCmpl;
import static com.googlecode.dex2jar.ir.expr.Exprs.nDiv;
import static com.googlecode.dex2jar.ir.expr.Exprs.nEq;
import static com.googlecode.dex2jar.ir.expr.Exprs.nExceptionRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nField;
import static com.googlecode.dex2jar.ir.expr.Exprs.nGe;
import static com.googlecode.dex2jar.ir.expr.Exprs.nGt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInstanceOf;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeInterface;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeSpecial;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeVirtual;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLe;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLength;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nMul;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNe;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNeg;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNew;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNot;
import static com.googlecode.dex2jar.ir.expr.Exprs.nOr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nParameterRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nRem;
import static com.googlecode.dex2jar.ir.expr.Exprs.nShl;
import static com.googlecode.dex2jar.ir.expr.Exprs.nShr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nStaticField;
import static com.googlecode.dex2jar.ir.expr.Exprs.nSub;
import static com.googlecode.dex2jar.ir.expr.Exprs.nThisRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nUshr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nXor;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIdentity;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLabel;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLock;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLookupSwitch;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturnVoid;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nTableSwitch;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nThrow;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nUnLock;

import java.util.Arrays;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3CodeAdapter implements DexCodeVisitor, Opcodes, DexOpcodes {

    private static LabelStmt toLabelStmt(Label label) {
        LabelStmt ls = (LabelStmt) label.info;
        if (ls == null) {
            ls = nLabel(label);
            label.info = ls;
        }
        return ls;
    }

    protected IrMethod irMethod;
    private StmtList list;
    private Local[] locals;
    /**
     * 函数调用的返回值保存的寄存器
     */
    private int tmp_reg;

    /**
     * @param mv
     */
    public V3CodeAdapter(Method method) {
        super();
        IrMethod irMethod = new IrMethod();
        irMethod.access = method.getAccessFlags();
        irMethod.args = Type.getArgumentTypes(method.getType().getDesc());
        irMethod.ret = Type.getType(method.getType().getReturnType());
        irMethod.owner = Type.getType(method.getOwner());
        irMethod.name = method.getName();
        this.list = irMethod.stmts;
        this.irMethod = irMethod;
    }

    @Override
    public void visitArguments(int total, int[] args) {
        Local[] locals = new Local[total + 1];
        this.locals = locals;
        this.tmp_reg = total;
        {
            int i = 0;
            if ((irMethod.access & ACC_STATIC) == 0) {
                Local _this = nLocal("this", this.irMethod.owner);
                list.add(nIdentity(_this, nThisRef(this.irMethod.owner)));
                locals[args[i]] = _this;
                i++;
            }
            int j = 0;
            for (; i < args.length; i++, j++) {
                Local _arg = nLocal("arg_" + args[i], this.irMethod.args[j]);
                list.add(nIdentity(_arg, nParameterRef(this.irMethod.args[j], j)));
                locals[args[i]] = _arg;
            }
        }
        for (int i = 0; i < locals.length; i++) {
            if (locals[i] == null) {
                locals[i] = nLocal("a" + i, null);
            }
        }
    }

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg) {
        switch (opcode) {
        case OP_APUT:
            list.add(nAssign(nArray(locals[arrayReg], locals[indexReg]), locals[formOrToReg]));
            break;
        case OP_AGET:
            list.add(nAssign(locals[formOrToReg], nArray(locals[arrayReg], locals[indexReg])));
            break;
        }
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int constant) {
        Local dist = locals[distReg];
        Local a = locals[srcReg];
        Constant b = Constant.nInt(constant);
        switch (opcode) {
        case OP_ADD_INT_LIT_X:
            list.add(nAssign(dist, nAdd(a, b)));
            break;
        case OP_RSUB_INT_LIT_X:
            list.add(nAssign(dist, nSub(b, a)));
            break;
        case OP_MUL_INT_LIT_X:
            list.add(nAssign(dist, nMul(a, b)));
            break;
        case OP_DIV_INT_LIT_X:
            list.add(nAssign(dist, nDiv(a, b)));
            break;
        case OP_REM_INT_LIT_X:
            list.add(nAssign(dist, nRem(a, b)));
            break;
        case OP_AND_INT_LIT_X:
            list.add(nAssign(dist, nAnd(a, b)));
            break;
        case OP_OR_INT_LIT_X:
            list.add(nAssign(dist, nOr(a, b)));
            break;
        case OP_XOR_INT_LIT_X:
            list.add(nAssign(dist, nXor(a, b)));
            break;
        case OP_SHL_INT_LIT_X:
            list.add(nAssign(dist, nShl(a, b)));
            break;
        case OP_SHR_INT_LIT_X:
            list.add(nAssign(dist, nShr(a, b)));
            break;
        case OP_USHR_INT_LIT_X:
            list.add(nAssign(dist, nUshr(a, b)));
            break;

        }
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2) {
        Local dist = locals[toReg];
        Local a = locals[r1];
        Local b = locals[r2];
        switch (opcode) {
        case OP_ADD_INT:
        case OP_ADD_FLOAT:
        case OP_ADD_DOUBLE:
        case OP_ADD_LONG:
            list.add(nAssign(dist, nAdd(a, b)));
            break;
        case OP_SUB_INT:
        case OP_SUB_FLOAT:
        case OP_SUB_LONG:
        case OP_SUB_DOUBLE:
            list.add(nAssign(dist, nSub(a, b)));
            break;
        case OP_MUL_INT:
        case OP_MUL_LONG:
        case OP_MUL_DOUBLE:
        case OP_MUL_FLOAT:
            list.add(nAssign(dist, nMul(a, b)));
            break;
        case OP_DIV_INT:
        case OP_DIV_DOUBLE:
        case OP_DIV_LONG:
        case OP_DIV_FLOAT:
            list.add(nAssign(dist, nDiv(a, b)));
            break;
        case OP_REM_INT:
        case OP_REM_LONG:
        case OP_REM_DOUBLE:
        case OP_REM_FLOAT:
            list.add(nAssign(dist, nRem(a, b)));
            break;
        case OP_AND_INT:
        case OP_AND_LONG:
            list.add(nAssign(dist, nAnd(a, b)));
            break;
        case OP_OR_INT:
        case OP_OR_LONG:
            list.add(nAssign(dist, nOr(a, b)));
            break;
        case OP_XOR_INT:
        case OP_XOR_LONG:
            list.add(nAssign(dist, nXor(a, b)));
            break;
        case OP_SHL_INT:
        case OP_SHL_LONG:
            list.add(nAssign(dist, nShl(a, b)));
            break;
        case OP_SHR_INT:
        case OP_SHR_LONG:
            list.add(nAssign(dist, nShr(a, b)));
            break;
        case OP_USHR_INT:
        case OP_USHR_LONG:
            list.add(nAssign(dist, nUshr(a, b)));
            break;
        }
    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        switch (opcode) {
        case OP_INSTANCE_OF:
            list.add(nAssign(locals[a], nInstanceOf(locals[b], Type.getType(type))));
            break;
        case OP_NEW_ARRAY:
            list.add(nAssign(locals[a], nNewArray(Type.getType(type.substring(1)), locals[b])));
            break;
        }
    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {
        switch (opcode) {
        case OP_CHECK_CAST:
            list.add(nAssign(locals[saveTo], nCheckCast(locals[saveTo], Type.getType(type))));
            break;
        case OP_NEW_INSTANCE:
            list.add(nAssign(locals[saveTo], nNew(Type.getType(type))));
            break;
        }

    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC) {
        Local dist = locals[distReg];
        Local a = locals[bB];
        Local b = locals[cC];
        switch (opcode) {
        case OP_CMPL_FLOAT:
        case OP_CMPL_DOUBLE:
            list.add(nAssign(dist, nCmpl(a, b)));
            break;
        case OP_CMPG_FLOAT:
        case OP_CMPG_DOUBLE:
            list.add(nAssign(dist, nCmpg(a, b)));
            break;
        case OP_CMP_LONG:
            list.add(nAssign(dist, nCmp(a, b)));
            break;
        }
    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value) {
        switch (opcode) {
        case OP_CONST:
            list.add(nAssign(locals[toReg], nInt((Integer) value)));
            break;
        case OP_CONST_WIDE:
            list.add(nAssign(locals[toReg], nLong((Long) value)));
            break;
        case OP_CONST_STRING:
            list.add(nAssign(locals[toReg], nString((String) value)));
            break;
        case OP_CONST_CLASS:
            list.add(nAssign(locals[toReg], nClass(Type.getType((String) value))));
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeVisitor#visitEnd()
     */
    public void visitEnd() {
        irMethod.locals.addAll(Arrays.asList(this.locals));
        this.locals = null;
        for (Stmt stmt : list) {// clean label.info
            if (stmt.st == ST.LABEL) {
                ((LabelStmt) stmt).label.info = null;
            }
        }

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field) {
        switch (opcode) {

        case OP_SGET:
            list.add(nAssign(locals[fromOrToReg],
                    nStaticField(Type.getType(field.getOwner()), field.getName(), Type.getType(field.getType()))));
            break;
        case OP_SPUT:
            list.add(nAssign(
                    nStaticField(Type.getType(field.getOwner()), field.getName(), Type.getType(field.getType())),
                    locals[fromOrToReg]));
            break;
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field) {
        switch (opcode) {

        case OP_IGET:
            list.add(nAssign(
                    locals[fromOrToReg],
                    nField(locals[objReg], Type.getType(field.getOwner()), field.getName(),
                            Type.getType(field.getType()))));
            break;
        case OP_IPUT:
            list.add(nAssign(
                    nField(locals[objReg], Type.getType(field.getOwner()), field.getName(),
                            Type.getType(field.getType())), locals[fromOrToReg]));
            break;
        }
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        Local array = locals[aA];
        Type type = null;
        switch (elemWidth) {
        case 1:
            type = Type.BYTE_TYPE;
            break;
        case 2:
            type = Type.SHORT_TYPE;
            break;
        case 4:
            type = Type.INT_TYPE;
            break;
        case 8:
            type = Type.LONG_TYPE;
            break;
        }

        for (int i = 0; i < initLength; i++) {
            list.add(nAssign(nArray(array, Constant.nInt(i)), Constant.n(type, values[i])));
        }
    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        Local array = locals[tmp_reg];
        Type elem = Type.getType(type).getElementType();
        list.add(nAssign(array, nNewArray(elem, Constant.nInt(args.length))));
        for (int i = 0; i < args.length; i++) {
            list.add(nAssign(nArray(array, Constant.nInt(i)), locals[args[i]]));
        }
    }

    @Override
    public void visitJumpStmt(int opcode, int r1, int r2, Label label) {
        Local a = locals[r1];
        Local b = locals[r2];
        LabelStmt ls = toLabelStmt(label);
        switch (opcode) {
        case OP_IF_EQ:
            list.add(nIf(nEq(a, b), ls));
            break;
        case OP_IF_NE:
            list.add(nIf(nNe(a, b), ls));
            break;
        case OP_IF_LT:
            list.add(nIf(nLt(a, b), ls));
            break;
        case OP_IF_GE:
            list.add(nIf(nGe(a, b), ls));
            break;
        case OP_IF_GT:
            list.add(nIf(nGt(a, b), ls));
            break;
        case OP_IF_LE:
            list.add(nIf(nLe(a, b), ls));
            break;
        }
    }

    @Override
    public void visitJumpStmt(int opcode, int reg, Label label) {
        Local a = locals[reg];
        Value b = nInt(0);
        LabelStmt ls = toLabelStmt(label);
        switch (opcode) {
        case OP_IF_EQZ:
            list.add(nIf(nEq(a, b), ls));
            break;
        case OP_IF_NEZ:
            list.add(nIf(nNe(a, b), ls));
            break;
        case OP_IF_LTZ:
            list.add(nIf(nLt(a, b), ls));
            break;
        case OP_IF_GEZ:
            list.add(nIf(nGe(a, b), ls));
            break;
        case OP_IF_GTZ:
            list.add(nIf(nGt(a, b), ls));
            break;
        case OP_IF_LEZ:
            list.add(nIf(nLe(a, b), ls));
            break;
        }
    }

    @Override
    public void visitJumpStmt(int opcode, Label label) {

        list.add(nGoto(toLabelStmt(label)));
    }

    @Override
    public void visitLabel(Label label) {
        list.add(toLabelStmt(label));
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, Label label, int[] cases, Label[] labels) {
        LabelStmt[] lss = new LabelStmt[cases.length];
        for (int i = 0; i < cases.length; i++) {
            lss[i] = toLabelStmt(labels[i]);
        }
        list.add(nLookupSwitch(locals[aA], cases, lss, toLabelStmt(label)));
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        Value[] vs = new Value[args.length];
        for (int j = 0; j < vs.length; j++) {
            vs[j] = locals[args[j]];
        }
        Value saveTo = locals[tmp_reg];
        Value invoke = null;
        switch (opcode) {
        case OP_INVOKE_VIRTUAL:
            invoke = nInvokeVirtual(vs, Type.getType(method.getOwner()), method.getName(),
                    Type.getArgumentTypes(method.getType().getDesc()), Type.getType(method.getType().getReturnType()));
            break;
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
            invoke = nInvokeSpecial(vs, Type.getType(method.getOwner()), method.getName(),
                    Type.getArgumentTypes(method.getType().getDesc()), Type.getType(method.getType().getReturnType()));
            break;
        case OP_INVOKE_STATIC:
            invoke = nInvokeStatic(vs, Type.getType(method.getOwner()), method.getName(),
                    Type.getArgumentTypes(method.getType().getDesc()), Type.getType(method.getType().getReturnType()));
            break;
        case OP_INVOKE_INTERFACE:
            invoke = nInvokeInterface(vs, Type.getType(method.getOwner()), method.getName(),
                    Type.getArgumentTypes(method.getType().getDesc()), Type.getType(method.getType().getReturnType()));
            break;
        }
        list.add(nAssign(saveTo, invoke));
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_MONITOR_ENTER:
            list.add(nLock(locals[reg]));
            break;
        case OP_MONITOR_EXIT:
            list.add(nUnLock(locals[reg]));
            break;
        }
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg) {
        switch (opcode) {
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_RESULT_OBJECT:
            list.add(nAssign(locals[toReg], locals[tmp_reg]));
            break;
        case OP_MOVE_EXCEPTION:
            list.add(nIdentity(locals[toReg], nExceptionRef(Type.getType(Throwable.class))));
            break;
        }
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg) {
        list.add(nAssign(locals[toReg], locals[fromReg]));
    }

    @Override
    public void visitReturnStmt(int opcode) {
        list.add(nReturnVoid());
    }

    @Override
    public void visitReturnStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_THROW:
            list.add(nThrow(locals[reg]));
            break;
        case OP_RETURN:
            list.add(nReturn(locals[reg]));
            break;
        }

    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, Label label, int first_case, int last_case, Label[] labels) {
        LabelStmt[] lss = new LabelStmt[labels.length];
        for (int i = 0; i < labels.length; i++) {
            lss[i] = toLabelStmt(labels[i]);
        }
        list.add(nTableSwitch(locals[aA], first_case, last_case, lss, toLabelStmt(label)));

    }

    @Override
    public void visitTryCatch(Label start, Label end, Label handler, String type) {
        irMethod.traps.add(new Trap(toLabelStmt(start), toLabelStmt(end), toLabelStmt(handler), type == null ? null
                : Type.getType(type)));
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg) {
        Value dist = locals[toReg];
        Value src = locals[fromReg];
        switch (opcode) {
        case OP_ARRAY_LENGTH:
            list.add(nAssign(dist, nLength(src)));
            break;
        case OP_NEG_INT:
        case OP_NEG_LONG:
        case OP_NEG_FLOAT:
        case OP_NEG_DOUBLE:
            list.add(nAssign(dist, nNeg(src)));
            break;
        case OP_NOT_INT:
        case OP_NOT_LONG:
            list.add(nAssign(dist, nNot(src)));
            break;
        case OP_INT_TO_LONG:
        case OP_FLOAT_TO_LONG:
        case OP_DOUBLE_TO_LONG:

            list.add(nAssign(dist, nCast(src, Type.LONG_TYPE)));
            break;
        case OP_INT_TO_FLOAT:
        case OP_DOUBLE_TO_FLOAT:
        case OP_LONG_TO_FLOAT:
            list.add(nAssign(dist, nCast(src, Type.FLOAT_TYPE)));
            break;
        case OP_INT_TO_DOUBLE:
        case OP_FLOAT_TO_DOUBLE:
        case OP_LONG_TO_DOUBLE:
            list.add(nAssign(dist, nCast(src, Type.DOUBLE_TYPE)));
            break;
        case OP_LONG_TO_INT:
        case OP_DOUBLE_TO_INT:
        case OP_FLOAT_TO_INT:
            list.add(nAssign(dist, nCast(src, Type.INT_TYPE)));
            break;
        case OP_INT_TO_BYTE:
            list.add(nAssign(dist, nCast(src, Type.BYTE_TYPE)));
            break;
        case OP_INT_TO_CHAR:
            list.add(nAssign(dist, nCast(src, Type.CHAR_TYPE)));
            break;
        case OP_INT_TO_SHORT:
            list.add(nAssign(dist, nCast(src, Type.SHORT_TYPE)));
            break;

        }
    }
}
