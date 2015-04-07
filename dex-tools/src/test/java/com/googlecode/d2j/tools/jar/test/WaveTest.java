package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.asm.LdcOptimizeAdapter;
import com.googlecode.d2j.jasmin.JasminDumper;
import com.googlecode.d2j.jasmin.Jasmins;
import com.googlecode.d2j.tools.jar.InvocationWeaver;
import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class WaveTest {
    @Test
    public void testA() throws IOException, RecognitionException {

        InvocationWeaver iw = new InvocationWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("d LA;.m()V=LB;.t(Lp;)Ljava/lang/Object;");

        test0(iw, "a");
    }

    @Test
    public void testB() throws IOException, RecognitionException {

        InvocationWeaver iw = new InvocationWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("r Ljava/util/ArrayList;.size=Lcom/googlecode/d2j/tools/jar/test/WaveTest;.size(Lp;)Ljava/lang/Object;");
        iw.withConfig("r Ljava/util/ArrayList;.add=Lcom/googlecode/d2j/tools/jar/test/WaveTest;.add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        iw.withConfig("r Ljava/io/PrintStream;.append(Ljava/lang/CharSequence;)Ljava/io/PrintStream;=Lcom/googlecode/d2j/tools/jar/test/WaveTest;.append(Lp;)Ljava/lang/Object;");
        iw.withConfig("r Ljava/io/PrintStream;.println(Ljava/lang/String;)V=Lcom/googlecode/d2j/tools/jar/test/WaveTest;.println(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");

        test0(iw, "b");
    }

    @Test
    public void testC() throws IOException, RecognitionException {

        InvocationWeaver iw = new InvocationWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("r LT;.a()V=LB;.a(Lp;)Ljava/lang/Object;");
        iw.withConfig("r LT;.b()V=LB;.b(Lp;)V");
        iw.withConfig("r LT;.c()I=LB;.c(Lp;)Ljava/lang/Object;");
        // iw.withConfig("r LT;.d()I=LB;.d(Lp;)V");
        test0(iw, "c");
    }

    private void test0(InvocationWeaver iw, String prefix) throws IOException, RecognitionException {
        ClassNode before = Jasmins
                .parse(prefix + "-before.j", getClass().getResourceAsStream("/weave/" + prefix + "-before.j"));
        ClassNode expectedAfter = Jasmins
                .parse(prefix + "-after.j", getClass().getResourceAsStream("/weave/" + prefix + "-after.j"));
        ClassNode expectedGen = Jasmins
                .parse(prefix + "-gen.j", getClass().getResourceAsStream("/weave/" + prefix + "-gen.j"));

        ClassNode after = new ClassNode();

        before.accept(iw.wrapper(after));

        assertEqual(expectedAfter, after);

        ClassNode gen = new ClassNode();
        iw.buildInvocationClz(LdcOptimizeAdapter.wrap(gen));

        assertEqual(expectedGen, gen);
    }

    private void assertEqual(ClassNode expected, ClassNode actual) throws IOException {
        String stdExpect = toStd(expected);
        String stdActual = toStd(actual);
        Assert.assertEquals(stdExpect, stdActual);
    }

    public static String toStd(ClassNode expected) throws IOException {
        expected.access &= ~Opcodes.ACC_SUPER;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        expected.accept(LdcOptimizeAdapter.wrap(cw));

        ClassReader cr = new ClassReader(cw.toByteArray());
        ClassNode n = new ClassNode(Opcodes.ASM4);
        cr.accept(n, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);

        StringWriter stringWriter = new StringWriter();
        new JasminDumper(new PrintWriter(stringWriter, true)).dump(n);
        return stringWriter.toString();
    }

}
