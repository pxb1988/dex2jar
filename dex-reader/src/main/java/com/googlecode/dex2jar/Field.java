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
 * 成员
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Field {
    /**
     * 修饰符
     */
    private int access_flags;
    /**
     * 成员名
     */
    private String name;
    /**
     * 所有者
     */
    private String owner;
    /**
     * 成员类型
     */
    private String type;

    public Field(String owner, String name, String type) {
        this.owner = owner;
        this.type = type;
        this.name = name;
    }

    /**
     * @see #access_flags
     * @return the access_flags
     */
    public int getAccessFlags() {
        return access_flags;
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
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @see #access_flags
     * @param access_flags
     */
    public void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getOwner() + "." + this.getName() + " " + this.getType();
    }
}
