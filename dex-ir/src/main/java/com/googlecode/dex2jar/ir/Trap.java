package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

/**
 * TODO DOC
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Trap {

    public LabelStmt start;

    public LabelStmt end;

    public LabelStmt[] handlers;

    public String[] types;

    public Trap() {
        super();
    }

    public Trap(LabelStmt start, LabelStmt end, LabelStmt[] handlers, String[] types) {
        super();
        this.start = start;
        this.end = end;
        this.handlers = handlers;
        this.types = types;
    }

    public Trap clone(LabelAndLocalMapper mapper) {
        int size = handlers.length;
        LabelStmt[] cloneHandlers = new LabelStmt[size];
        String[] cloneTypes = new String[size];
        for (int i = 0; i < size; i++) {
            cloneHandlers[i] = handlers[i].clone(mapper);
            cloneTypes[i] = types[i];
        }
        return new Trap(start.clone(mapper), end.clone(mapper), cloneHandlers, cloneTypes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format(".catch %s - %s : ", start.getDisplayName(),
                end.getDisplayName()));
        for (int i = 0; i < handlers.length; i++) {
            sb.append(types[i] == null ? "all" : types[i]).append(" > ").append(handlers[i].getDisplayName())
                    .append(",");
        }
        return sb.toString();
    }

}
