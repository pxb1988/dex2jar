/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.dex2jar.util;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.OdexOpcodes;

/**
 * a util to dump the name of opcode
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public final class DexOpcodeDump {

    private static final Map<Integer, String> map;
    static {
        java.lang.reflect.Field[] fs = OdexOpcodes.class.getFields();
        map = new HashMap<Integer, String>(fs.length);
        for (java.lang.reflect.Field f : fs) {
            f.setAccessible(true);
            if (f.getName().startsWith("OP_")) {
                try {
                    map.put(f.getInt(null), f.getName().substring(3));
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 
     * dump the name of a opcode
     * 
     * @param opcode
     *            the opcode
     */
    public static final String dump(int opcode) {
        String s = map.get(opcode);
        if (s == null) {
            throw new DexException("can't dump opcode %08x", opcode);
        }
        return s;
    }
}
