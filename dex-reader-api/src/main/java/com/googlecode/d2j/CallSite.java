package com.googlecode.d2j;

public class CallSite {
    private final String name;
    private final MethodHandle bootstrapMethodHandler;
    private final String methodName;
    private final Proto methodProto;
    private final Object[] extraArguments;

    public CallSite(String name, MethodHandle bootstrapMethodHandler, String methodName, Proto methodProto,
                    Object... extraArguments) {
        this.name = name;
        this.bootstrapMethodHandler = bootstrapMethodHandler;
        this.methodName = methodName;
        this.methodProto = methodProto;
        this.extraArguments = extraArguments;
    }

    public String getName() {
        return name;
    }

    public MethodHandle getBootstrapMethodHandler() {
        return bootstrapMethodHandler;
    }

    public String getMethodName() {
        return methodName;
    }

    public Proto getMethodProto() {
        return methodProto;
    }

    public Object[] getExtraArguments() {
        return extraArguments;
    }
}
