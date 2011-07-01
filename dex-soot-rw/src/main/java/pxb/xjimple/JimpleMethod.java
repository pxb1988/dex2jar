package pxb.xjimple;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import pxb.xjimple.stmt.Stmt;
import pxb.xjimple.stmt.StmtList;

public class JimpleMethod {
    public StmtList stmts = new StmtList();

    public String name;
    public List<Type> args = new ArrayList<Type>();
    public List<Local> locals = new ArrayList<Local>();
    public Type ret;
    public int access;

    public List<Stmt> _ls_visit_order;

    public List<Trap> traps = new ArrayList<Trap>();
}
