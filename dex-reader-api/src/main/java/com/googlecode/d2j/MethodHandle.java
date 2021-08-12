package com.googlecode.d2j;

import java.util.Objects;

public class MethodHandle {

    public static final int STATIC_PUT = 0x00;

    public static final int STATIC_GET = 0x01;

    public static final int INSTANCE_PUT = 0x02;

    public static final int INSTANCE_GET = 0x03;

    public static final int INVOKE_STATIC = 0x04;

    public static final int INVOKE_INSTANCE = 0x05;

    public static final int INVOKE_CONSTRUCTOR = 0x06;

    public static final int INVOKE_DIRECT = 0x07;

    public static final int INVOKE_INTERFACE = 0x08;

    private final int type;

    private Field field;

    private Method method;

    public MethodHandle(int type, Field field) {
        this.type = type;
        this.field = field;
    }

    public MethodHandle(int type, Method method) {
        this.type = type;
        this.method = method;
    }

    public MethodHandle(int type, Field field, Method method) {
        this.type = type;
        this.field = field;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodHandle that = (MethodHandle) o;

        if (type != that.type) {
            return false;
        }
        if (!Objects.equals(field, that.field)) {
            return false;
        }
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (field != null ? field.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    public int getType() {
        return type;
    }

    public Field getField() {
        return field;
    }

    public Method getMethod() {
        return method;
    }

}
