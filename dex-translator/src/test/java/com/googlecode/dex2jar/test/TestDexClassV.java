package com.googlecode.dex2jar.test;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

import org.junit.Ignore;

@Ignore
public class TestDexClassV extends EmptyVisitor {
    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        return new V3MethodAdapter(accessFlags, method, null);
    }
}
