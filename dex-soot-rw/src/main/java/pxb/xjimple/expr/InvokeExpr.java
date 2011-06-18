package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class InvokeExpr extends Value {

    public Type[] argmentTypes;
    public ValueBox args[];
    public String methodName;
    public Type methodOwnerType;
    public Type methodReturnType;
    public ValueBox object;

    public InvokeExpr(VT type, ValueBox object, ValueBox[] args, Type ownerType, String methodName,
            Type[] argmentTypes, Type returnType) {
        super(type);
        this.object = object;
        this.methodReturnType = returnType;
        this.methodName = methodName;
        this.methodOwnerType = ownerType;
        this.argmentTypes = argmentTypes;
        this.args = args;

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (super.vt == VT.INVOKE_NEW) {
            sb.append("new ").append(methodOwnerType.getClassName()).append('(');
        } else {
            sb.append(object == null ? methodOwnerType.getClassName() : object).append('.').append(this.methodName)
                    .append('(');
        }
        boolean first = true;
        for (ValueBox arg : args) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(arg);
        }
        sb.append(')');
        return sb.toString();
    }
}
