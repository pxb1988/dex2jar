/*
 * dex2jar - Tools to work with android .dex and java .class files
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
package com.googlecode.dex2jar.ir.ts;

import java.util.List;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ts.TypeAnalyze.DefTypeRef;

/**
 * Type and correct Exprs
 * 
 * @author Panxiaobo
 * 
 */
public class LocalType implements Transformer {

    public static Type typeOf(Value local) {
        return local.typeRef == null ? null : local.typeRef.get();
    }

    @Override
    public void transform(IrMethod irMethod) {
        TypeAnalyze ta = new TypeAnalyze(irMethod);
        List<DefTypeRef> refs = ta.analyze();

        for (DefTypeRef ref : refs) {
            Type type = null;

            if (ref.providerAs.size() > 0) {
                type = ref.providerAs.iterator().next();
            }
            if (ref.useAs.size() > 0) {
                Type useType = ref.useAs.iterator().next();
                if (type == null) {
                    type = useType;
                } else {
                    switch (useType.getSort()) {
                    case Type.ARRAY:
                    case Type.OBJECT:
                        if (type.getSort() != Type.ARRAY) {
                            type = useType;
                        }
                        break;
                    case Type.FLOAT:
                    case Type.DOUBLE:
                        type = useType;
                    default:
                    }
                }
            }

            if (type == null) {
                System.err.println(ref);
                continue;
            }
            ref.type = type;

            if (ref.value.vt == VT.CONSTANT) {
                Constant cst = (Constant) ref.value;
                switch (type.getSort()) {
                case Type.ARRAY:
                case Type.OBJECT:
                    if (Integer.valueOf(0).equals(cst.value)) {
                        cst.value = Constant.Null;
                    }
                    break;
                case Type.FLOAT:
                    if (!(cst.value instanceof Float)) {
                        cst.value = Float.intBitsToFloat(((Number) cst.value).intValue());
                    }
                    break;
                case Type.DOUBLE:
                    if (!(cst.value instanceof Double)) {
                        cst.value = Double.longBitsToDouble(((Number) cst.value).longValue());
                    }
                    break;
                default:
                }

            }

            ref.arrayValues = null;
            ref.arryRoots = null;
            ref.froms = null;
            ref.tos = null;
            ref.providerAs = null;
            ref.tos = null;
            ref.useAs = null;
            ref.value = null;
            ref.sameValues = null;
        }
    }
}
