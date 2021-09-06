package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

/**
 * Represent a method invocation expression. To represent a {@link VT#INVOKE_INTERFACE},{@link VT#INVOKE_SPECIAL} or
 * {@link VT#INVOKE_VIRTUAL} the first element of ops is the owner object,To represent a {@link VT#INVOKE_NEW} or
 * {@link VT#INVOKE_STATIC} all ops are arguments. The return type of {@link VT#INVOKE_NEW} is owner instead of ret
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see VT#INVOKE_INTERFACE
 * @see VT#INVOKE_NEW
 * @see VT#INVOKE_SPECIAL
 * @see VT#INVOKE_STATIC
 * @see VT#INVOKE_VIRTUAL
 */
public class InvokeExpr extends AbstractInvokeExpr {

    public Method method;

    @Override
    protected void releaseMemory() {
        method = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return method == null ? null : method.getProto();
    }

    public InvokeExpr(VT type, Value[] args, String ownerType, String methodName, String[] argmentTypes,
                      String returnType) {
        this(type, args, new Method(ownerType, methodName, argmentTypes, returnType));
    }

    public InvokeExpr(VT type, Value[] args, Method method) {
        super(type, args);
        this.method = method;
    }

    @Override
    public InvokeExpr clone() {
        return new InvokeExpr(vt, cloneOps(), method);
    }

    @Override
    public InvokeExpr clone(LabelAndLocalMapper mapper) {
        return new InvokeExpr(vt, cloneOps(mapper), method);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        if (vt == VT.INVOKE_NEW) {
            sb.append("new ").append(Util.toShortClassName(getOwner()));
        } else if (vt == VT.INVOKE_STATIC) {
            sb.append(Util.toShortClassName(getOwner())).append('.')
                    .append(getName());
        } else {
            sb.append(ops[i++]).append('.').append(getName());
        }
        sb.append('(');
        boolean first = true;
        for (; i < ops.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(ops[i]);
        }
        sb.append(')');
        return sb.toString();
    }

    public String getOwner() {
        return method == null ? null : method.getOwner();
    }

    public String getRet() {
        return method == null ? null : method.getReturnType();
    }

    public String getName() {
        return method == null ? null : method.getName();
    }

    public String[] getArgs() {
        return method == null ? null : method.getParameterTypes();
    }

}
