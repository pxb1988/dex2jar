/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar;

/**
 * 方法
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Method {
    /**
     * 描述
     */
    private String desc;
    /**
     * 方法名
     */
    private String name;
    /**
     * 方法所有者
     */
    private String owner;
    /**
     * 参数类型
     */
    private String[] parameterTypes;

    /**
     * 返回类型
     */
    private String returnType;

    public Method(String owner, String name, String[] parameterTypes, String returnType) {
        this.owner = owner;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return the parameterTypes
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getOwner() + "." + this.getName() + this.getDesc();
    }
}
