package com.googlecode.d2j.tools.jar;


public interface MethodInvocation {
    Object proceed() throws Throwable;

    String getMethodOwner();

    String getMethodName();

    String getMethodDesc();

    Object getThis();

    Object[] getArguments();
}
