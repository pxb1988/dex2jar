package com.googlecode.dex2jar.ir.test;

import junit.framework.Assert;

import org.junit.Test;

import com.googlecode.dex2jar.ir.Constant;

public class ConstantStringTest {
    @Test
    public void test() {
        String s = Constant.nString("a\nb").toString();
        Assert.assertEquals("\"a\\nb\"", s);
    }
}
