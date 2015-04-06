package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.jasmin.JasminDumper;
import com.googlecode.d2j.tools.jar.InvocationWeaver;
import com.googlecode.d2j.tools.jar.MethodInvocation;
import com.googlecode.dex2jar.tools.BaseCmd;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import com.googlecode.d2j.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.util.List;

/**
 * public class Res extends ArrayList { public static void main(String... args) { System.out.append("");
 * System.out.println("test"); }
 * 
 * @Override public int size() { return super.size(); }
 * @Override public boolean add(Object o) { return super.add(o); } }
 */
public class WaveTest {
    static boolean appendCalled = false;
    static boolean printlnCalled = false;

    public static Object add(Object thiz, Object obj) throws Throwable {
        System.out.println("add");
        return false;
    }

    public static Object size(MethodInvocation mi) throws Throwable {
        System.out.println(mi.getMethodName());
        return -1;
    }

    public static Object append(MethodInvocation mi) throws Throwable {
        appendCalled = true;
        System.out.println(mi.getMethodName());
        return null;
    }

    public static Object println(Object thiz, String str) throws Throwable {
        printlnCalled = true;
        System.out.println("println");
        return null;
    }

    @Test
    public void test() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException,
            NoSuchMethodException, InvocationTargetException, URISyntaxException {

        InvocationWeaver w = new InvocationWeaver().withConfig(WaveTest.class.getResourceAsStream("/wave.config"));
        File tmp = File.createTempFile("abc", ".jar");

        try (FileSystem fs2 = BaseCmd.createZip(tmp.toPath());
                FileSystem fs = BaseCmd.openZip(new File(WaveTest.class.getResource("/wave.jar").getPath()).toPath())) {
            w.wave(fs.getPath("/"), fs2.getPath("/"));
        }
        try(URLClassLoader cl = new URLClassLoader(new URL[] { tmp.toURI().toURL() }, WaveTest.class.getClassLoader())) {
            Class<?> clz = cl.loadClass("com.googlecode.d2j.tools.jar.test.res.Res");
            List<Object> list = (List<Object>) clz.newInstance();
            Assert.assertFalse(list.add(""));
            Assert.assertEquals(-1, list.size());

            Method m = clz.getMethod("main", String[].class);
            System.out.println(m);
            m.invoke(null, new Object[]{null});
            Assert.assertTrue(appendCalled);
            Assert.assertTrue(printlnCalled);

            list = null;
            tmp.delete();
        }
    }

    @Test
    public void test2() {
        ClassNode cn = new ClassNode();
        cn.name = "A";
        cn.version = Opcodes.V1_6;
        MethodVisitor mv = cn.visitMethod(0, "m", "()V", null, null);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        cn.visitEnd();


        new JasminDumper(new PrintWriter(System.out, true)).dump(cn);


        InvocationWeaver iw = new InvocationWeaver();
        iw.setInvocationInterfaceDesc("Lp;");
        iw.withConfig("d LA;.m()V=LB;.t(Lp;)Ljava/lang/Object;");
        ClassNode nc = new ClassNode();
        cn.accept(iw.wrapper(LdcOptimizeAdapter.wrap(nc)));

        new JasminDumper(new PrintWriter(System.out, true)).dump(nc);

        ClassNode nc2 = new ClassNode();
        iw.buildInvocationClz(LdcOptimizeAdapter.wrap(nc2));

        new JasminDumper(new PrintWriter(System.out, true)).dump(nc2);
    }

}
