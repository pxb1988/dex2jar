package com.googlecode.dex2jar.ir;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.StmtList;

public class JimpleMethod {

    public int access;
    public Type[] args;
    public List<Local> locals = new ArrayList<Local>();
    public String name;

    public Type owner;

    public Type ret;

    public StmtList stmts = new StmtList();

    public List<Trap> traps = new ArrayList<Trap>();
}
