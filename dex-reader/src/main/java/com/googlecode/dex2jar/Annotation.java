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

import java.util.ArrayList;
import java.util.List;

/**
 * 注解
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Annotation {
    public static class Item {
        public String name;

        public Object value;

        /**
         * @param name
         * @param value
         */
        public Item(String name, Object value) {
            super();
            this.name = name;
            this.value = value;
        }
    }

    public List<Item> items = new ArrayList<Item>();

    public String type;
    public boolean visible;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(type);
        if (items.size() > 0) {
            sb.append('(');
            boolean first = true;
            for (Item it : items) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }

                if ("value".equals(it.name) && items.size() == 1) {
                    // not print value=
                } else {
                    sb.append(it.name).append('=');
                }
                if (it.value instanceof Annotation) {
                    Annotation a = (Annotation) it.value;
                    if (a.type == null) {// array
                        sb.append('{');
                        boolean first2 = true;
                        for (Item it2 : a.items) {
                            if (first2) {
                                first2 = false;
                            } else {
                                sb.append(',');
                            }
                            sb.append(it2.value);
                        }
                        sb.append('}');
                    } else {
                        sb.append(it.value);
                    }
                } else {
                    sb.append(it.value);
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * @param type
     * @param visitable
     */
    public Annotation(String type, boolean visitable) {
        super();
        this.type = type;
        this.visible = visitable;
    }
}