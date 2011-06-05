package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class MethodExpr extends Value {

    public Type[] argmentTypes;
    public ValueBox args[];
    public String methodName;
    public Type methodOwnerType;
    public Type methodReturnType;
    public ValueBox object;

    public MethodExpr(VT type, Value object, Value[] args, Type ownerType, String methodName, Type[] argmentTypes,
            Type returnType) {
        super(type);
        this.object = new ValueBox(object);
        this.methodReturnType = returnType;
        this.methodName = methodName;
        this.methodOwnerType = ownerType;
        this.argmentTypes = argmentTypes;
        this.args = new ValueBox[args.length];
        for (int i = 0; i < args.length; i++) {
            this.args[i] = new ValueBox(args[i]);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder().append(object == null ? methodOwnerType.getClassName() : object)
                .append(this.methodName).append('(');
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
