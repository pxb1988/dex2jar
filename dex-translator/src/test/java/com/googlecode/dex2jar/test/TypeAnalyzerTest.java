package com.googlecode.dex2jar.test;

import java.io.File;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.analysis.CodeNode;
import com.googlecode.dex2jar.analysis.type.TypeAnalyzer;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.v3.Main;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

public class TypeAnalyzerTest {
    @Test
    public void test() throws Exception {
        try {
            for (File f : TestUtils.listTestDexFiles(true)) {
                System.out.println("tyep-analyze file " + f);
                TestUtils.initDexFileReader(f).accept(new EmptyVisitor() {

                    @Override
                    public DexClassVisitor visit(int access_flags, String className, String superClass,
                            String[] interfaceNames) {
                        return this;
                    }

                    Method method;
                    boolean isStatic;

                    @Override
                    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
                        this.method = method;
                        this.isStatic = (accessFlags & Opcodes.ACC_STATIC) != 0;
                        return this;
                    }

                    @Override
                    public DexCodeVisitor visitCode() {
                        return new CodeNode() {
                            @Override
                            public void visitEnd() {
                                TypeAnalyzer ta = new TypeAnalyzer(this, isStatic, method);
                                ta.analyze();
                            }
                        };
                    }

                }, DexFileReader.SKIP_DEBUG);
            }
        } catch (Exception e) {
            Main.niceExceptionMessage(e, 0);
            throw e;
        }
    }
}
