/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Idx;
import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class ProtoIdItem extends BaseItem implements Comparable<ProtoIdItem> {
    @Idx
    public final StringIdItem shorty;
    @Idx
    public final TypeIdItem ret;
    @Off
    public final TypeListItem parameters;

    public ProtoIdItem(TypeListItem parameters, TypeIdItem ret, StringIdItem shorty) {
        super();
        this.parameters = parameters;
        this.ret = ret;
        this.shorty = shorty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((ret == null) ? 0 : ret.hashCode());
        result = prime * result + ((shorty == null) ? 0 : shorty.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProtoIdItem other = (ProtoIdItem) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (ret == null) {
            if (other.ret != null)
                return false;
        } else if (!ret.equals(other.ret))
            return false;
        if (shorty == null) {
            if (other.shorty != null)
                return false;
        } else if (!shorty.equals(other.shorty))
            return false;
        return true;
    }

    @Override
    public int place(int offset) {
        return offset + 0x0c;
    }

    @Override
    public int compareTo(ProtoIdItem o) {
        int x = ret.compareTo(o.ret);
        if (x != 0) {
            return x;
        }
        return parameters.compareTo(o.parameters);
    }

    @Override
    public void write(DataOut out) {
        out.uint("shorty_idx", shorty.index);
        out.uint("return_type_idx", ret.index);
        // can't use zero-size type_list_item in libart
        out.uint("parameters_off", (parameters == null || parameters.items.size() == 0) ? 0 : parameters.offset);
    }

}