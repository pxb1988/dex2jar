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

import java.util.Arrays;

public class Proto {
    public Proto(String[] parameterTypes, String returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    /**
     * descriptor of the method, this will build after {@link #getDesc()}.
     */
    private String desc;
    /**
     * parameter types of the method, in TypeDescriptor format.
     */
    private String[] parameterTypes;

    /**
     * return type of the method, in TypeDescriptor format.
     */
    private String returnType;

    /**
     * @return the parameterTypes
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getDesc() {
        if (desc == null) {
            StringBuilder ps = new StringBuilder("(");
            if (parameterTypes != null) {
                for (String t : parameterTypes) {
                    ps.append(t);
                }
            }
            ps.append(")").append(returnType);
            desc = ps.toString();
        }
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Proto proto = (Proto) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parameterTypes, proto.parameterTypes)) return false;
        return returnType != null ? returnType.equals(proto.returnType) : proto.returnType == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(parameterTypes);
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }
}
