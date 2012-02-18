package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.LineNumStmt;
import com.googlecode.dex2jar.ir.stmt.LocVarStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;

public class InfoMove2Code implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        StmtList stmts = irMethod.stmts;
        List<Stmt> lvs = new ArrayList<Stmt>();
        //Try to found all LOCALVARIABLE block and move them to the code before it's end label.
        //And all LINENUMBER block and move them after it's label
        for(Stmt st:stmts){
            if(st.st == ST.LOCALVARIABLE || st.st == ST.LINENUMBER){
                lvs.add(st);
            }
        }
        for(Stmt st:lvs){
            Stmt dist = null;
            if(st.st == ST.LOCALVARIABLE){
                dist = ((LocVarStmt)st).end.getPre();
                boolean flag = true;
                boolean skip = true;
                //用于跳过Switch table形成的空行
                while(flag && dist != null){
                    switch(dist.st){
                    case GOTO:
                    case RETURN:
                    case RETURN_VOID:
                    case THROW:
                        dist = dist.getPre();
                        skip = false;
                        break;
                    case LOCALVARIABLE:
                        dist = dist.getPre();
                        break;
                    case LABEL:
                    case LINENUMBER:
                        if(skip){//SKIP EMPTY LINE
                            dist = dist.getPre();
                        }else{
                            flag = false;
                        }
                        break;
                    default:
                        //a1 := @Exception
                        //a8 = a1
                        flag = false;
                        break;
                    }
                }
            } else if(st.st == ST.LINENUMBER){
                dist = ((LineNumStmt)st).label;
            }
            if(dist != null){
                stmts.move(st, st, dist);
            }
        }
    }

}
