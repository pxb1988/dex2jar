/*
 * Copyright (c) 2009-2017 Panxiaobo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.d2j;

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

    private int type;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodHandle that = (MethodHandle) o;

        if (type != that.type) return false;
        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        return method != null ? method.equals(that.method) : that.method == null;
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
