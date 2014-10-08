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

import com.googlecode.dex2jar.ir.expr.Value.VT;

public final class Exprs {
    public static Value[] copy(Value[] v) {
        if (v == null) {
            return new Value[0];
        }
        Value vb[] = new Value[v.length];
        for (int i = 0; i < v.length; i++) {
            vb[i] = v[i].trim();
        }
        return vb;
    }

    public static Constant nByte(byte i) {
        return new Constant(i);
    }

    public static Constant nChar(char i) {
        return new Constant(i);
    }

    public static Constant nType(String desc) {
        return new Constant(new Constant.Type(desc));
    }

    public static Constant nDouble(double i) {
        return new Constant(i);
    }

    public static Constant nFloat(float i) {
        return new Constant(i);
    }

    public static Constant nInt(int i) {
        return new Constant(i);
    }

    public static Constant nLong(long i) {
        return new Constant(i);
    }

    public static Constant nNull() {
        return new Constant(Constant.Null);
    }

    public static Constant nShort(short i) {
        return new Constant(i);
    }

    public static Constant nString(String i) {
        return new Constant(i);
    }

    public static BinopExpr nAdd(Value a, Value b, String type) {
        return new BinopExpr(VT.ADD, a, b, type);
    }

    public static BinopExpr niAdd(Value a, Value b) {
        return new BinopExpr(VT.ADD, a, b, "I");
    }

    public static BinopExpr nAnd(Value a, Value b, String type) {
        return new BinopExpr(VT.AND, a, b, type);
    }

    public static ArrayExpr nArray(Value base, Value index, String elementType) {
        return new ArrayExpr(base, index, elementType);
    }

    public static Constant nArrayValue(Object array) {
        return new Constant(array);
    }

    public static CastExpr nCast(Value obj, String from, String to) {
        return new CastExpr(obj, from, to);
    }

    public static TypeExpr nCheckCast(Value obj, String type) {
        return new TypeExpr(VT.CHECK_CAST, obj, type);
    }

    public static BinopExpr nDCmpg(Value a, Value b) {
        return new BinopExpr(VT.DCMPG, a, b, "D");
    }

    public static BinopExpr nDCmpl(Value a, Value b) {
        return new BinopExpr(VT.DCMPL, a, b, "D");
    }

    public static BinopExpr nDiv(Value a, Value b, String type) {
        switch (type){
            case "I":
                return new BinopExpr(VT.IDIV, a, b, type);
            case "J":
                return new BinopExpr(VT.LDIV, a, b, type);
            case "F":
                return new BinopExpr(VT.FDIV, a, b, type);
            case "D":
                return new BinopExpr(VT.DDIV, a, b, type);
            default:
                throw new RuntimeException("type must set to one of I/J/F/D");
        }
    }

    public static BinopExpr nEq(Value a, Value b, String type) {
        return new BinopExpr(VT.EQ, a, b, type);
    }

    public static BinopExpr niEq(Value a, Value b) {
        return nEq(a, b, "I");
    }

    public static RefExpr nExceptionRef(String type) {
        return new RefExpr(VT.EXCEPTION_REF, type, -1);
    }

    public static BinopExpr nFCmpg(Value a, Value b) {
        return new BinopExpr(VT.FCMPG, a, b, "F");
    }

    public static BinopExpr nFCmpl(Value a, Value b) {
        return new BinopExpr(VT.FCMPL, a, b, "F");
    }

    public static FieldExpr nField(Value object, String ownerType, String fieldName, String fieldType) {
        return new FieldExpr(object, ownerType, fieldName, fieldType);
    }

    public static BinopExpr nGe(Value a, Value b, String type) {
        return new BinopExpr(VT.GE, a, b, type);
    }

    public static BinopExpr nGt(Value a, Value b, String type) {
        return new BinopExpr(VT.GT, a, b, type);
    }

    public static BinopExpr njGt(Value a, Value b) {
        return new BinopExpr(VT.GT, a, b, "J");
    }

    public static BinopExpr niGt(Value a, Value b) {
        return new BinopExpr(VT.GT, a, b, "I");
    }

    public static TypeExpr nInstanceOf(Value value, String type) {
        return new TypeExpr(VT.INSTANCE_OF, value, type);
    }

    public static InvokeExpr nInvokeInterface(Value[] regs, String owner, String name, String[] argmentTypes,
            String returnType) {
        return new InvokeExpr(VT.INVOKE_INTERFACE, regs, owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeNew(Value[] regs, String[] argmentTypes, String owner) {
        return new InvokeExpr(VT.INVOKE_NEW, regs, owner, "<init>", argmentTypes, owner);
    }

    public static InvokeExpr nInvokeSpecial(Value[] regs, String owner, String name, String[] argmentTypes,
            String returnType) {
        return new InvokeExpr(VT.INVOKE_SPECIAL, regs, owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeStatic(Value[] regs, String owner, String name, String[] argmentTypes,
            String returnType) {
        return new InvokeExpr(VT.INVOKE_STATIC, regs, owner, name, argmentTypes, returnType);
    }

    public static InvokeExpr nInvokeVirtual(Value[] regs, String owner, String name, String[] argmentTypes,
            String returnType) {
        return new InvokeExpr(VT.INVOKE_VIRTUAL, regs, owner, name, argmentTypes, returnType);
    }

    public static BinopExpr nLCmp(Value a, Value b) {
        return new BinopExpr(VT.LCMP, a, b, "J");
    }

    public static BinopExpr nLe(Value a, Value b, String type) {
        return new BinopExpr(VT.LE, a, b, type);
    }

    public static UnopExpr nLength(Value array) {
        return new UnopExpr(VT.LENGTH, array, null);
    }

    public static Local nLocal(int index) {
        return new Local(index);
    }

    public static Local nLocal(String debugName) {
        return new Local(debugName);
    }

    public static Local nLocal(int index, String debugName) {
        return new Local(index, debugName);
    }

    public static BinopExpr nLt(Value a, Value b, String type) {
        return new BinopExpr(VT.LT, a, b, type);
    }

    public static BinopExpr nMul(Value a, Value b, String type) {
        return new BinopExpr(VT.MUL, a, b, type);
    }

    public static BinopExpr nNe(Value a, Value b, String type) {
        return new BinopExpr(VT.NE, a, b, type);
    }

    public static UnopExpr nNeg(Value array, String type) {
        return new UnopExpr(VT.NEG, array, type);
    }

    public static NewExpr nNew(String type) {
        return new NewExpr(type);
    }

    public static TypeExpr nNewArray(String elementType, Value size) {
        return new TypeExpr(VT.NEW_ARRAY, size, elementType);
    }

    public static TypeExpr nNewIntArray(Value size) {
        return nNewArray("I", size);
    }

    public static TypeExpr nNewLongArray(Value size) {
        return nNewArray("J", size);
    }

    public static FilledArrayExpr nFilledArray(String elementType, Value[] datas) {
        return new FilledArrayExpr(datas, elementType);
    }

    public static NewMutiArrayExpr nNewMutiArray(String base, int dim, Value[] sizes) {
        return new NewMutiArrayExpr(base, dim, sizes);
    }

    public static UnopExpr nNot(Value array, String type) {
        return new UnopExpr(VT.NOT, array, type);
    }

    public static BinopExpr nOr(Value a, Value b, String type) {
        return new BinopExpr(VT.OR, a, b, type);
    }

    public static RefExpr nParameterRef(String type, int index) {
        return new RefExpr(VT.PARAMETER_REF, type, index);
    }

    public static BinopExpr nRem(Value a, Value b, String type) {
        return new BinopExpr(VT.REM, a, b, type);
    }

    public static BinopExpr nShl(Value a, Value b, String type) {
        return new BinopExpr(VT.SHL, a, b, type);
    }

    public static BinopExpr nShr(Value a, Value b, String type) {
        return new BinopExpr(VT.SHR, a, b, type);
    }

    public static StaticFieldExpr nStaticField(String ownerType, String fieldName, String fieldType) {
        return new StaticFieldExpr(ownerType, fieldName, fieldType);
    }

    public static BinopExpr nSub(Value a, Value b, String type) {
        return new BinopExpr(VT.SUB, a, b, type);
    }

    public static RefExpr nThisRef(String type) {
        return new RefExpr(VT.THIS_REF, type, -1);
    }

    public static BinopExpr nUshr(Value a, Value b, String type) {
        return new BinopExpr(VT.USHR, a, b, type);
    }

    public static BinopExpr nXor(Value a, Value b, String type) {
        return new BinopExpr(VT.XOR, a, b, type);
    }

    private Exprs() {
    }

    public static PhiExpr nPhi(Value... ops) {
        return new PhiExpr(ops);
    }

    public static Constant nConstant(Object cst) {
        return new Constant(cst);
    }
}
