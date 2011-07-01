package com.googlecode.dex2jar.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;

public class JimpleMethod {
    public Set<Stmt> _cfg_tails;
    public List<AssignStmt> _ls_inits = new ArrayList<AssignStmt>();
    public List<Stmt> _ls_visit_order;

    public int access;
    public Type[] args;
    public List<Local> locals = new ArrayList<Local>();
    public String name;

    public Type owner;

    public Type ret;

    public StmtList stmts = new StmtList();

    public List<Trap> traps = new ArrayList<Trap>();
}
