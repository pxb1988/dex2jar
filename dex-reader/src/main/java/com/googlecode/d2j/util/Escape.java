package com.googlecode.d2j.util;

import com.googlecode.d2j.CallSite;
import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public final class Escape implements DexConstants {

    private Escape() {
        throw new UnsupportedOperationException();
    }

    static boolean contain(int a, int b) {
        return (a & b) != 0;
    }

    public static String classAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_INTERFACE)) {
            sb.append("ACC_INTERFACE|");
        }
        if (contain(acc, ACC_ABSTRACT)) {
            sb.append("ACC_ABSTRACT|");
        }
        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_ANNOTATION)) {
            sb.append("ACC_ANNOTATION|");
        }
        if (contain(acc, ACC_ENUM)) {
            sb.append("ACC_ENUM|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String methodAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_BRIDGE)) {
            sb.append("ACC_BRIDGE|");
        }
        if (contain(acc, ACC_VARARGS)) {
            sb.append("ACC_VARARGS|");
        }
        if (contain(acc, ACC_NATIVE)) {
            sb.append("ACC_NATIVE|");
        }
        if (contain(acc, ACC_ABSTRACT)) {
            sb.append("ACC_ABSTRACT|");
        }
        if (contain(acc, ACC_STRICT)) {
            sb.append("ACC_STRICT|");
        }
        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_CONSTRUCTOR)) {
            sb.append("ACC_CONSTRUCTOR|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String fieldAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_VOLATILE)) {
            sb.append("ACC_VOLATILE|");
        }
        if (contain(acc, ACC_TRANSIENT)) {
            sb.append("ACC_TRANSIENT|");
        }

        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_ENUM)) {
            sb.append("ACC_ENUM|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String v(Field f) {
        return String.format("new Field(%s,%s,%s)", v(f.getOwner()), v(f.getName()), v(f.getType()));
    }

    public static String v(Method m) {
        return String.format("new Method(%s,%s,%s,%s)", v(m.getOwner()), v(m.getName()), v(m.getParameterTypes()),
                v(m.getReturnType()));
    }

    public static String v(Proto m) {
        return String.format("new Proto(%s,%s)", v(m.getParameterTypes()), v(m.getReturnType()));
    }

    public static String v(MethodHandle m) {
        switch (m.getType()) {
        case MethodHandle.INSTANCE_GET:
            return String.format("new MethodHandle(MethodHandle.INSTANCE_GET,%s)", v(m.getField()));
        case MethodHandle.INSTANCE_PUT:
            return String.format("new MethodHandle(MethodHandle.INSTANCE_PUT,%s)", v(m.getField()));
        case MethodHandle.STATIC_GET:
            return String.format("new MethodHandle(MethodHandle.STATIC_GET,%s)", v(m.getField()));
        case MethodHandle.STATIC_PUT:
            return String.format("new MethodHandle(MethodHandle.STATIC_PUT,%s)", v(m.getField()));

        case MethodHandle.INVOKE_INSTANCE:
            return String.format("new MethodHandle(MethodHandle.INVOKE_INSTANCE,%s)", v(m.getMethod()));
        case MethodHandle.INVOKE_STATIC:
            return String.format("new MethodHandle(MethodHandle.INVOKE_STATIC,%s)", v(m.getMethod()));
        case MethodHandle.INVOKE_CONSTRUCTOR:
            return String.format("new MethodHandle(MethodHandle.INVOKE_CONSTRUCTOR,%s)", v(m.getMethod()));
        case MethodHandle.INVOKE_DIRECT:
            return String.format("new MethodHandle(MethodHandle.INVOKE_DIRECT,%s)", v(m.getMethod()));
        case MethodHandle.INVOKE_INTERFACE:
            return String.format("new MethodHandle(MethodHandle.INVOKE_INTERFACE,%s)", v(m.getMethod()));
        default:
            throw new RuntimeException();
        }
    }

    public static String v(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + Utf8Utils.escapeString(s) + "\"";
    }

    public static String v(DexType t) {
        return "new DexType(" + v(t.desc) + ")";

    }

    public static String v(int[] vs) {
        StringBuilder sb = new StringBuilder("new int[]{ ");
        boolean first = true;
        for (int obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(obj);
        }
        return sb.append("}").toString();
    }

    public static String v(byte[] vs) {
        StringBuilder sb = new StringBuilder("new byte[]{ ");
        boolean first = true;
        for (byte obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("(byte)").append(obj);
        }
        return sb.append("}").toString();
    }

    public static String v(String[] vs) {
        if (vs == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("new String[]{ ");
        boolean first = true;
        for (String obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(obj));
        }
        return sb.append("}").toString();
    }

    public static String v(Object[] vs) {
        StringBuilder sb = new StringBuilder("new Object[]{ ");
        boolean first = true;
        for (Object obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(obj));
        }
        return sb.append("}").toString();
    }

    public static String v(CallSite callSite) {
        StringBuilder sb = new StringBuilder()
                .append("new CallSite(")
                .append(v(callSite.getName()))
                .append(", ")
                .append(v(callSite.getBootstrapMethodHandler()))
                .append(", ")
                .append(v(callSite.getMethodName()))
                .append(", ")
                .append(v(callSite.getMethodProto()));
        Object[] extraArguments = callSite.getExtraArguments();
        if (extraArguments != null && extraArguments.length > 0) {
            for (Object arg : extraArguments) {
                sb.append(", ").append(v(arg));
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String v(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return v((String) obj);
        }

        if (obj instanceof DexType) {
            return v((DexType) obj);
        }

        if (obj instanceof Method) {
            return v((Method) obj);
        }
        if (obj instanceof Field) {
            return v((Field) obj);
        }
        if (obj instanceof Proto) {
            return v((Proto) obj);
        }
        if (obj instanceof MethodHandle) {
            return v((MethodHandle) obj);
        }
        if (obj instanceof CallSite) {
            return v((CallSite) obj);
        }

        if (obj instanceof Integer) {
            return " Integer.valueOf(" + obj + ")";
        }
        if (obj instanceof Long) {
            return "Long.valueOf(" + obj + "L)";
        }
        if (obj instanceof Float) {
            return "Float.valueOf(" + obj + "F)";
        }
        if (obj instanceof Double) {
            return "Double.valueOf(" + obj + "D)";
        }
        if (obj instanceof Short) {
            return "Short.valueOf((short)" + obj + ")";
        }
        if (obj instanceof Byte) {
            return "Byte.valueOf((byte)" + obj + ")";
        }
        if (obj instanceof Character) {
            return "Character.valueOf('" + obj + "')";
        }
        if (obj instanceof Boolean) {
            return "Boolean.valueOf(" + obj + ")";
        }
        if (obj instanceof int[]) {
            StringBuilder sb = new StringBuilder("new int[]{ ");
            boolean first = true;
            for (int i : (int[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i);
            }
            return sb.append("}").toString();
        }
        if (obj instanceof short[]) {
            StringBuilder sb = new StringBuilder("new short[]{ ");
            boolean first = true;
            for (int i : (short[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append("(short)").append(i);
            }
            return sb.append("}").toString();
        }
        if (obj instanceof long[]) {
            StringBuilder sb = new StringBuilder("new long[]{ ");
            boolean first = true;
            for (long i : (long[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i).append("L");
            }
            return sb.append("}").toString();
        }
        if (obj instanceof float[]) {
            StringBuilder sb = new StringBuilder("new float[]{ ");
            boolean first = true;
            for (float i : (float[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i).append("F");
            }
            return sb.append("}").toString();
        }
        return null;
    }

}
