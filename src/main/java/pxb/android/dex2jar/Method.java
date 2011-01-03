/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android.dex2jar;

/**
 * 方法
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class Method {
    /**
     * 修饰符
     */
    private int access_flags;

    private Dex dex;
    /**
     * 方法名
     */
    private String name;
    /**
     * 方法所有者
     */
    private String owner;
    /**
     * 参数和返回值
     */
    private Proto type;
    /**
     * 方法编号
     */
    private int type_idx;

    public Method(Dex dex, DataIn in) {
        int owner_idx = in.readShortx();
        type_idx = in.readShortx();
        int name_idx = in.readIntx();

        owner = dex.getType(owner_idx);
        // type = dex.getProto(type_idx);
        name = dex.getString(name_idx);
        this.dex = dex;
    }

    /**
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
    public Proto getType() {
        if (type == null) {
            type = dex.getProto(type_idx);
        }
        return type;
    }

    /**
     * @param access_flags
     *            the access_flags to set
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
        return this.getOwner() + "." + this.getName() + this.getType();
    }
}
