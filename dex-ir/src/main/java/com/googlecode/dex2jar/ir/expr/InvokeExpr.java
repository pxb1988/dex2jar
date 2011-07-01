package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;

public class InvokeExpr extends Value {

    public Type[] argmentTypes;
    public ValueBox args[];
    public String methodName;
    public Type methodOwnerType;
    public Type methodReturnType;

    public InvokeExpr(VT type, ValueBox[] args, Type ownerType, String methodName, Type[] argmentTypes, Type returnType) {
        super(type);
        this.methodReturnType = returnType;
        this.methodName = methodName;
        this.methodOwnerType = ownerType;
        this.argmentTypes = argmentTypes;
        this.args = args;

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        if (super.vt == VT.INVOKE_NEW) {
            sb.append("new ").append(methodOwnerType.getClassName()).append('(');
        } else {
            sb.append(super.vt == VT.INVOKE_STATIC ? methodOwnerType.getClassName() : args[i]).append('.')
                    .append(this.methodName).append('(');
            i++;
        }
        boolean first = true;
        for (; i < args.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(args[i]);
        }
        sb.append(')');
        return sb.toString();
    }
}
