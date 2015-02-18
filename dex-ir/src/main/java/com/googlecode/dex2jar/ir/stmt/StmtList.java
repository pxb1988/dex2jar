/*
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
package com.googlecode.dex2jar.ir.stmt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

/**
 * Represent a list of statement.
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
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

    private Stmt first, last;

    private int index = 1;
    private int size = 0;

    public void add(Stmt stmt) {
        insertLast(stmt);
    }

    public void addAll(Collection<Stmt> list) {
        for (Stmt stmt : list) {
            insertLast(stmt);
        }
    }

    public StmtList clone(LabelAndLocalMapper mapper) {
        StmtList nList = new StmtList();
        for (Stmt stmt : this) {
            nList.add(stmt.clone(mapper));
        }
        return nList;
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
        if (stmt.id <= 0) {
            stmt.id = this.index;
            this.index++;
        }
    }

    public void insertAfter(Stmt position, Stmt stmt) {
        if (position.list == this) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            stmt.next = position.next;
            stmt.pre = position;
            if (position.next == null) {
                last = stmt;
            } else {
                position.next.pre = stmt;
            }
            position.next = stmt;
        }
    }

    public void insertBefore(Stmt position, Stmt stmt) {
        if (position.list == this) {
            indexIt(stmt);
            stmt.list = this;
            size++;
            stmt.pre = position.pre;
            stmt.next = position;
            if (position.pre == null) {
                first = stmt;
            } else {
                position.pre.next = stmt;
            }
            position.pre = stmt;
        }
    }

    public void insertFirst(Stmt stmt) {
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

    public void insertLast(Stmt stmt) {
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

    public void replace(Stmt stmt, Stmt nas) {
        if (stmt.list == this) {
            indexIt(nas);
            nas.list = this;
            nas.next = stmt.next;
            nas.pre = stmt.pre;
            if (stmt.next != null) {
                stmt.next.pre = nas;
            } else {
                this.last = nas;
            }
            if (stmt.pre != null) {
                stmt.pre.next = nas;
            } else {
                this.first = nas;
            }
            stmt.next = null;
            stmt.pre = null;
            stmt.list = null;
        }
    }

    @Override
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

    public void move(Stmt start, Stmt end, Stmt dist) {
        if (start.pre == null) {
            this.first = end.next;
        } else {
            start.pre.next = end.next;
        }
        if (end.next == null) {
            this.last = start.pre;
        } else {
            end.next.pre = start.pre;
        }

        if (dist.next == null) {
            this.last = end;
            end.next = null;
        } else {
            dist.next.pre = end;
            end.next = dist.next;
        }
        dist.next = start;
        start.pre = dist;
    }

    public void clear() {
        size = 0;
        first = null;
        last = null;
    }

}
