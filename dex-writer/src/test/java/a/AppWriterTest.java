package a;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.DexFileWriter;
import com.googlecode.d2j.dex.writer.DexWriteException;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.*;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class AppWriterTest implements DexConstants {
    @Test(expected = DexWriteException.class)
    public void testDupClz() {
        DexFileWriter w = new DexFileWriter();
        DexClassVisitor cv = w.visit(0, "La/b;", null, null);
        cv.visitEnd();
        cv = w.visit(0, "La/b;", null, null);
        cv.visitEnd();
        w.visitEnd();
    }

    @Test
    public void test3() {
        DexFileWriter w = new DexFileWriter();
        DexClassVisitor cv = w.visit(0x1, "La/c;", null, new String[]{"Ljava/lang/Comparable;"});
        cv.visitSource("c.java");
        cv.visitAnnotation("LAnn;", Visibility.SYSTEM).visitEnd();
        DexFieldVisitor fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, new Field("La/c;", "a", "I"), 55);
        fv.visitAnnotation("LE;", Visibility.RUNTIME).visitEnd();
        fv.visitEnd();

        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La/c;", "bb", new String[]{"I"}, "V"));
        mv.visitAnnotation("Laaa;", Visibility.RUNTIME).visitEnd();
        DexAnnotationVisitor dav = mv.visitParameterAnnotation(0).visitAnnotation("Laaa;", Visibility.RUNTIME);
        dav.visit("abc", true);
        DexAnnotationVisitor dav2 = dav.visitArray("efg");
        dav2.visit("", "123");
        dav2.visit("", "456");
        dav2.visitEnd();
        dav.visitEnd();

        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(2);

        code.visitStmt0R(Op.RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();

        cv.visitEnd();


        w.visitEnd();
        w.toByteArray();
    }

    @Test
    public void test4() throws IOException {
        DexFileWriter w = new DexFileWriter();
        DexFileReader dexFileReader = new DexFileReader(new File("../dex-translator/src/test/resources/dexes/i_jetty.dex"));
        dexFileReader.accept(w);
        w.toByteArray();
    }
}
