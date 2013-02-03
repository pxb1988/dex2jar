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
package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;

public final class Exprs {

    public static ValueBox box(Value value) {
        return new ValueBox(value);
    }

    public static ValueBox[] box(Value[] v) {
        if (v == null) {
            return new ValueBox[0];
        }
        ValueBox vb[] = new ValueBox[v.length];
        for (int i = 0; i < v.length; i++) {
            vb[i] = new ValueBox(v[i]);
        }
        return vb;
    }

    public static BinopExpr nAdd(Value a, Value b, Type type) {
        return new BinopExpr(VT.ADD, a, b, type);
    }

    public static BinopExpr nAnd(Value a, Value b, Type type) {
        return new BinopExpr(VT.AND, a, b, type);
    }

    public static ArrayExpr nArray(Value base, Value index) {
        return new ArrayExpr(base, index);
    }

    public static CastExpr nCast(Value obj, Type from, Type to) {
        return new CastExpr(obj, from, to);
    }

    public static TypeExpr nCheckCast(Value obj, Type type) {
        return new TypeExpr(VT.CHECK_CAST, obj, type);
    }

    public static BinopExpr nDCmpg(Value a, Value b) {
        return new BinopExpr(VT.DCMPG, a, b, Type.DOUBLE_TYPE);
    }

    public static BinopExpr nDCmpl(Value a, Value b) {
        return new BinopExpr(VT.DCMPL, a, b, Type.DOUBLE_TYPE);
    }

    public static BinopExpr nDiv(Value a, Value b, Type type) {
        return new BinopExpr(VT.DIV, a, b, type);
    }

    public static BinopExpr nEq(Value a, Value b, Type type) {
        return new BinopExpr(VT.EQ, a, b, type);
    }

    public static RefExpr nExceptionRef(Type type) {
        return new RefExpr(VT.EXCEPTION_REF, type, -1);
    }

    public static BinopExpr nFCmpg(Value a, Value b) {
        return new BinopExpr(VT.FCMPG, a, b, Type.FLOAT_TYPE);
    }

    public static BinopExpr nFCmpl(Value a, Value b) {
        return new BinopExpr(VT.FCMPL, a, b, Type.FLOAT_TYPE);
    }

    public static FieldExpr nField(Value object, Type ownerType, String fieldName, Type fieldType) {
        return new FieldExpr(new ValueBox(object), ownerType, fieldName, fieldType);
    }

    public static BinopExpr nGe(Value a, Value b, Type type) {
        return new BinopExpr(VT.GE, a, b, type);
    }

    public static BinopExpr nGt(Value a, Value b, Type type) {
        return new BinopExpr(VT.GT, a, b, type);
    }

    public static TypeExpr nInstanceOf(Value value, Type type) {
        return new TypeExpr(VT.INSTANCE_OF, value, type);
    }

    public static InvokeExpr nInvokeInterface(Value[] regs, Type owner, String name, Type[] argmentTypes,
            Type returnType) {
        return new InvokeExpr(VT.INVOKE_INTERFACE, box(regs), owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeNew(Value[] regs, Type[] argmentTypes, Type owner) {
        return new InvokeExpr(VT.INVOKE_NEW, box(regs), owner, "<init>", argmentTypes, owner);
    }

    public static InvokeExpr nInvokeNew(ValueBox[] regs, Type[] argmentTypes, Type owner) {
        return new InvokeExpr(VT.INVOKE_NEW, regs, owner, "<init>", argmentTypes, owner);
    }

    public static InvokeExpr nInvokeSpecial(Value[] regs, Type owner, String name, Type[] argmentTypes, Type returnType) {
        return new InvokeExpr(VT.INVOKE_SPECIAL, box(regs), owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeStatic(Value[] regs, Type owner, String name, Type[] argmentTypes, Type returnType) {
        return new InvokeExpr(VT.INVOKE_STATIC, box(regs), owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeVirtual(Value[] regs, Type owner, String name, Type[] argmentTypes, Type returnType) {
        return new InvokeExpr(VT.INVOKE_VIRTUAL, box(regs), owner, name, argmentTypes, returnType);
    }

    public static BinopExpr nLCmp(Value a, Value b) {
        return new BinopExpr(VT.LCMP, a, b, Type.LONG_TYPE);
    }

    public static BinopExpr nLe(Value a, Value b, Type type) {
        return new BinopExpr(VT.LE, a, b, type);
    }

    public static UnopExpr nLength(Value array) {
        return new UnopExpr(VT.LENGTH, array, null);
    }

    public static Local nLocal(String name) {
        return new Local(name);
    }

    public static BinopExpr nLt(Value a, Value b, Type type) {
        return new BinopExpr(VT.LT, a, b, type);
    }

    public static BinopExpr nMul(Value a, Value b, Type type) {
        return new BinopExpr(VT.MUL, a, b, type);
    }

    public static BinopExpr nNe(Value a, Value b, Type type) {
        return new BinopExpr(VT.NE, a, b, type);
    }

    public static UnopExpr nNeg(Value array, Type type) {
        return new UnopExpr(VT.NEG, array, type);
    }

    public static NewExpr nNew(Type type) {
        return new NewExpr(type);
    }

    public static TypeExpr nNewArray(Type elementType, Value size) {
        return new TypeExpr(VT.NEW_ARRAY, size, elementType);
    }

    public static FilledArrayExpr nFilledArray(Type elementType, Value[] datas) {
        return new FilledArrayExpr(box(datas), elementType);
    }

    public static NewMutiArrayExpr nNewMutiArray(Type base, int dim, Value[] sizes) {
        return new NewMutiArrayExpr(base, dim, box(sizes));
    }

    public static UnopExpr nNot(Value array, Type type) {
        return new UnopExpr(VT.NOT, array, type);
    }

    public static BinopExpr nOr(Value a, Value b, Type type) {
        return new BinopExpr(VT.OR, a, b, type);
    }

    public static RefExpr nParameterRef(Type type, int index) {
        return new RefExpr(VT.PARAMETER_REF, type, index);
    }

    public static BinopExpr nRem(Value a, Value b, Type type) {
        return new BinopExpr(VT.REM, a, b, type);
    }

    public static BinopExpr nShl(Value a, Value b, Type type) {
        return new BinopExpr(VT.SHL, a, b, type);
    }

    public static BinopExpr nShr(Value a, Value b, Type type) {
        return new BinopExpr(VT.SHR, a, b, type);
    }

    public static FieldExpr nStaticField(Type ownerType, String fieldName, Type fieldType) {
        return new FieldExpr(null, ownerType, fieldName, fieldType);
    }

    public static BinopExpr nSub(Value a, Value b, Type type) {
        return new BinopExpr(VT.SUB, a, b, type);
    }

    public static RefExpr nThisRef(Type type) {
        return new RefExpr(VT.THIS_REF, type, -1);
    }

    public static BinopExpr nUshr(Value a, Value b, Type type) {
        return new BinopExpr(VT.USHR, a, b, type);
    }

    public static BinopExpr nXor(Value a, Value b, Type type) {
        return new BinopExpr(VT.XOR, a, b, type);
    }

    private Exprs() {
    }
}
