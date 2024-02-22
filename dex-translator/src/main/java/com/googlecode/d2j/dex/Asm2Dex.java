package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import java.util.Arrays;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Asm2Dex {
    public static Object convertConstantValue(Object ele) {
        if (ele instanceof Type) {
            Type type = (Type) ele;
            if (type.getSort() == Type.METHOD) {
                return new Proto(toDescArray(type.getArgumentTypes()), type.getReturnType().getDescriptor());
            }
            return new DexType(type.getDescriptor());
        } else if (ele instanceof Handle) {
            return toMethodHandle((Handle) ele);
        }
        return ele;
    }

    public static Object[] convertConstObjects(Object[] bsmArgs) {
        if (bsmArgs == null) {
            return null;
        }
        Object[] copy = Arrays.copyOf(bsmArgs, bsmArgs.length);
        for (int i = 0; i < copy.length; i++) {
            Object ele = copy[i];
            ele = convertConstantValue(ele);
            copy[i] = ele;
        }
        return copy;
    }

    public static Proto toMethodType(String desc) {
        return new Proto(toDescArray(Type.getArgumentTypes(desc)), Type.getReturnType(desc).getDescriptor());
    }

    public static MethodHandle toMethodHandle(Handle bsm) {
        switch (bsm.getTag()) {
        case Opcodes.H_GETFIELD:
            return new MethodHandle(MethodHandle.INSTANCE_GET, toField(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_GETSTATIC:
            return new MethodHandle(MethodHandle.STATIC_GET, toField(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_PUTFIELD:
            return new MethodHandle(MethodHandle.INSTANCE_PUT, toField(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_PUTSTATIC:
            return new MethodHandle(MethodHandle.STATIC_PUT, toField(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_INVOKEVIRTUAL:
            return new MethodHandle(MethodHandle.INVOKE_INSTANCE, toMethod(bsm.getOwner(), bsm.getName(),
                    bsm.getDesc()));
        case Opcodes.H_INVOKESTATIC:
            return new MethodHandle(MethodHandle.INVOKE_STATIC, toMethod(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_INVOKESPECIAL:
            return new MethodHandle(MethodHandle.INVOKE_DIRECT, toMethod(bsm.getOwner(), bsm.getName(), bsm.getDesc()));
        case Opcodes.H_NEWINVOKESPECIAL:
            return new MethodHandle(MethodHandle.INVOKE_CONSTRUCTOR, toMethod(bsm.getOwner(), bsm.getName(),
                    bsm.getDesc()));
        case Opcodes.H_INVOKEINTERFACE:
            return new MethodHandle(MethodHandle.INVOKE_INTERFACE, toMethod(bsm.getOwner(), bsm.getName(),
                    bsm.getDesc()));
        default:
            throw new RuntimeException("Not supported yet.");
        }
    }

    static private Method toMethod(String internalName, String name, String desc) {
        return new Method("L" + internalName + ";", name, toMethodType(desc));
    }

    static private Field toField(String internalName, String name, String desc) {
        return new Field("L" + internalName + ";", name, desc);
    }

    public static String[] toDescArray(Type[] ts) {
        String[] ds = new String[ts.length];
        for (int i = 0; i < ts.length; i++) {
            ds[i] = ts[i].getDescriptor();
        }
        return ds;
    }
}
