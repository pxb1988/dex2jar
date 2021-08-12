package com.googlecode.d2j.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ArrayOut implements Out {

    int i = 0;

    public List<String> array = new ArrayList<>();

    public List<Integer> is = new ArrayList<>();

    @Override
    public void push() {
        i++;
    }

    @Override
    public void s(String s) {
        is.add(i);
        array.add(s);
    }

    @Override
    public void s(String format, Object... arg) {
        s(String.format(format, arg));
    }

    @Override
    public void pop() {
        i--;
    }

}
