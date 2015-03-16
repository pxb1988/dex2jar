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
package com.googlecode.d2j.dex;

import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAnd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nArrayValue;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCast;
import static com.googlecode.dex2jar.ir.expr.Exprs.nCheckCast;
import static com.googlecode.dex2jar.ir.expr.Exprs.nDCmpg;
import static com.googlecode.dex2jar.ir.expr.Exprs.nDCmpl;
import static com.googlecode.dex2jar.ir.expr.Exprs.nDiv;
import static com.googlecode.dex2jar.ir.expr.Exprs.nExceptionRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nFCmpg;
import static com.googlecode.dex2jar.ir.expr.Exprs.nFCmpl;
import static com.googlecode.dex2jar.ir.expr.Exprs.nField;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInstanceOf;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeInterface;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeNew;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeSpecial;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeVirtual;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLCmp;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLength;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLong;
import static com.googlecode.dex2jar.ir.expr.Exprs.nMul;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNeg;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNew;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNot;
import static com.googlecode.dex2jar.ir.expr.Exprs.nOr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nRem;
import static com.googlecode.dex2jar.ir.expr.Exprs.nShl;
import static com.googlecode.dex2jar.ir.expr.Exprs.nShr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nStaticField;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.nSub;
import static com.googlecode.dex2jar.ir.expr.Exprs.nType;
import static com.googlecode.dex2jar.ir.expr.Exprs.nUshr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nXor;
import static com.googlecode.dex2jar.ir.stmt.Stmts.*;

import java.util.*;

import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.TryCatchNode;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.FilledNewArrayStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.node.insn.MethodStmtNode;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import com.googlecode.dex2jar.ir.TypeClass;
import org.objectweb.asm.Opcodes;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Dex2IrAdapter extends DexCodeVisitor implements Opcodes, DexConstants {

    protected IrMethod irMethod;
    private Method method;
    private boolean isStatic;
    private StmtList list;
    private Local[] locals;
    Map<DexLabel, LabelStmt> labelStmtMap = new HashMap<>();
    /**
     * 函数调用的返回值保存的寄存器
     */
    private Local tmpLocal;

    /**
     * @param isStatic
     * @param method
     */
    public Dex2IrAdapter(boolean isStatic, Method method) {
        super();
        IrMethod irMethod = new IrMethod();
        irMethod.args = method.getParameterTypes();
        irMethod.ret = method.getReturnType();
        irMethod.owner = method.getOwner();
        irMethod.name = method.getName();
        irMethod.isStatic = isStatic;
        this.irMethod = irMethod;
        this.list = irMethod.stmts;
        this.irMethod = irMethod;
        this.method = method;
        this.isStatic = isStatic;
    }

    private LabelStmt toLabelStmt(DexLabel label) {
        LabelStmt ls = labelStmtMap.get(label);
        if (ls == null) {
            ls = new LabelStmt();
            labelStmtMap.put(label, ls);
        }
        return ls;
    }

    static int countParameterRegisters(Method m, boolean isStatic) {
        int a = isStatic ? 0 : 1;
        for (String t : m.getParameterTypes()) {
            switch (t.charAt(0)) {
            case 'J':
            case 'D':
                a += 2;
                break;
            default:
                a += 1;
                break;
            }
        }
        return a;
    }

    void x(Stmt stmt) {
        list.add(stmt);
    }

    @Override
    public void visitRegister(int total) {
        Local[] locals = new Local[total];
        this.locals = locals;
        this.tmpLocal = new Local(total);
        for (int i = 0; i < locals.length; i++) {
            locals[i] = new Local(i);
        }
        int nextReg = total - countParameterRegisters(method, isStatic);
        int nextReg0 = nextReg;
        if (!isStatic) {// is not static
            x(Stmts.nIdentity(locals[nextReg], Exprs.nThisRef(method.getOwner())));
            nextReg++;
        }
        String[] args = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            String t = args[i];
            x(Stmts.nIdentity(locals[nextReg], Exprs.nParameterRef(t, i)));
            nextReg++;
            if (t.equals("J") || t.equals("D")) {
                nextReg++;
            }
        }
        // simple fix for issue 219, init all tmp register to 0 at the start of insn.
        for (int i = 0; i < nextReg0; i++) {
            x(Stmts.nAssign(locals[i], nInt(0)));
        }
        x(Stmts.nAssign(tmpLocal, nInt(0)));
    }

    @Override
    public void visitStmt2R1N(Op op, int a, int b, int content) {
        Local va = locals[a];
        Local vb = locals[b];
        Value to;
        switch (op) {
        case ADD_INT_LIT16:
        case ADD_INT_LIT8:
            to = nAdd(vb, nInt(content), "I");
            break;
        case RSUB_INT_LIT8:
        case RSUB_INT://
            to = nSub(nInt(content), vb, "I");
            break;
        case MUL_INT_LIT8:
        case MUL_INT_LIT16:
            to = nMul(vb, nInt(content), "I");
            break;
        case DIV_INT_LIT16:
        case DIV_INT_LIT8:
            to = nDiv(vb, nInt(content), "I");
            break;
        case REM_INT_LIT16:
        case REM_INT_LIT8:
            to = nRem(vb, nInt(content), "I");
            break;
        case AND_INT_LIT16:
        case AND_INT_LIT8:
            to = nAnd(vb, nInt(content), content < 0 || content > 1 ? "I" : TypeClass.ZI.name);
            break;
        case OR_INT_LIT16:
        case OR_INT_LIT8:
            to = nOr(vb, nInt(content), content < 0 || content > 1 ? "I" : TypeClass.ZI.name);
            break;
        case XOR_INT_LIT16:
        case XOR_INT_LIT8:
            to = nXor(vb, nInt(content), content < 0 || content > 1 ? "I" : TypeClass.ZI.name);
            break;
        case SHL_INT_LIT8:
            to = nShl(vb, nInt(content), "I");
            break;
        case SHR_INT_LIT8:
            to = nShr(vb, nInt(content), "I");
            break;
        case USHR_INT_LIT8:
            to = nUshr(vb, nInt(content), "I");
            break;
        default:
            throw new RuntimeException();
        }
        x(nAssign(va, to));
    }

    @Override
    public void visitStmt3R(Op op, int a, int b, int c) {
        Value va = locals[a];
        Value vb = locals[b];
        Value vc = locals[c];
        switch (op) {
        case APUT:
            x(nAssign(nArray(vb, vc, TypeClass.IF.name), va));
            break;
        case APUT_BOOLEAN:
            x(nAssign(nArray(vb, vc, "Z"), va));
            break;
        case APUT_BYTE:
            x(nAssign(nArray(vb, vc, "B"), va));
            break;
        case APUT_CHAR:
            x(nAssign(nArray(vb, vc, "C"), va));
            break;
        case APUT_OBJECT:
            x(nAssign(nArray(vb, vc, "L"), va));
            break;
        case APUT_SHORT:
            x(nAssign(nArray(vb, vc, "S"), va));
            break;
        case APUT_WIDE:
            x(nAssign(nArray(vb, vc, TypeClass.JD.name), va));
            break;
        case AGET:
            x(nAssign(va, nArray(vb, vc, TypeClass.IF.name)));
            break;
        case AGET_BOOLEAN:
            x(nAssign(va, nArray(vb, vc, "Z")));
            break;
        case AGET_BYTE:
            x(nAssign(va, nArray(vb, vc, "B")));
            break;
        case AGET_CHAR:
            x(nAssign(va, nArray(vb, vc, "C")));
            break;
        case AGET_OBJECT:
            x(nAssign(va, nArray(vb, vc, "L")));
            break;
        case AGET_SHORT:
            x(nAssign(va, nArray(vb, vc, "S")));
            break;
        case AGET_WIDE:
            x(nAssign(va, nArray(vb, vc, TypeClass.JD.name)));
            break;
        case CMP_LONG:
            x(nAssign(va, nLCmp(vb, vc)));
            break;
        case CMPG_DOUBLE:
            x(nAssign(va, nDCmpg(vb, vc)));
            break;
        case CMPG_FLOAT:
            x(nAssign(va, nFCmpg(vb, vc)));
            break;
        case CMPL_DOUBLE:
            x(nAssign(va, nDCmpl(vb, vc)));
            break;
        case CMPL_FLOAT:
            x(nAssign(va, nFCmpl(vb, vc)));
            break;
        case ADD_DOUBLE:
            x(nAssign(va, nAdd(vb, vc, "D")));
            break;
        case ADD_FLOAT:
            x(nAssign(va, nAdd(vb, vc, "F")));
            break;
        case ADD_INT:
            x(nAssign(va, nAdd(vb, vc, "I")));
            break;
        case ADD_LONG:
            x(nAssign(va, nAdd(vb, vc, "J")));
            break;
        case SUB_DOUBLE:
            x(nAssign(va, nSub(vb, vc, "D")));
            break;
        case SUB_FLOAT:
            x(nAssign(va, nSub(vb, vc, "F")));
            break;
        // case RSUB_INT:
        // x(nAssign(va, nSub(vc, vb, "I")));
        // break;
        case SUB_INT:
            x(nAssign(va, nSub(vb, vc, "I")));
            break;
        case SUB_LONG:
            x(nAssign(va, nSub(vb, vc, "J")));
            break;
        case MUL_DOUBLE:
            x(nAssign(va, nMul(vb, vc, "D")));
            break;
        case MUL_FLOAT:
            x(nAssign(va, nMul(vb, vc, "F")));
            break;
        case MUL_INT:
            x(nAssign(va, nMul(vb, vc, "I")));
            break;
        case MUL_LONG:
            x(nAssign(va, nMul(vb, vc, "J")));
            break;
        case DIV_DOUBLE:
            x(nAssign(va, nDiv(vb, vc, "D")));
            break;
        case DIV_FLOAT:
            x(nAssign(va, nDiv(vb, vc, "F")));
            break;
        case DIV_INT:
            x(nAssign(va, nDiv(vb, vc, "I")));
            break;
        case DIV_LONG:
            x(nAssign(va, nDiv(vb, vc, "J")));
            break;
        case REM_DOUBLE:
            x(nAssign(va, nRem(vb, vc, "D")));
            break;
        case REM_FLOAT:
            x(nAssign(va, nRem(vb, vc, "F")));
            break;
        case REM_INT:
            x(nAssign(va, nRem(vb, vc, "I")));
            break;
        case REM_LONG:
            x(nAssign(va, nRem(vb, vc, "J")));
            break;
        case AND_INT:
            x(nAssign(va, nAnd(vb, vc, TypeClass.ZI.name)));
            break;
        case AND_LONG:
            x(nAssign(va, nAnd(vb, vc, "J")));
            break;
        case OR_INT:
            x(nAssign(va, nOr(vb, vc, TypeClass.ZI.name)));
            break;
        case OR_LONG:
            x(nAssign(va, nOr(vb, vc, "J")));
            break;
        case XOR_INT:
            x(nAssign(va, nXor(vb, vc, TypeClass.ZI.name)));
            break;
        case XOR_LONG:
            x(nAssign(va, nXor(vb, vc, "J")));
            break;
        case SHL_INT:
            x(nAssign(va, nShl(vb, vc, "I")));
            break;
        case SHL_LONG:
            x(nAssign(va, nShl(vb, vc, "J")));
            break;
        case SHR_INT:
            x(nAssign(va, nShr(vb, vc, "I")));
            break;
        case SHR_LONG:
            x(nAssign(va, nShr(vb, vc, "J")));
            break;
        case USHR_INT:
            x(nAssign(va, nUshr(vb, vc, "I")));
            break;
        case USHR_LONG:
            x(nAssign(va, nUshr(vb, vc, "J")));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitTypeStmt(Op op, int a, int b, String type) {
        switch (op) {
        case INSTANCE_OF:
            list.add(nAssign(locals[a], nInstanceOf(locals[b], type)));
            break;
        case NEW_ARRAY:
            list.add(nAssign(locals[a], nNewArray(type.substring(1), locals[b])));
            break;
        case CHECK_CAST:
            list.add(nAssign(locals[a], nCheckCast(locals[a], type)));
            break;
        case NEW_INSTANCE:
            list.add(nAssign(locals[a], nNew(type)));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitFillArrayDataStmt(Op op, int ra, Object array) {
        x(nFillArrayData(locals[ra], nArrayValue(array)));
    }

    @Override
    public void visitConstStmt(Op op, int toReg, Object value) {
        switch (op) {
        case CONST:
        case CONST_16:
        case CONST_4:
        case CONST_HIGH16:
            x(nAssign(locals[toReg], nInt((Integer) value)));
            break;
        case CONST_WIDE:
        case CONST_WIDE_16:
        case CONST_WIDE_32:
        case CONST_WIDE_HIGH16:
            x(nAssign(locals[toReg], nLong((Long) value)));
            break;
        case CONST_CLASS:
            x(nAssign(locals[toReg], nType(((DexType) value).desc)));
            break;
        case CONST_STRING:
        case CONST_STRING_JUMBO:
            x(nAssign(locals[toReg], nString((String) value)));
            break;
        default:
            throw new RuntimeException();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexCodeVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        irMethod.locals.addAll(Arrays.asList(this.locals));
        irMethod.locals.add(tmpLocal);
        this.locals = null;
    }

    @Override
    public void visitFieldStmt(Op op, int a, int b, Field field) {
        switch (op) {
        case IGET:
        case IGET_BOOLEAN:
        case IGET_BYTE:
        case IGET_CHAR:
        case IGET_OBJECT:
        case IGET_SHORT:
        case IGET_WIDE:
            list.add(nAssign(locals[a], nField(locals[b], field.getOwner(), field.getName(), field.getType())));
            break;
        case IPUT:
        case IPUT_BOOLEAN:
        case IPUT_BYTE:
        case IPUT_CHAR:
        case IPUT_OBJECT:
        case IPUT_SHORT:
        case IPUT_WIDE:
            list.add(nAssign(nField(locals[b], field.getOwner(), field.getName(), field.getType()), locals[a]));
            break;
        case SGET:
        case SGET_BOOLEAN:
        case SGET_BYTE:
        case SGET_CHAR:
        case SGET_OBJECT:
        case SGET_SHORT:
        case SGET_WIDE:
            list.add(nAssign(locals[a], nStaticField(field.getOwner(), field.getName(), field.getType())));
            break;
        case SPUT:
        case SPUT_BOOLEAN:
        case SPUT_BYTE:
        case SPUT_CHAR:
        case SPUT_OBJECT:
        case SPUT_SHORT:
        case SPUT_WIDE:
            list.add(nAssign(nStaticField(field.getOwner(), field.getName(), field.getType()), locals[a]));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitFilledNewArrayStmt(Op opc, int[] args, String type) {
        Local array = tmpLocal;
        String elem = type.substring(1);
        list.add(nAssign(array, nNewArray(elem, nInt(args.length))));
        for (int i = 0; i < args.length; i++) {
            list.add(nAssign(nArray(array, nInt(i), elem), locals[args[i]]));
        }
    }

    @Override
    public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
        switch (op) {
        case GOTO:
        case GOTO_16:
        case GOTO_32:
            x(nGoto(toLabelStmt(label)));
            break;
        case IF_EQ:
            x(nIf(Exprs.nEq(locals[a], locals[b], TypeClass.ZIL.name), toLabelStmt(label)));
            break;
        case IF_GE:
            x(nIf(Exprs.nGe(locals[a], locals[b], "I"), toLabelStmt(label)));
            break;
        case IF_GT:
            x(nIf(Exprs.nGt(locals[a], locals[b], "I"), toLabelStmt(label)));
            break;
        case IF_LE:
            x(nIf(Exprs.nLe(locals[a], locals[b], "I"), toLabelStmt(label)));
            break;
        case IF_LT:
            x(nIf(Exprs.nLt(locals[a], locals[b], "I"), toLabelStmt(label)));
            break;
        case IF_NE:
            x(nIf(Exprs.nNe(locals[a], locals[b], TypeClass.ZIL.name), toLabelStmt(label)));
            break;
        case IF_EQZ:
            x(nIf(Exprs.nEq(locals[a], nInt(0), TypeClass.ZIL.name), toLabelStmt(label)));
            break;
        case IF_GEZ:
            x(nIf(Exprs.nGe(locals[a], nInt(0), "I"), toLabelStmt(label)));
            break;
        case IF_GTZ:
            x(nIf(Exprs.nGt(locals[a], nInt(0), "I"), toLabelStmt(label)));
            break;
        case IF_LEZ:
            x(nIf(Exprs.nLe(locals[a], nInt(0), "I"), toLabelStmt(label)));
            break;
        case IF_LTZ:
            x(nIf(Exprs.nLt(locals[a], nInt(0), "I"), toLabelStmt(label)));
            break;
        case IF_NEZ:
            x(nIf(Exprs.nNe(locals[a], nInt(0), TypeClass.ZIL.name), toLabelStmt(label)));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitLabel(DexLabel label) {
        list.add(toLabelStmt(label));
    }

    @Override
    public void visitSparseSwitchStmt(Op op, int aA, int[] cases, DexLabel[] labels) {
        LabelStmt[] lss = new LabelStmt[cases.length];
        for (int i = 0; i < cases.length; i++) {
            lss[i] = toLabelStmt(labels[i]);
        }
        LabelStmt d = new LabelStmt();
        x(nLookupSwitch(locals[aA], cases, lss, d));
        x(d);
    }

    @Override
    public void visitMethodStmt(Op op, int[] args, Method method) {
        Value[] vs;
        if (args.length > 0) {
            int i = 0;
            List<Local> ps = new ArrayList<Local>(args.length);
            if (op == Op.INVOKE_STATIC || op == Op.INVOKE_STATIC_RANGE) {
                ;
            } else {
                ps.add(locals[args[i]]);
                i++;
            }
            for (String t : method.getParameterTypes()) {
                ps.add(locals[args[i]]);
                if (t.equals("J") || t.equals("D")) {
                    i += 2;
                } else {
                    i++;
                }
            }
            vs = ps.toArray(new Value[ps.size()]);
        } else {
            vs = new Value[0];
        }

        Value invoke = null;
        switch (op) {
        case INVOKE_VIRTUAL_RANGE:
        case INVOKE_VIRTUAL:
            invoke = nInvokeVirtual(vs, method.getOwner(), method.getName(), method.getParameterTypes(),
                    method.getReturnType());
            break;
        case INVOKE_SUPER_RANGE:
        case INVOKE_DIRECT_RANGE:
        case INVOKE_SUPER:
        case INVOKE_DIRECT:
            invoke = nInvokeSpecial(vs, method.getOwner(), method.getName(), method.getParameterTypes(),
                    method.getReturnType());
            break;
        case INVOKE_STATIC_RANGE:
        case INVOKE_STATIC:
            invoke = nInvokeStatic(vs, method.getOwner(), method.getName(), method.getParameterTypes(),
                    method.getReturnType());
            break;
        case INVOKE_INTERFACE_RANGE:
        case INVOKE_INTERFACE:
            invoke = nInvokeInterface(vs, method.getOwner(), method.getName(), method.getParameterTypes(),
                    method.getReturnType());
            break;
        default:
            throw new RuntimeException();
        }
        if ("V".equals(method.getReturnType())) {
            x(nVoidInvoke(invoke));
        } else {
            x(nAssign(tmpLocal, invoke));
        }
    }

    @Override
    public void visitStmt1R(Op op, int reg) {
        Local va = locals[reg];
        switch (op) {
        case MONITOR_ENTER:
            x(nLock(va));
            break;
        case MONITOR_EXIT:
            x(nUnLock(va));
            break;
        case RETURN:
        case RETURN_WIDE:
        case RETURN_OBJECT:
            x(nReturn(va));
            break;
        case THROW:
            x(nThrow(va));
            break;
        case MOVE_RESULT:
        case MOVE_RESULT_WIDE:
        case MOVE_RESULT_OBJECT:
            if (lastIsInvokeOrFilledNewArray) { // right position
                x(nAssign(va, tmpLocal));
            } else { // wrong position, replace with throw new RuntimeExceptoin("...");
                System.err.println("WARN: find wrong position of " + op + " in method " + method);
                x(nThrow(nInvokeNew(new Value[] { nString("d2j: wrong position of " + op) },
                        new String[] { "Ljava/lang/String;" }, "Ljava/lang/RuntimeException;")));
            }

            break;
        case MOVE_EXCEPTION:
            x(nIdentity(va, nExceptionRef("Ljava/lang/Throwable;")));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitStmt2R(Op op, int a, int b) {
        Local va = locals[a];
        Local vb = locals[b];
        Value to = null;
        switch (op) {
        case MOVE:
        case MOVE_16:
        case MOVE_FROM16:
        case MOVE_OBJECT:
        case MOVE_OBJECT_16:
        case MOVE_OBJECT_FROM16:
        case MOVE_WIDE:
        case MOVE_WIDE_FROM16:
        case MOVE_WIDE_16:
            to = vb;
            break;
        case ARRAY_LENGTH:
            to = nLength(vb);
            break;
        case ADD_DOUBLE_2ADDR:
            to = nAdd(va, vb, "D");
            break;
        case ADD_FLOAT_2ADDR:
            to = nAdd(va, vb, "F");
            break;
        case ADD_INT_2ADDR:
            to = nAdd(va, vb, "I");
            break;
        case ADD_LONG_2ADDR:
            to = nAdd(va, vb, "J");
            break;
        case SUB_DOUBLE_2ADDR:
            to = nSub(va, vb, "D");
            break;
        case SUB_FLOAT_2ADDR:
            to = nSub(va, vb, "F");
            break;
        case SUB_INT_2ADDR:
            to = nSub(va, vb, "I");
            break;
        case SUB_LONG_2ADDR:
            to = nSub(va, vb, "J");
            break;
        case MUL_DOUBLE_2ADDR:
            to = nMul(va, vb, "D");
            break;
        case MUL_FLOAT_2ADDR:
            to = nMul(va, vb, "F");
            break;
        case MUL_INT_2ADDR:
            to = nMul(va, vb, "I");
            break;
        case MUL_LONG_2ADDR:
            to = nMul(va, vb, "J");
            break;
        case DIV_DOUBLE_2ADDR:
            to = nDiv(va, vb, "D");
            break;
        case DIV_FLOAT_2ADDR:
            to = nDiv(va, vb, "F");
            break;
        case DIV_INT_2ADDR:
            to = nDiv(va, vb, "I");
            break;
        case DIV_LONG_2ADDR:
            to = nDiv(va, vb, "J");
            break;
        case REM_DOUBLE_2ADDR:
            to = nRem(va, vb, "D");
            break;
        case REM_FLOAT_2ADDR:
            to = nRem(va, vb, "F");
            break;
        case REM_INT_2ADDR:
            to = nRem(va, vb, "I");
            break;
        case REM_LONG_2ADDR:
            to = nRem(va, vb, "J");
            break;
        case AND_INT_2ADDR:
            to = nAnd(va, vb, TypeClass.ZI.name);
            break;
        case AND_LONG_2ADDR:
            to = nAnd(va, vb, "J");
            break;
        case OR_INT_2ADDR:
            to = nOr(va, vb, TypeClass.ZI.name);
            break;
        case OR_LONG_2ADDR:
            to = nOr(va, vb, "J");
            break;
        case XOR_INT_2ADDR:
            to = nXor(va, vb, TypeClass.ZI.name);
            break;
        case XOR_LONG_2ADDR:
            to = nXor(va, vb, "J");
            break;
        case SHL_INT_2ADDR:
            to = nShl(va, vb, "I");
            break;
        case SHL_LONG_2ADDR:
            to = nShl(va, vb, "J");
            break;
        case SHR_INT_2ADDR:
            to = nShr(va, vb, "I");
            break;
        case SHR_LONG_2ADDR:
            to = nShr(va, vb, "J");
            break;
        case USHR_INT_2ADDR:
            to = nUshr(va, vb, "I");
            break;
        case USHR_LONG_2ADDR:
            to = nUshr(va, vb, "J");
            break;
        case NOT_INT:
            to = nNot(vb, "I");
            break;
        case NOT_LONG:
            to = nNot(vb, "J");
            break;
        case NEG_DOUBLE:
            to = nNeg(vb, "D");
            break;
        case NEG_FLOAT:
            to = nNeg(vb, "F");
            break;
        case NEG_INT:
            to = nNeg(vb, "I");
            break;
        case NEG_LONG:
            to = nNeg(vb, "J");
            break;
        case INT_TO_BYTE:
            to = nCast(vb, "I", "B");
            break;
        case INT_TO_CHAR:
            to = nCast(vb, "I", "C");
            break;
        case INT_TO_DOUBLE:
            to = nCast(vb, "I", "D");
            break;
        case INT_TO_FLOAT:
            to = nCast(vb, "I", "F");
            break;
        case INT_TO_LONG:
            to = nCast(vb, "I", "J");
            break;
        case INT_TO_SHORT:
            to = nCast(vb, "I", "S");
            break;
        case FLOAT_TO_DOUBLE:
            to = nCast(vb, "F", "D");
            break;
        case FLOAT_TO_INT:
            to = nCast(vb, "F", "I");
            break;
        case FLOAT_TO_LONG:
            to = nCast(vb, "F", "J");
            break;
        case DOUBLE_TO_FLOAT:
            to = nCast(vb, "D", "F");
            break;
        case DOUBLE_TO_INT:
            to = nCast(vb, "D", "I");
            break;
        case DOUBLE_TO_LONG:
            to = nCast(vb, "D", "J");
            break;
        case LONG_TO_DOUBLE:
            to = nCast(vb, "J", "D");
            break;
        case LONG_TO_FLOAT:
            to = nCast(vb, "J", "F");
            break;
        case LONG_TO_INT:
            to = nCast(vb, "J", "I");
            break;
        default:
            throw new RuntimeException();
        }
        x(nAssign(va, to));
    }

    @Override
    public void visitStmt0R(Op op) {
        switch (op) {
        case RETURN_VOID:
            x(nReturnVoid());
            break;
        case NOP:
            // ignore
            break;
        case BAD_OP:
            x(nThrow(nInvokeNew(new Value[] { nString("bad dex opcode") }, new String[] { "Ljava/lang/String;" },
                    "Ljava/lang/VerifyError;")));
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void visitPackedSwitchStmt(Op op, int aA, int first_case, DexLabel[] labels) {
        LabelStmt[] lss = new LabelStmt[labels.length];
        for (int i = 0; i < labels.length; i++) {
            lss[i] = toLabelStmt(labels[i]);
        }
        LabelStmt d = new LabelStmt();
        x(nTableSwitch(locals[aA], first_case, lss, d));
        x(d);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
        LabelStmt xlabelStmts[] = new LabelStmt[types.length];
        for (int i = 0; i < types.length; i++) {
            xlabelStmts[i] = toLabelStmt(handlers[i]);
        }
        irMethod.traps.add(new Trap(toLabelStmt(start), toLabelStmt(end), xlabelStmts, types));
    }

    boolean lastIsInvokeOrFilledNewArray = false;

    public IrMethod convert(DexCodeNode codeNode) {
        if (codeNode.tryStmts != null) {
            for (TryCatchNode n : codeNode.tryStmts) {
                n.accept(this);
            }
        }
        if (codeNode.debugNode != null) {
            DexDebugVisitor ddv = this.visitDebug();
            if (ddv != null) {
                codeNode.debugNode.accept(ddv);
                ddv.visitEnd();
            }
        }
        lastIsInvokeOrFilledNewArray = false;
        if (codeNode.totalRegister >= 0) {
            this.visitRegister(codeNode.totalRegister);
        }
        for (DexStmtNode n : codeNode.stmts) {
            n.accept(this);
            if (n instanceof FilledNewArrayStmtNode) {
                lastIsInvokeOrFilledNewArray = true;
            } else if (n instanceof MethodStmtNode) {
                lastIsInvokeOrFilledNewArray = !((MethodStmtNode) n).method.getReturnType().equals("V");
            } else if (!(n instanceof DexLabelStmtNode)) {
                lastIsInvokeOrFilledNewArray = false;
            }
        }

        visitEnd();
        return irMethod;
    }
}
