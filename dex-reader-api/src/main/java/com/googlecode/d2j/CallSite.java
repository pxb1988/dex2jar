package com.googlecode.d2j;

public class CallSite {
    private String name;
    private MethodHandle bootstrapMethodHandler;
    private String methodName;
    private Proto methodProto;
    private Object[] extraArguments;

    public CallSite(String name, MethodHandle bootstrapMethodHandler, String methodName, Proto methodProto, Object... extraArguments) {
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
