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
package com.googlecode.dex2jar.reader;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.DataIn;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;

/**
 * 读取注解
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexAnnotationReader {

    private static final int VISIBILITY_BUILD = 0;

    // private static final int VISIBILITY_RUNTIME = 1;
    // private static final int VISIBILITY_SYSTEM = 2;

    /**
     * 处理
     * 
     * @param in
     *            输入流
     * @param daa
     */
    public static void accept(DexFileReader dex, DataIn in, DexAnnotationAble daa) {
        int size = in.readUIntx();
        for (int j = 0; j < size; j++) {
            int annotation_off = in.readUIntx();
            in.pushMove(annotation_off);
            try {
                int visible_i = in.readUByte();
                int type_idx = (int) in.readULeb128();
                String type = dex.getType(type_idx);
                DexAnnotationVisitor dav = daa.visitAnnotation(type, visible_i != VISIBILITY_BUILD);
                if (dav != null) {
                    long sizex = in.readULeb128();
                    for (int k = 0; k < sizex; k++) {
                        int name_idx = (int) in.readULeb128();
                        String name = dex.getString(name_idx);
                        Object o = Constant.ReadConstant(dex, in);
                        doAccept(dav, name, o);
                    }
                    dav.visitEnd();
                }
            } finally {
                in.pop();
            }
        }
    }

    private static void doAccept(DexAnnotationVisitor dav, String name, Object o) {
        if (o instanceof Object[]) {
            DexAnnotationVisitor arrayVisitor = dav.visitArray(name);
            if (arrayVisitor != null) {
                Object[] array = (Object[]) o;
                for (Object e : array) {
                    doAccept(arrayVisitor, null, e);
                }
                arrayVisitor.visitEnd();
            }
        } else if (o instanceof Annotation) {
            Annotation ann = (Annotation) o;
            DexAnnotationVisitor av = dav.visitAnnotation(name, ann.type);
            if (av != null) {
                for (Annotation.Item item : ann.items) {
                    doAccept(av, item.name, item.value);
                }
                av.visitEnd();
            }
        } else if (o instanceof Field) {
            Field f = (Field) o;
            dav.visitEnum(name, f.getType(), f.getName());
        } else {
            dav.visit(name, o);
        }
    }

}
