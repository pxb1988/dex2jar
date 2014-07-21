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
package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public final class Stmts {

    public static AssignStmt nAssign(Value left, Value right) {
        return new AssignStmt(ST.ASSIGN, left, right);
    }

    public static AssignStmt nFillArrayData(Value left, Value arrayData) {
        return new AssignStmt(ST.FILL_ARRAY_DATA, left, arrayData);
    }

    public static GotoStmt nGoto(LabelStmt target) {
        return new GotoStmt(target);
    }

    public static AssignStmt nIdentity(Value local, Value identityRef) {
        return new AssignStmt(ST.IDENTITY, local, identityRef);
    }

    public static IfStmt nIf(Value a, LabelStmt target) {
        return new IfStmt(ST.IF, a, target);
    }

    public static LabelStmt nLabel() {
        return new LabelStmt();
    }

    public static UnopStmt nLock(Value op) {
        return new UnopStmt(ST.LOCK, op);
    }

    public static LookupSwitchStmt nLookupSwitch(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt target) {
        return new LookupSwitchStmt(key, lookupValues, targets, target);
    }

    public static NopStmt nNop() {
        return new NopStmt();
    }

    public static UnopStmt nReturn(Value op) {
        return new UnopStmt(ST.RETURN, op);
    }

    public static ReturnVoidStmt nReturnVoid() {
        return new ReturnVoidStmt();
    }

    public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, LabelStmt[] targets,
            LabelStmt target) {
        return new TableSwitchStmt(key, lowIndex, targets, target);
    }

    public static UnopStmt nThrow(Value op) {
        return new UnopStmt(ST.THROW, op);
    }

    public static UnopStmt nUnLock(Value op) {
        return new UnopStmt(ST.UNLOCK, op);
    }

    public static VoidInvokeStmt nVoidInvoke(Value op) {
        return new VoidInvokeStmt(op);
    }

    private Stmts() {
    }
}
