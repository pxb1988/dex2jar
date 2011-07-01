package com.googlecode.dex2jar.v3;

import org.objectweb.asm.ClassVisitor;

public interface ClassVisitorFactory {

    ClassVisitor create(String x);

}
