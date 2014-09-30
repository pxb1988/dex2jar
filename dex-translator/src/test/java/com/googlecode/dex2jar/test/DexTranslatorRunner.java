package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.node.DexClassNode;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

public class DexTranslatorRunner extends BlockJUnit4ClassRunner {

    public DexTranslatorRunner(Class klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
        if (method.getMethod().getParameterTypes().length > 0) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    // 1.invoke the method
                    DexClassNode clzNode = new DexClassNode(DexConstants.ACC_PUBLIC, "La;", "Ljava/lang/Object;", null);
                    if (method.isStatic()) {
                        method.invokeExplosively(null, clzNode);
                    } else {
                        method.invokeExplosively(test, clzNode);
                    }
                    // 2. convert and verify
                    TestUtils.translateAndCheck(clzNode);
                }
            };
        } else {
            return super.methodInvoker(method, test);
        }
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        // All methods are validate
    }
}
