package com.googlecode.dex2jar.ir.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.ts.TypeAnalyze.DefTypeRef;

public class TyepAnalyzeTypeRefTest {
    @Test
    public void test() {
        DefTypeRef d = new DefTypeRef();
        Set<Type> t = d.providerAs;
        t.add(Type.INT_TYPE);
        t.add(Type.BOOLEAN_TYPE);
        t.add(Type.BYTE_TYPE);
        t.add(Type.SHORT_TYPE);
        t.add(Type.CHAR_TYPE);
        t.add(Type.LONG_TYPE);
        t.add(Type.FLOAT_TYPE);
        t.add(Type.DOUBLE_TYPE);
        t.add(Type.getType("[Ljava/lang/Object;"));
        t.add(Type.getType("Ljava/lang/Object;"));
        t.add(Type.getType("[[Ljava/lang/Object;"));
        t.add(Type.getType("[Ljava/lang/String;"));
        t.add(Type.getType("Ljava/lang/String;"));
        Assert.assertEquals(
                "",
                "[[[Ljava/lang/Object;, [Ljava/lang/String;, [Ljava/lang/Object;, Ljava/lang/String;, Ljava/lang/Object;, D, J, F, I, C, S, B, Z]",
                t.toString());
        ;
    }
}
