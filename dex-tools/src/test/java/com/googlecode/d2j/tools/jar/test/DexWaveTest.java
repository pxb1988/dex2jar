package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.smali.BaksmaliDumpOut;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;
import com.googlecode.d2j.tools.jar.DexWeaver;
import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

public class DexWaveTest {
    @Test
    public void testA() throws IOException, RecognitionException {

        DexWeaver iw = new DexWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("d LA;.m()V=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m1()I=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m2()J=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m3(J)V=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m4()V=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m5(J)V=LB;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("d LA;.m6(J)J=LB;.t(Lp;)Ljava/lang/Object;");
        test0(iw, "a");
    }

    @Test
    public void testB() throws IOException, RecognitionException {

        DexWeaver iw = new DexWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("r LB;.b=LX;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("r LB;.c=LX;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("r LB;.d=LX;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("r LB;.e=LX;.t(Lp;)Ljava/lang/Object;");
        iw.withConfig("r LB;.f=LX;.t(Lp;)Ljava/lang/Object;");
        test0(iw, "b");
    }

    private void test0(DexWeaver iw, String prefix) throws IOException, RecognitionException {
        DexClassNode before = Smali.smaliFile2Node(prefix + "-before.smali", getClass()
                .getResourceAsStream("/weave/smali/" + prefix + "-before.smali"));
        DexClassNode expectedAfter = Smali.smaliFile2Node(prefix + "-after.smali", getClass()
                .getResourceAsStream("/weave/smali/" + prefix + "-after.smali"));

        DexFileNode dfn = new DexFileNode();

        before.accept(iw.wrap(dfn));

        assertEqual(expectedAfter, dfn.clzs.get(0));

        DexClassNode expectedGen = Smali.smaliFile2Node(prefix + "-gen.j", getClass()
                .getResourceAsStream("/weave/smali/" + prefix + "-gen.smali"));

        dfn.clzs.clear();
        iw.buildInvocationClz(dfn);

        assertEqual(expectedGen, dfn.clzs.get(0));

    }

    private void assertEqual(DexClassNode expected, DexClassNode actual) throws IOException {
        String stdExpect = toStd(expected);
        String stdActual = toStd(actual);
        Assert.assertEquals(stdExpect, stdActual);
    }

    public static String toStd(DexClassNode expected) throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        BaksmaliDumpOut out = new BaksmaliDumpOut(bufferedWriter);
        final BaksmaliDumper bs = new BaksmaliDumper(true, false);
        bs.baksmaliClass(expected, out);
        bufferedWriter.close();
        return stringWriter.toString();
    }
}
