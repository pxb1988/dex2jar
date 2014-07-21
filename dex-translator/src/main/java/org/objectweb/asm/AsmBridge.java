/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
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
package org.objectweb.asm;

import org.objectweb.asm.tree.MethodNode;

public class AsmBridge {
    public static boolean isMethodWriter(MethodVisitor mv) {
        return mv instanceof MethodWriter;
    }

    public static int sizeOfMethodWriter(MethodVisitor mv) {
        MethodWriter mw = (MethodWriter) mv;
        return mw.getSize();
    }

    public static void removeMethodWriter(MethodVisitor mv) {
        // mv must be the last element
        MethodWriter mw = (MethodWriter) mv;
        ClassWriter cw = mw.cw;
        MethodWriter p = cw.firstMethod;
        if (p == mw) {
            cw.firstMethod = null;
            if (cw.lastMethod == mw) {
                cw.lastMethod = null;
            }
        } else {
            while (p != null) {
                if (p.mv == mw) {
                    p.mv = mw.mv;
                    if (cw.lastMethod == mw) {
                        cw.lastMethod = p;
                    }
                    break;
                } else {
                    p = (MethodWriter) p.mv;
                }
            }
        }
    }

    public static void replaceMethodWriter(MethodVisitor mv, MethodNode mn) {
        MethodWriter mw = (MethodWriter) mv;
        ClassWriter cw = mw.cw;
        mn.accept(cw);
        removeMethodWriter(mv);
    }
}
