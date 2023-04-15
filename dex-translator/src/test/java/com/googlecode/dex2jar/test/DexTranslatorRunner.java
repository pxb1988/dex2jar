package com.googlecode.dex2jar.test;

import com.android.tools.r8.internal.Ex;
import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.node.DexClassNode;
import java.util.List;

import com.googlecode.d2j.visitors.DexClassVisitor;
import org.junit.jupiter.api.extension.*;

import static org.junit.jupiter.api.Assertions.*;

public class DexTranslatorRunner implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(DexClassNode.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // Provide a class-node that will be modified by tests.
        // When tests call 'visitEnd' the class-node will be validated.
        return new ValidatingDexClassNode(DexConstants.ACC_PUBLIC, "La;", "Ljava/lang/Object;", null);
    }

    private static class ValidatingDexClassNode extends DexClassNode {
        public ValidatingDexClassNode(DexClassVisitor v, int access, String className, String superClass, String[] interfaceNames) {
            super(v, access, className, superClass, interfaceNames);
        }

        public ValidatingDexClassNode(int access, String className, String superClass, String[] interfaceNames) {
            super(access, className, superClass, interfaceNames);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
           try {
               TestUtils.translateAndCheck(this);
           }catch (Exception ex) {
               fail(ex);
           }
        }
    }
}
