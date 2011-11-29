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
 * a light weight version of org.objectweb.asm.Label
 * 
 * @author Pan
 * 
 */
public class DexLabel {
    public Object info;

    private int offset = -1;

    public DexLabel(int offset) {
        super();
        this.offset = offset;
    }

    public DexLabel() {
        super();
    }

    public String toString() {
        if (offset >= 0) {
            return String.format("L%x", offset);
        }
        return String.format("L%08x", this.hashCode());
    }
}
