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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * 读取dex文件
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexFileReader {
    private static final byte[] DEX_FILE_MAGIC = new byte[] { 0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00 };

    /* default */static final int ENDIAN_CONSTANT = 0x12345678;
    /* default */static final int REVERSE_ENDIAN_CONSTANT = 0x78563412;

    private int class_defs_off;

    private int class_defs_size;

    private int field_ids_off;
    private int field_ids_size;
    private DataIn in;
    private int method_ids_off;
    private int method_ids_size;

    private int proto_ids_off;
    private int proto_ids_size;
    private int string_ids_off;
    private int string_ids_size;
    private int type_ids_off;
    private int type_ids_size;

    public static final int SKIP_DEBUG = 0x00000001;
    public static final int SKIP_CODE = 0x00000002;

    /**
     * 
     * @param data
     * 
     */
    public DexFileReader(byte[] data) {
        DataIn in = new EndianDataIn(data);

        // { 0x64 0x65 0x78 0x0a 0x30 0x33 0x35 0x00 } = "dex\n035\0"
        byte[] magic = in.readBytes(8);

        if (!Arrays.equals(magic, DEX_FILE_MAGIC)) {
            throw new DexException("not support magic.");
        }

        // skip uint checksum
        // and 20 bytes signature
        // and uint file_size
        // and uint header_size 0x70
        in.skip(4 + 20 + 4 + 4);

        int endian_tag = in.readUIntx();
        if (endian_tag == REVERSE_ENDIAN_CONSTANT) {
            in = new ReverseEndianDataIn(data, in.getCurrentPosition());
        } else if (endian_tag != ENDIAN_CONSTANT) {
            throw new DexException("not support endian_tag");
        }

        this.in = in;

        // skip uint link_size
        // and uint link_off
        // and uint map_off
        in.skip(4 + 4 + 4);

        string_ids_size = in.readUIntx();
        string_ids_off = in.readUIntx();
        type_ids_size = in.readUIntx();
        type_ids_off = in.readUIntx();
        proto_ids_size = in.readUIntx();
        proto_ids_off = in.readUIntx();
        field_ids_size = in.readUIntx();
        field_ids_off = in.readUIntx();
        method_ids_size = in.readUIntx();
        method_ids_off = in.readUIntx();
        class_defs_size = in.readUIntx();
        class_defs_off = in.readUIntx();
        // skip uint data_size data_off
    }

    public DexFileReader(File f) throws IOException {
        this(FileUtils.readFileToByteArray(f));
    }

    public DexFileReader(InputStream in) throws IOException {
        this(IOUtils.toByteArray(in));
    }

    public void accept(DexFileVisitor dv) {
        this.accept(dv, 0);
    }

    /**
     * 
     * @param dv
     * @param config
     *            {@link #SKIP_CODE}, {@link #SKIP_DEBUG}, {@link #SKIP_FIELD}, {@link #SKIP_METHOD}
     */
    public void accept(DexFileVisitor dv, int config) {
        DataIn in = this.in;
        for (int cid = 0; cid < class_defs_size; cid++) {
            int idxOffset = this.class_defs_off + cid * 32;
            in.pushMove(idxOffset);
            String className = null;
            try {
                className = this.getType(in.readUIntx());
                int access_flags = in.readUIntx();
                int superclass_idx = in.readUIntx();
                String superClassName = superclass_idx == -1 ? null : this.getType(superclass_idx);
                // 获取接口
                String[] interfaceNames = null;
                {
                    int interfaces_off = in.readUIntx();
                    if (interfaces_off != 0) {
                        in.pushMove(interfaces_off);
                        try {
                            int size = in.readUIntx();
                            interfaceNames = new String[size];
                            for (int i = 0; i < size; i++) {
                                interfaceNames[i] = getType(in.readUShortx());
                            }
                        } finally {
                            in.pop();
                        }
                    }
                }
                DexClassVisitor dcv = dv.visit(access_flags, className, superClassName, interfaceNames);
                if (dcv != null)// 不处理
                {
                    acceptClass(dv, dcv, className, config);
                }
            } finally {
                in.pop();
            }
        }
        dv.visitEnd();
    }

    private void acceptClass(DexFileVisitor dv, DexClassVisitor dcv, String className, int config) {
        DataIn in = this.in;

        // 获取源文件
        {
            int source_file_idx = in.readUIntx();
            if (source_file_idx != -1)
                dcv.visitSource(this.getString(source_file_idx));
        }
        // 获取注解
        Map<Integer, Integer> fieldAnnotationPositions = new HashMap<Integer, Integer>();
        Map<Integer, Integer> methodAnnotationPositions = new HashMap<Integer, Integer>();
        Map<Integer, Integer> paramAnnotationPositions = new HashMap<Integer, Integer>();
        {
            int annotations_off = in.readUIntx();
            if (annotations_off != 0) {
                in.pushMove(annotations_off);
                try {
                    int class_annotations_off = in.readUIntx();
                    if (class_annotations_off != 0) {
                        in.pushMove(class_annotations_off);
                        try {
                            DexAnnotationReader.accept(this, in, dcv);
                        } catch (Exception e) {
                            throw new RuntimeException("error on reading Annotation of class " + className, e);
                        } finally {
                            in.pop();
                        }
                    }

                    int field_annotation_size = in.readUIntx();
                    int method_annotation_size = in.readUIntx();
                    int parameter_annotation_size = in.readUIntx();
                    for (int i = 0; i < field_annotation_size; i++) {
                        int field_idx = in.readUIntx();
                        int field_annotations_offset = in.readUIntx();
                        fieldAnnotationPositions.put(field_idx, field_annotations_offset);
                    }
                    for (int i = 0; i < method_annotation_size; i++) {
                        int method_idx = in.readUIntx();
                        int method_annotation_offset = in.readUIntx();
                        methodAnnotationPositions.put(method_idx, method_annotation_offset);
                    }
                    for (int i = 0; i < parameter_annotation_size; i++) {
                        int method_idx = in.readUIntx();
                        int parameter_annotation_offset = in.readUIntx();
                        paramAnnotationPositions.put(method_idx, parameter_annotation_offset);
                    }
                } finally {
                    in.pop();
                }
            }
        }

        int class_data_off = in.readUIntx();

        int static_values_off = in.readUIntx();

        if (class_data_off != 0) {
            in.pushMove(class_data_off);
            try {
                int static_fields = (int) in.readULeb128();
                int instance_fields = (int) in.readULeb128();
                int direct_methods = (int) in.readULeb128();
                int virtual_methods = (int) in.readULeb128();
                {
                    int lastIndex = 0;
                    {
                        Object[] constant = null;
                        {
                            if (static_values_off != 0) {
                                in.pushMove(static_values_off);
                                try {
                                    int size = (int) in.readULeb128();
                                    constant = new Object[size];
                                    for (int i = 0; i < size; i++) {
                                        constant[i] = Constant.ReadConstant(this, in);
                                    }
                                } finally {
                                    in.pop();
                                }
                            }
                        }
                        for (int i = 0; i < static_fields; i++) {
                            Object value = null;
                            if (constant != null && i < constant.length) {
                                value = constant[i];
                            }
                            lastIndex = acceptField(lastIndex, dcv, fieldAnnotationPositions, value);
                        }
                    }
                    lastIndex = 0;
                    for (int i = 0; i < instance_fields; i++) {
                        lastIndex = acceptField(lastIndex, dcv, fieldAnnotationPositions, null);
                    }
                    lastIndex = 0;
                    for (int i = 0; i < direct_methods; i++) {
                        lastIndex = acceptMethod(lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions,
                                config);
                    }
                    lastIndex = 0;
                    for (int i = 0; i < virtual_methods; i++) {
                        lastIndex = acceptMethod(lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions,
                                config);
                    }
                }
            } finally {
                in.pop();
            }
        }
        dcv.visitEnd();
    }

    /* default */Field getField(int id) {
        if (id >= this.field_ids_size || id < 0)
            throw new IllegalArgumentException("Id out of bound");
        DataIn in = this.in;
        int idxOffset = this.field_ids_off + id * 8;
        in.pushMove(idxOffset);
        try {
            int owner_idx = in.readUShortx();
            int type_idx = in.readUShortx();
            int name_idx = in.readUIntx();
            return new Field(getType(owner_idx), getString(name_idx), getType(type_idx));
        } finally {
            in.pop();
        }

    }

    /* default */Method getMethod(int method_idx) {
        if (method_idx >= this.method_ids_size || method_idx < 0)
            throw new IllegalArgumentException("Id out of bound");
        DataIn in = this.in;
        int idxOffset = this.method_ids_off + method_idx * 8;
        in.pushMove(idxOffset);
        try {
            int owner_idx = in.readUShortx();
            int proto_idx = in.readUShortx();
            int name_idx = in.readUIntx();
            String[] parameterTypes;
            String returnType;
            {
                if (proto_idx >= proto_ids_size) {
                    throw new IllegalArgumentException("Id out of bound");
                }
                int proto_off = this.proto_ids_off + proto_idx * 12;
                in.pushMove(proto_off);
                try {
                    in.skip(4);// skip shorty_idx uint
                    int return_type_idx = in.readUIntx();
                    int parameters_off = in.readUIntx();

                    returnType = getType(return_type_idx);

                    if (parameters_off != 0) {
                        in.pushMove(parameters_off);
                        try {
                            int size = in.readUIntx();
                            parameterTypes = new String[size];
                            for (int i = 0; i < size; i++) {
                                parameterTypes[i] = getType(in.readUShortx());
                            }
                        } finally {
                            in.pop();
                        }
                    } else {
                        parameterTypes = new String[0];
                    }
                } finally {
                    in.pop();
                }
            }
            return new Method(getType(owner_idx), getString(name_idx), parameterTypes, returnType);
        } finally {
            in.pop();
        }

    }

    /**
     * 一个String id为4字节
     * 
     */
    /* default */String getString(int id) {
        if (id >= this.string_ids_size || id < 0)
            throw new IllegalArgumentException("Id out of bound");
        DataIn in = this.in;
        int idxOffset = this.string_ids_off + id * 4;
        in.pushMove(idxOffset);
        try {
            int offset = in.readIntx();
            in.pushMove(offset);
            try {
                int length = (int) in.readULeb128();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
                for (int b = in.readByte(); b != 0; b = in.readByte()) {
                    baos.write(b);
                }
                return new String(baos.toByteArray(), UTF8);
            } finally {
                in.pop();
            }
        } finally {
            in.pop();
        }
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /* default */String getType(int id) {
        if (id >= this.type_ids_size || id < 0)
            throw new IllegalArgumentException("Id out of bound");
        DataIn in = this.in;
        int idxOffset = this.type_ids_off + id * 4;
        in.pushMove(idxOffset);
        try {
            int offset = in.readIntx();
            return this.getString(offset);
        } finally {
            in.pop();
        }
    }

    /**
     * 访问成员
     * 
     * @param lastIndex
     * @param dcv
     * @param fieldAnnotationPositions
     * @param value
     * @return
     */
    protected int acceptField(int lastIndex, DexClassVisitor dcv, Map<Integer, Integer> fieldAnnotationPositions,
            Object value) {
        DataIn in = this.in;
        int diff = (int) in.readULeb128();
        int field_id = lastIndex + diff;
        Field field = getField(field_id);
        int field_access_flags = (int) in.readULeb128();
        // //////////////////////////////////////////////////////////////
        DexFieldVisitor dfv = dcv.visitField(field_access_flags, field, value);
        if (dfv != null) {
            Integer annotation_offset = fieldAnnotationPositions.get(field_id);
            if (annotation_offset != null) {
                in.pushMove(annotation_offset);
                try {
                    DexAnnotationReader.accept(this, in, dfv);
                } catch (Exception e) {
                    throw new DexException(e, "while accept annotation in field:%s.", field.toString());
                } finally {
                    in.pop();
                }
            }
            dfv.visitEnd();
        }
        // //////////////////////////////////////////////////////////////
        return field_id;
    }

    /**
     * 访问方法
     * 
     * @param lastIndex
     * @param cv
     * @param methodAnnos
     * @param parameterAnnos
     * @return
     */
    protected int acceptMethod(int lastIndex, DexClassVisitor cv, Map<Integer, Integer> methodAnnos,
            Map<Integer, Integer> parameterAnnos, int config) {
        DataIn in = this.in;
        int diff = (int) in.readULeb128();
        int method_access_flags = (int) in.readULeb128();
        int code_off = (int) in.readULeb128();
        int method_id = lastIndex + diff;
        Method method = getMethod(method_id);
        try {
            DexMethodVisitor dmv = cv.visitMethod(method_access_flags, method);
            if (dmv != null) {
                {
                    Integer annotation_offset = methodAnnos.get(method_id);
                    if (annotation_offset != null) {
                        in.pushMove(annotation_offset);
                        try {
                            DexAnnotationReader.accept(this, in, dmv);
                        } catch (Exception e) {
                            throw new DexException(e, "while accept annotation in method:%s.", method.toString());
                        } finally {
                            in.pop();
                        }
                    }
                }
                {
                    Integer parameter_annotation_offset = parameterAnnos.get(method_id);
                    if (parameter_annotation_offset != null) {
                        in.pushMove(parameter_annotation_offset);
                        try {
                            int sizeJ = in.readUIntx();
                            for (int j = 0; j < sizeJ; j++) {
                                int field_annotation_offset = in.readUIntx();
                                in.pushMove(field_annotation_offset);
                                try {
                                    DexAnnotationAble dpav = dmv.visitParameterAnnotation(j);
                                    if (dpav != null)
                                        DexAnnotationReader.accept(this, in, dpav);
                                } catch (Exception e) {
                                    throw new DexException(e,
                                            "while accept parameter annotation in method:[%s], parameter:[%d]",
                                            method.toString(), j);
                                } finally {
                                    in.pop();
                                }
                            }
                        } finally {
                            in.pop();
                        }
                    }
                }
                if (code_off != 0 && (0 == (SKIP_CODE & config))) {
                    in.pushMove(code_off);
                    try {
                        DexCodeVisitor dcv = dmv.visitCode();
                        if (dcv != null) {
                            try {
                                new DexCodeReader(this, in, (0 != (DexOpcodes.ACC_STATIC & method_access_flags)),
                                        method).accept(dcv, config);
                            } catch (Exception e) {
                                throw new DexException(e, "while accept code in method:[%s]", method.toString());
                            }
                        }
                    } finally {
                        in.pop();
                    }
                }
                dmv.visitEnd();
            }
        } catch (Exception e) {
            throw new DexException(e, "while accept method:[%s]", method.toString());
        }

        return method_id;
    }
}
