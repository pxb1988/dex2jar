package com.googlecode.dex2jar.ir.test;


import com.googlecode.dex2jar.ir.expr.Exprs;
import org.junit.Assert;
import org.junit.Test;

public class ConstantStringTest {
    @Test
    public void test() {
        String s = Exprs.nString("a\nb").toString();
        Assert.assertEquals("\"a\\nb\"", s);
    }
}
