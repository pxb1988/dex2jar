package com.googlecode.d2j.tools.jar.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.d2j.tools.jar.InvocationWeaver;
import com.googlecode.d2j.tools.jar.MethodInvocation;
import com.googlecode.dex2jar.tools.BaseCmd;

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
        URLClassLoader cl = new URLClassLoader(new URL[] { tmp.toURI().toURL() }, WaveTest.class.getClassLoader());
        Class<?> clz = cl.loadClass("com.googlecode.d2j.tools.jar.test.res.Res");
        List<Object> list = (List<Object>) clz.newInstance();
        Assert.assertFalse(list.add(""));
        Assert.assertEquals(-1, list.size());

        Method m = clz.getMethod("main", String[].class);
        System.out.println(m);
        m.invoke(null, new Object[] { null });
        Assert.assertTrue(appendCalled);
        Assert.assertTrue(printlnCalled);

        list = null;
        cl.close();
        tmp.delete();
    }

}
