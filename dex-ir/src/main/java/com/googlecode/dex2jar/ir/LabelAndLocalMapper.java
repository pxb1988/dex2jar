package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

import java.util.HashMap;
import java.util.Map;

public class LabelAndLocalMapper {
    Map<LabelStmt, LabelStmt> labels = new HashMap<>();
    Map<Local, Local> locals = new HashMap<>();

    public LabelStmt map(LabelStmt label) {
        LabelStmt nTarget = labels.get(label);
        if (nTarget == null) {
            nTarget = Stmts.nLabel();
            labels.put(label, nTarget);
        }
        return nTarget;
    }

    public Local map(Local local) {
        Local nTarget = locals.get(local);
        if (nTarget == null) {
            nTarget = (Local) local.clone();
            locals.put(local, nTarget);
        }
        return nTarget;
    }
}
