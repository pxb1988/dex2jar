package com.googlecode.dex2jar.ir.ts;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class UniqueQueue<T> extends LinkedList<T> {
    Set<T> set = new HashSet<>();

    public UniqueQueue() {
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T t : c) {
            if (add(t)) {
                result = true;
            }
        }
        return result;

    }

    @Override
    public boolean add(T t) {
        if (set.add(t)) {
            super.add(t);
        }
        return true;
    }

    public T poll() {
        T t = super.poll();
        set.remove(t);
        return t;
    }

    @Override
    public T pop() {
        T t = super.pop();
        set.remove(t);
        return t;
    }
}
