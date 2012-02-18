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

import static com.googlecode.dex2jar.ir.expr.Exprs.box;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public final class Stmts {

    public static AssignStmt nAssign(Value left, Value right) {
        return new AssignStmt(ST.ASSIGN, box(left), box(right));
    }

    public static AssignStmt nAssign(ValueBox left, ValueBox right) {
        return new AssignStmt(ST.ASSIGN, left, right);
    }

    public static JumpStmt nGoto(LabelStmt target) {
        return new JumpStmt(ST.GOTO, target);
    }

    public static AssignStmt nIdentity(Value local, Value identityRef) {
        return new AssignStmt(ST.IDENTITY, box(local), box(identityRef));
    }

    public static JumpStmt nIf(Value a, LabelStmt target) {
        return new JumpStmt(ST.IF, box(a), target);
    }

    public static LabelStmt nLabel() {
        return new LabelStmt(new Label());
    }

    public static LabelStmt nLabel(Label label) {
        return new LabelStmt(label);
    }

    public static UnopStmt nLock(Value op) {
        return new UnopStmt(ST.LOCK, box(op));
    }

    public static LookupSwitchStmt nLookupSwitch(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt target) {
        return new LookupSwitchStmt(box(key), lookupValues, targets, target);
    }

    public static NopStmt nNop() {
        return new NopStmt();
    }

    public static UnopStmt nReturn(Value op) {
        return new UnopStmt(ST.RETURN, box(op));
    }

    public static ReturnVoidStmt nReturnVoid() {
        return new ReturnVoidStmt();
    }

    public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, int highIndex, LabelStmt[] targets,
            LabelStmt target) {
        return new TableSwitchStmt(key, lowIndex, highIndex, targets, target);
    }

    public static UnopStmt nThrow(Value op) {
        return new UnopStmt(ST.THROW, box(op));
    }

    public static UnopStmt nUnLock(Value op) {
        return new UnopStmt(ST.UNLOCK, box(op));
    }

    public static LineNumStmt nLineNum(int line, LabelStmt label) {
        return new LineNumStmt(line, label);
    }

    public static LocVarStmt nLocVar(String name, String type, String signature,
            LabelStmt start, LabelStmt end, Value reg) {
        return new LocVarStmt(name, type, signature, start, end, box(reg));
    }

    private Stmts() {
    }
}
