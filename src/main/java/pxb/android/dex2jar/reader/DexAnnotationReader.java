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
package pxb.android.dex2jar.reader;

import org.objectweb.asm.AnnotationVisitor;

import pxb.android.dex2jar.Annotation;
import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.visitors.DexAnnotationAble;

/**
 * 读取注解
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexAnnotationReader {
    private Dex dex;


    private static final int VISIBILITY_BUILD = 0;

    // private static final int VISIBILITY_RUNTIME = 1;
    // private static final int VISIBILITY_SYSTEM = 2;

    /**
     * @param dex
     *            dex文件
     */
    public DexAnnotationReader(Dex dex) {
        super();
        this.dex = dex;
    }

    /**
     * 处理
     * 
     * @param in
     *            输入流
     * @param daa
     */
    public void accept(DataIn in, DexAnnotationAble daa) {
        int size = in.readIntx();
        for (int j = 0; j < size; j++) {
            int field_annotation_offset = in.readIntx();
            in.pushMove(field_annotation_offset);
            try {
                int visible_i = in.readByte();
                int type_idx = (int) in.readUnsignedLeb128();
                String type = dex.getType(type_idx);
                AnnotationVisitor dav = daa.visitAnnotation(type, visible_i != VISIBILITY_BUILD);
                if (dav != null) {
                    long sizex = in.readUnsignedLeb128();
                    for (int k = 0; k < sizex; k++) {
                        int name_idx = (int) in.readUnsignedLeb128();
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

    private static void doAccept(AnnotationVisitor dav, String name, Object o) {
        if (o instanceof Object[]) {
            AnnotationVisitor arrayVisitor = dav.visitArray(name);
            if (arrayVisitor != null) {
                Object[] array = (Object[]) o;
                for (Object e : array) {
                    doAccept(arrayVisitor, null, e);
                }
                arrayVisitor.visitEnd();
            }
        } else if (o instanceof Annotation) {
            Annotation ann = (Annotation) o;
            AnnotationVisitor av = dav.visitAnnotation(name, ann.type);
            if (av != null) {
                for (Annotation.Item item : ann.items) {
                    doAccept(av, item.name, item.value);
                }
                av.visitEnd();
            }
        } else {
            dav.visit(name, o);
        }
    }

}
