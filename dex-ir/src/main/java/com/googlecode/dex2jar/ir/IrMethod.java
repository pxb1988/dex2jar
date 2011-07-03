package com.googlecode.dex2jar.ir;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.StmtList;

public class IrMethod {

    public int access;
    public Type[] args;
    public List<Local> locals = new ArrayList<Local>();
    public String name;

    public Type owner;

    public Type ret;

    public StmtList stmts = new StmtList();

    public List<Trap> traps = new ArrayList<Trap>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("// ").append(this.owner.getClassName()).append("\n");
        sb.append(ToStringUtil.getAccDes(access)).append(ToStringUtil.toShortClassName(ret)).append(' ')
                .append(this.name).append('(');
        boolean first = true;
        for (Type arg : args) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(ToStringUtil.toShortClassName(arg));
        }
        sb.append(") {\n\n").append(stmts).append("\n");
        if (traps.size() > 0) {
            sb.append("=============\n");
            for (Trap trap : traps) {
                sb.append(trap.start).append(" - ").append(trap.end).append(" > ").append(trap.handler).append(" // ")
                        .append(trap.type == null ? null : ToStringUtil.toShortClassName(trap.type)).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
