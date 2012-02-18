package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;

public class LocVarMove2End implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        //Try to found all LOCALVARIABLE block and move them to the end.
        StmtList stmts = irMethod.stmts;
        List<Stmt> lvs = new ArrayList<Stmt>();
        for(Stmt st:stmts){
            if(st.st == ST.LOCALVARIABLE){
                lvs.add(st);
            }
        }
        Stmt end = stmts.getLast();
        for(Stmt st:lvs){
            stmts.move(st, st, end);
        }
    }

}
