/*
 * dex2jar - Tools to work with android .dex and java .class files
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
package com.googlecode.dex2jar.ir.ts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.*;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

/**
 * Clean unused {@link LabelStmt}
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class CleanLabel implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        Set<LabelStmt> uselabels = new HashSet<LabelStmt>();
        addTrap(irMethod.traps, uselabels);
        addVars(irMethod.vars, uselabels);
        addStmt(irMethod.stmts, uselabels);
        rmUnused(irMethod.stmts, uselabels);
    }

    private void addVars(List<LocalVar> vars, Set<LabelStmt> uselabels) {
        if (vars != null) {
            for (LocalVar var : vars) {
                uselabels.add(var.start);
                uselabels.add(var.end);
            }
        }

    }

    private void rmUnused(StmtList stmts, Set<LabelStmt> uselabels) {
        for (Stmt p = stmts.getFirst(); p != null;) {
            if (p.st == ST.LABEL) {
                if (!uselabels.contains(p)) {
                    Stmt q = p.getNext();
                    stmts.remove(p);
                    p = q;
                    continue;
                }
            }
            p = p.getNext();
        }
    }

    private void addStmt(StmtList stmts, Set<LabelStmt> labels) {
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            if (p instanceof JumpStmt) {
                labels.add(((JumpStmt) p).getTarget());
            } else if (p instanceof BaseSwitchStmt) {
                BaseSwitchStmt stmt = (BaseSwitchStmt) p;
                labels.add(stmt.defaultTarget);
                for (LabelStmt t : stmt.targets) {
                    labels.add(t);
                }
            }
        }
    }

    private void addTrap(List<Trap> traps, Set<LabelStmt> labels) {
        if (traps != null) {
            for (Trap trap : traps) {
                labels.add(trap.start);
                labels.add(trap.end);
                for (LabelStmt h : trap.handlers) {
                    labels.add(h);
                }
            }
        }
    }

}
