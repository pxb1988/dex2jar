package com.googlecode.dex2jar.ir.stmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public class StmtList implements Iterable<Stmt>, java.util.Comparator<Stmt> {

    private static class StmtListIterator implements Iterator<Stmt> {
        private Stmt current, next;
        private final StmtList list;

        /**
         * @param list
         * @param next
         */
        public StmtListIterator(StmtList list, Stmt next) {
            super();
            this.list = list;
            this.next = next;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Stmt next() {
            Stmt x = current = next;
            if (x != null) {
                next = x.next;
            } else {
                next = null;
            }
            return x;
        }

        @Override
        public void remove() {
            if (current != null) {
                list.remove(current);
                current = null;
            }
        }
    }
    public Set<Stmt> _cfg_tais;
    public List<AssignStmt> _ls_inits = new ArrayList<AssignStmt>();
    public List<Stmt> _ls_visit_order;

    private Stmt first, last;

    private int index = 0;
    private int size = 0;

    public void add(Stmt stmt) {
        insertLast(stmt);
    }

    @Override
    public int compare(Stmt o1, Stmt o2) {
        return o1.id - o2.id;
    }

    public boolean contains(Stmt stmt) {
        return stmt.list == this;
    }

    public Stmt getFirst() {
        return first;
    }

    public Stmt getLast() {
        return last;
    }

    public int getSize() {
        return size;
    }

    private void indexIt(Stmt stmt) {
        stmt.id = this.index;
        this.index++;
    }

    public void insertAftre(Stmt position, Stmt stmt) {
        if (position.list == this && stmt.list == null) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            if (position.next == null) {
                last = stmt;
            }
            stmt.next = position.next;
            stmt.pre = position;
            position.next = stmt;

        }
    }

    public void insertBefore(Stmt position, Stmt stmt) {
        if (position.list == this && stmt.list == null) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            if (position.pre == null) {
                first = stmt;
            }
            stmt.pre = position.pre;
            stmt.next = position;
            position.pre = stmt;

        }
    }

    public void insertFirst(Stmt stmt) {
        if (stmt.list == null) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            if (first == null) {// empty
                first = last = stmt;
                stmt.pre = stmt.next = null;
            } else {
                stmt.pre = null;
                stmt.next = first;
                first.pre = stmt;
                first = stmt;
            }
        }
    }

    public void insertLast(Stmt stmt) {
        if (stmt.list == null) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            if (first == null) {// empty
                first = last = stmt;
                stmt.pre = stmt.next = null;
            } else {
                stmt.next = null;
                stmt.pre = last;
                last.next = stmt;
                last = stmt;
            }
        }
    }

    @Override
    public Iterator<Stmt> iterator() {
        return new StmtListIterator(this, first);
    };

    public void remove(Stmt stmt) {
        if (stmt.list == this) {
            size--;
            stmt.list = null;
            if (stmt.pre == null) {
                first = stmt.next;
            } else {
                stmt.pre.next = stmt.next;
            }
            if (stmt.next == null) {
                last = stmt.pre;
            } else {
                stmt.next.pre = stmt.pre;
            }
            stmt.pre = null;
            stmt.next = null;
        }
    }

    public void replace(AssignStmt stmt, AssignStmt nas) {
        if (stmt.list == this) {
            indexIt(nas);
            nas.list = this;
            nas.next = stmt.next;
            nas.pre = stmt.pre;
            if (stmt.next != null) {
                stmt.next.pre = nas;
            }
            if (stmt.pre != null) {
                stmt.pre.next = nas;
            }
            stmt.next = null;
            stmt.pre = null;
            stmt.list = null;
        }
    }

    public String toString() {
        if (this.size == 0) {
            return "[Empty]";
        }
        StringBuilder sb = new StringBuilder();
        for (Stmt s : this) {
            if (s.st == ST.LABEL) {
                sb.append("\n");
            }
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

}
