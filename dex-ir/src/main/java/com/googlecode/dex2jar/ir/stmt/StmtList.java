package com.googlecode.dex2jar.ir.stmt;

import java.util.Iterator;

import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public class StmtList implements Iterable<Stmt> {

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

    private Stmt first, last;

    private int size = 0;

    public void add(Stmt stmt) {
        insertLast(stmt);
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

    public void insertAftre(Stmt position, Stmt stmt) {
        if (position.list == this && stmt.list == null) {
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
    }

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
    };

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.size == 0) {
            sb.append("[Empty]");
        }
        for (Stmt s : this) {
            if (s.st == ST.LABEL) {
                sb.append("\n");
            }
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

}
