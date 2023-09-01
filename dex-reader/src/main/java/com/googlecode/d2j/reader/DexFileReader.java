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
package com.googlecode.d2j.reader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.googlecode.d2j.*;
import com.googlecode.d2j.node.DexAnnotationNode;
import com.googlecode.d2j.util.Mutf8;
import com.googlecode.d2j.visitors.*;

import static com.googlecode.d2j.DexConstants.*;

/**
 * Open and read a dex file.this is the entrance of dex-reader. to read a dex/odex, use the following code:
 * 
 * <pre>
 * DexFileVisitor visitor = new xxxFileVisitor();
 * DexFileReader reader = new DexFileReader(dexFile);
 * reader.accept(visitor);
 * </pre>
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexFileReader implements BaseDexFileReader {
    /**
     * skip debug infos in dex file.
     */
    public static final int SKIP_DEBUG = 1;
    /**
     * skip code info in dex file, this indicate {@link #SKIP_DEBUG}
     */
    public static final int SKIP_CODE = 1 << 2;
    /**
     * skip annotation info in dex file.
     */
    public static final int SKIP_ANNOTATION = 1 << 3;
    /**
     * skip field constant in dex file.
     */
    public static final int SKIP_FIELD_CONSTANT = 1 << 4;
    /**
     * ingore read exception
     */
    public static final int IGNORE_READ_EXCEPTION = 1 << 5;
    /**
     * read all methods, even if they are glitch
     */
    public static final int KEEP_ALL_METHODS = 1 << 6;
    /**
     * keep clinit method when {@link #SKIP_DEBUG}
     */
    public static final int KEEP_CLINIT = 1 << 7;

    /**
     * keep clinit method when {@link #SKIP_DEBUG}
     */
    public static final int SKIP_EXCEPTION = 1 << 8;

    // private static final int REVERSE_ENDIAN_CONSTANT = 0x78563412;

    static final int DBG_END_SEQUENCE = 0x00;
    static final int DBG_ADVANCE_PC = 0x01;
    static final int DBG_ADVANCE_LINE = 0x02;
    static final int DBG_START_LOCAL = 0x03;
    static final int DBG_START_LOCAL_EXTENDED = 0x04;
    static final int DBG_END_LOCAL = 0x05;
    static final int DBG_RESTART_LOCAL = 0x06;
    static final int DBG_SET_PROLOGUE_END = 0x07;
    static final int DBG_SET_EPILOGUE_BEGIN = 0x08;
    static final int DBG_SET_FILE = 0x09;
    static final int DBG_FIRST_SPECIAL = 0x0a;
    static final int DBG_LINE_BASE = -4;
    static final int DBG_LINE_RANGE = 15;

    private static final int ENDIAN_CONSTANT = 0x12345678;
    private static final int VALUE_BYTE = 0x00;
    private static final int VALUE_SHORT = 0x02;
    private static final int VALUE_CHAR = 0x03;
    private static final int VALUE_INT = 0x04;
    private static final int VALUE_LONG = 0x06;
    private static final int VALUE_FLOAT = 0x10;
    private static final int VALUE_DOUBLE = 0x11;
    private static final int VALUE_METHOD_TYPE = 0x15;
    private static final int VALUE_METHOD_HANDLE = 0x16;
    private static final int VALUE_STRING = 0x17;
    private static final int VALUE_TYPE = 0x18;
    private static final int VALUE_FIELD = 0x19;
    private static final int VALUE_METHOD = 0x1a;
    private static final int VALUE_ENUM = 0x1b;
    private static final int VALUE_ARRAY = 0x1c;
    private static final int VALUE_ANNOTATION = 0x1d;
    private static final int VALUE_NULL = 0x1e;
    private static final int VALUE_BOOLEAN = 0x1f;

    private static final int TYPE_CALL_SITE_ID_ITEM = 0x0007;
    private static final int TYPE_METHOD_HANDLE_ITEM = 0x0008;

    final ByteBuffer annotationSetRefListIn;
    final ByteBuffer annotationsDirectoryItemIn;
    final ByteBuffer annotationSetItemIn;
    final ByteBuffer annotationItemIn;
    final ByteBuffer classDataIn;
    final ByteBuffer codeItemIn;
    final ByteBuffer encodedArrayItemIn;
    final ByteBuffer stringIdIn;
    final ByteBuffer typeIdIn;
    final ByteBuffer protoIdIn;
    final ByteBuffer fieldIdIn;
    final ByteBuffer methoIdIn;
    final ByteBuffer classDefIn;
    final ByteBuffer typeListIn;
    final ByteBuffer stringDataIn;
    final ByteBuffer debugInfoIn;
    final ByteBuffer callSiteIdIn;
    final ByteBuffer methodHandleIdIn;
    final int string_ids_size;
    final int type_ids_size;
    final int proto_ids_size;
    final int field_ids_size;
    final int method_ids_size;
    final private int class_defs_size;
    final int call_site_ids_size;
    final int method_handle_ids_size;
    final int dex_version;

    /**
     * read dex from a {@link ByteBuffer}.
     * 
     * @param in
     */
    public DexFileReader(ByteBuffer in) {
        in.position(0);
        in = in.asReadOnlyBuffer().order(ByteOrder.BIG_ENDIAN);
        int magic = in.getInt() & 0xFFFFFF00;

        final int MAGIC_DEX = 0x6465780A & 0xFFFFFF00;// hex for 'dex ', ignore the 0A
        final int MAGIC_ODEX = 0x6465790A & 0xFFFFFF00;// hex for 'dey ', ignore the 0A

        if (magic == MAGIC_DEX) {
            // ok
        } else if (magic == MAGIC_ODEX) {
            throw new DexException("Odex unsupported.");
        } else {
            throw new DexException("Magic unsupported.");
        }
        int version = in.getInt() >> 8;
        if (version < DEX_035 || version > DEX_040) {
            System.err.println("Unknown DEX version. Trying anyway...");
        }
        this.dex_version = version;
        in.order(ByteOrder.LITTLE_ENDIAN);

        // skip uint checksum
        // and 20 bytes signature
        // and uint file_size
        // and uint header_size 0x70
        skip(in, 4 + 20 + 4 + 4);

        int endian_tag = in.getInt();
        if (endian_tag != ENDIAN_CONSTANT) {
            throw new DexException("Endian_tag unsupported");
        }

        // skip uint link_size
        // and uint link_off
        skip(in, 4 + 4);

        int map_off = in.getInt();

        string_ids_size = in.getInt();
        int string_ids_off = in.getInt();
        type_ids_size = in.getInt();
        int type_ids_off = in.getInt();
        proto_ids_size = in.getInt();
        int proto_ids_off = in.getInt();
        field_ids_size = in.getInt();
        int field_ids_off = in.getInt();
        method_ids_size = in.getInt();
        int method_ids_off = in.getInt();
        class_defs_size = in.getInt();
        int class_defs_off = in.getInt();
        // skip uint data_size data_off

        int call_site_ids_off = 0;
        int call_site_ids_size = 0;
        int method_handle_ids_off = 0;
        int method_handle_ids_size = 0;
        if (dex_version >= DEX_038) {
            in.position(map_off);
            int size = in.getInt();
            for (int i = 0; i < size; i++) {
                int type = in.getShort() & 0xFFFF;
                in.getShort(); // unused;
                int item_size = in.getInt();
                int item_offset = in.getInt();
                switch (type) {
                case TYPE_CALL_SITE_ID_ITEM:
                    call_site_ids_off = item_offset;
                    call_site_ids_size = item_size;
                    break;
                case TYPE_METHOD_HANDLE_ITEM:
                    method_handle_ids_off = item_offset;
                    method_handle_ids_size = item_size;
                    break;
                default:
                    break;
                }
            }
        }
        this.call_site_ids_size = call_site_ids_size;
        this.method_handle_ids_size = method_handle_ids_size;

        stringIdIn = slice(in, string_ids_off, string_ids_size * 4);
        typeIdIn = slice(in, type_ids_off, type_ids_size * 4);
        protoIdIn = slice(in, proto_ids_off, proto_ids_size * 12);
        fieldIdIn = slice(in, field_ids_off, field_ids_size * 8);
        methoIdIn = slice(in, method_ids_off, method_ids_size * 8);
        classDefIn = slice(in, class_defs_off, class_defs_size * 32);
        callSiteIdIn = slice(in, call_site_ids_off, call_site_ids_size * 4);
        methodHandleIdIn = slice(in, method_handle_ids_off, method_handle_ids_size * 8);

        in.position(0);
        annotationsDirectoryItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        annotationSetItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        annotationItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        annotationSetRefListIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        classDataIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        codeItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        stringDataIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        encodedArrayItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        typeListIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        debugInfoIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * 
     * @param data
     *            the byte array of dex
     * @return
     */
    public DexFileReader(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    /**
     * 
     * @param file
     *            the dex file
     * @throws IOException
     */
    public DexFileReader(File file) throws IOException {
        this(file.toPath());
    }

    public DexFileReader(Path file) throws IOException {
        this(Files.readAllBytes(file));
    }

    public DexFileReader(InputStream is) throws IOException {
        this(toByteArray(is));
    }

    /**
     * Reads a string index. String indices are offset by 1, and a 0 value in the stream (-1 as returned by this
     * method) means "null"
     * 
     * @return index into file's string ids table, -1 means null
     */
    private static int readStringIndex(ByteBuffer bs) {
        int offsetIndex = readULeb128i(bs);
        return offsetIndex - 1;
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        for (int c = is.read(buff); c > 0; c = is.read(buff)) {
            out.write(buff, 0, c);
        }
        return out.toByteArray();
    }

    private static ByteBuffer slice(ByteBuffer in, int offset, int length) {
        in.position(offset);
        ByteBuffer b = in.slice();
        b.limit(length);
        b.order(ByteOrder.LITTLE_ENDIAN);
        return b;
    }

    private static void skip(ByteBuffer in, int bytes) {
        in.position(in.position() + bytes);
    }

    public static void niceExceptionMessage(Throwable t, int deep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deep + 1; i++) {
            sb.append(".");
        }
        sb.append(' ');
        if (t instanceof DexException) {
            sb.append(t.getMessage());
            System.err.println(sb);
            if (t.getCause() != null) {
                niceExceptionMessage(t.getCause(), deep + 1);
            }
        } else {
            if (t != null) {
                System.err.println(sb.append("ROOT cause:"));
                t.printStackTrace(System.err);
            }
        }
    }

    private static long readIntBits(ByteBuffer in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= ((long) (0xFF & in.get())) << (j * 8);
        }
        int shift = (8 - length) * 8;
        return value << shift >> shift;
    }

    private static long readUIntBits(ByteBuffer in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= ((long) (0xFF & in.get())) << (j * 8);
        }
        return value;
    }

    private static long readFloatBits(ByteBuffer in, int before) {
        int bytes = ((before >> 5) & 0x7) + 1;
        long result = 0L;
        for (int i = 0; i < bytes; ++i) {
            result |= ((long) (0xFF & in.get())) << (i * 8);
        }
        result <<= (8 - bytes) * 8L;
        return result;
    }

    static int sshort(byte[] data, int offset) {
        return (data[offset + 1] << 8) | (0xFF & data[offset]);
    }

    static int ushort(byte[] data, int offset) {
        return ((0xFF & data[offset + 1]) << 8) | (0xFF & data[offset]);
    }

    static int sint(byte[] data, int offset) {
        return (data[offset + 3] << 24) | ((0xFF & data[offset + 2]) << 16) | ((0xFF & data[offset + 1]) << 8)
                | ((0xFF & data[offset]));
    }

    static int uint(byte[] data, int offset) {
        return sint(data, offset);
    }

    static void WARN(String fmt, Object... args) {
        System.err.printf((fmt) + "%n", args);
    }

    static int ubyte(byte[] insns, int offset) {
        return 0xFF & insns[offset];
    }

    static int sbyte(byte[] insns, int offset) {
        return insns[offset];
    }

    private static void order(Map<Integer, DexLabel> labelsMap, int offset) {
        if (!labelsMap.containsKey(offset)) {
            labelsMap.put(offset, new DexLabel(offset));
        }
    }

    public static int readULeb128i(ByteBuffer in) {
        int value = 0;
        int count = 0;
        int b = in.get();
        while ((b & 0x80) != 0) {
            value |= (b & 0x7f) << count;
            count += 7;
            b = in.get();
        }
        value |= (b & 0x7f) << count;
        return value;
    }

    public static int readLeb128i(ByteBuffer in) {
        int bitpos = 0;
        int vln = 0;
        do {
            int inp = in.get();
            vln |= (inp & 0x7F) << bitpos;
            bitpos += 7;
            if ((inp & 0x80) == 0) {
                break;
            }
        } while (true);
        if (((1L << (bitpos - 1)) & vln) != 0) {
            vln -= (1L << bitpos);
        }
        return vln;
    }

    private static void DEBUG_DEBUG(String fmt, Object... args) {
        // System.out.println(String.format(fmt, args));
    }

    private void read_debug_info(int offset, int regSize, boolean isStatic, Method method,
            Map<Integer, DexLabel> labelMap, DexDebugVisitor dcv) {
        ByteBuffer in = debugInfoIn;
        in.position(offset);
        int address = 0;
        int line = readULeb128i(in);
        int szParams = readULeb128i(in);
        LocalEntry[] lastEntryForReg = new LocalEntry[regSize];
        int argsSize = 0;
        for (String paramType : method.getParameterTypes()) {
            if (paramType.equals("J") || paramType.equals("D")) {
                argsSize += 2;
            } else {
                argsSize += 1;
            }
        }
        int curReg = regSize - argsSize;
        if (!isStatic) {
            // Start off with implicit 'this' entry
            LocalEntry thisEntry = new LocalEntry("this", method.getOwner(), null);
            lastEntryForReg[curReg - 1] = thisEntry;
            // dcv.visitParameterName(curReg - 1, "this");
            DEBUG_DEBUG("v%d :%s, %s", curReg - 1, "this", method.getOwner());
        }

        String[] params = method.getParameterTypes();
        for (int i = 0; i < szParams; i++) {
            String paramType = params[i];
            LocalEntry le;

            int nameIdx = readStringIndex(in);
            String name = getString(nameIdx);
            le = new LocalEntry(name, paramType);
            lastEntryForReg[curReg] = le;
            if (name != null) {
                dcv.visitParameterName(i, name);
            }
            DEBUG_DEBUG("v%d :%s, %s", curReg, name, paramType);
            curReg += 1;
            if (paramType.equals("J") || paramType.equals("D")) {
                curReg += 1;
            }
        }

        for (;;) {
            int opcode = in.get() & 0xff;

            switch (opcode) {
            case DBG_START_LOCAL: {
                int reg = readULeb128i(in);
                int nameIdx = readStringIndex(in);
                int typeIdx = readStringIndex(in);
                String name = getString(nameIdx);
                String type = getType(typeIdx);
                DEBUG_DEBUG("Start: v%d :%s, %s", reg, name, type);
                LocalEntry le = new LocalEntry(name, type);
                lastEntryForReg[reg] = le;
                order(labelMap, address);
                dcv.visitStartLocal(reg, labelMap.get(address), name, type, null);
            }
                break;

            case DBG_START_LOCAL_EXTENDED: {
                int reg = readULeb128i(in);
                int nameIdx = readStringIndex(in);
                int typeIdx = readStringIndex(in);
                int sigIdx = readStringIndex(in);
                String name = getString(nameIdx);
                String type = getType(typeIdx);
                String signature = getString(sigIdx);
                DEBUG_DEBUG("Start: v%d :%s, %s // %s", reg, name, type, signature);
                LocalEntry le = new LocalEntry(name, type, signature);
                order(labelMap, address);
                dcv.visitStartLocal(reg, labelMap.get(address), name, type, signature);
                lastEntryForReg[reg] = le;
            }
                break;

            case DBG_RESTART_LOCAL: {
                int reg = readULeb128i(in);
                LocalEntry le = lastEntryForReg[reg];
                if (le == null) {
                    throw new RuntimeException("Encountered RESTART_LOCAL on new v" + reg);
                }
                if (le.signature == null) {
                    DEBUG_DEBUG("Start: v%d :%s, %s", reg, le.name, le.type);
                } else {
                    DEBUG_DEBUG("Start: v%d :%s, %s // %s", reg, le.name, le.type, le.signature);
                }
                order(labelMap, address);
                dcv.visitRestartLocal(reg, labelMap.get(address));
            }
                break;

            case DBG_END_LOCAL: {
                int reg = readULeb128i(in);
                LocalEntry le = lastEntryForReg[reg];
                if (le == null) {
                    throw new RuntimeException("Encountered RESTART_LOCAL on new v" + reg);
                }
                if (le.signature == null) {
                    DEBUG_DEBUG("End: v%d :%s, %s", reg, le.name, le.type);
                } else {
                    DEBUG_DEBUG("End: v%d :%s, %s // %s", reg, le.name, le.type, le.signature);
                }
                order(labelMap, address);
                dcv.visitEndLocal(reg, labelMap.get(address));
            }
                break;

            case DBG_END_SEQUENCE:
                // all done
                return;

            case DBG_ADVANCE_PC:
                address += readULeb128i(in);
                break;

            case DBG_ADVANCE_LINE:
                line += readLeb128i(in);
                break;

            case DBG_SET_PROLOGUE_END:
                order(labelMap, address);
                dcv.visitPrologue(labelMap.get(address));
                break;
            case DBG_SET_EPILOGUE_BEGIN:
                order(labelMap, address);
                dcv.visitEpiogue(labelMap.get(address));
                break;
            case DBG_SET_FILE:
                // skip
                break;

            default:
                if (opcode < DBG_FIRST_SPECIAL) {
                    throw new RuntimeException("Invalid extended opcode encountered " + opcode);
                }

                int adjopcode = opcode - DBG_FIRST_SPECIAL;

                address += adjopcode / DBG_LINE_RANGE;
                line += DBG_LINE_BASE + (adjopcode % DBG_LINE_RANGE);

                order(labelMap, address);
                dcv.visitLineNumber(line, labelMap.get(address));
                break;

            }
        }
    }

    @Override
    public int getDexVersion() {
        return dex_version;
    }

    /**
     * equals to {@link #accept(DexFileVisitor, int)} with 0 as config
     * 
     * @param dv
     */
    @Override
    public void accept(DexFileVisitor dv) {
        this.accept(dv, 0);
    }

    @Override
    public List<String> getClassNames() {
        List<String> names = new ArrayList<>(class_defs_size);
        ByteBuffer in = classDefIn;
        for (int cid = 0; cid < class_defs_size; cid++) {
            in.position(cid * 32);
            String className = this.getType(in.getInt());
            names.add(className);
        }
        return names;
    }

    /**
     * Makes the given visitor visit the dex file.
     * 
     * @param dv
     *            visitor
     * @param config
     *            config flags, {@link #SKIP_CODE}, {@link #SKIP_DEBUG}, {@link #SKIP_ANNOTATION},
     *            {@link #SKIP_FIELD_CONSTANT}
     */
    @Override
    public void accept(DexFileVisitor dv, int config) {
        dv.visitDexFileVersion(this.dex_version);
        for (int cid = 0; cid < class_defs_size; cid++) {
            accept(dv, cid, config);
        }
        dv.visitEnd();
    }

    /**
     * Makes the given visitor visit the dex file. Notice the
     * {@link com.googlecode.d2j.visitors.DexFileVisitor#visitEnd()} is not called
     * 
     * @param dv
     *            visitor
     * @param classIdx
     *            index of class_def
     * @param config
     *            config flags, {@link #SKIP_CODE}, {@link #SKIP_DEBUG}, {@link #SKIP_ANNOTATION},
     *            {@link #SKIP_FIELD_CONSTANT}
     */
    @Override
    public void accept(DexFileVisitor dv, int classIdx, int config) {
        classDefIn.position(classIdx * 32);
        int class_idx = classDefIn.getInt();
        int access_flags = classDefIn.getInt();
        int superclass_idx = classDefIn.getInt();
        int interfaces_off = classDefIn.getInt();
        int source_file_idx = classDefIn.getInt();
        int annotations_off = classDefIn.getInt();
        int class_data_off = classDefIn.getInt();
        int static_values_off = classDefIn.getInt();

        String className = getType(class_idx);
        if(ignoreClass(className)) return;
        String superClassName = getType(superclass_idx);
        String[] interfaceNames = getTypeList(interfaces_off);
        try {
            DexClassVisitor dcv = dv.visit(access_flags, className, superClassName, interfaceNames);
            if (dcv != null)// 不处理
            {
                acceptClass(dcv, source_file_idx, annotations_off, class_data_off, static_values_off, config);
                dcv.visitEnd();
            }
        } catch (Exception ex) {
            DexException dexException = new DexException(ex, "Error process class: [%d]%s", class_idx, className);
            if (0 != (config & IGNORE_READ_EXCEPTION)) {
                niceExceptionMessage(dexException, 0);
            } else {
                throw dexException;
            }
        }
    }

    public Boolean ignoreClass(String className){
       return false;
    }

    private Object readEncodedValue(ByteBuffer in) {
        int b = 0xFF & in.get();
        int type = b & 0x1f;
        switch (type) {
        case VALUE_BYTE:
            return (byte) readIntBits(in, b);

        case VALUE_SHORT:
            return (short) readIntBits(in, b);

        case VALUE_CHAR:
            return (char) readUIntBits(in, b);

        case VALUE_INT:
            return (int) readIntBits(in, b);

        case VALUE_LONG:
            return readIntBits(in, b);

        case VALUE_FLOAT:
            return Float.intBitsToFloat((int) (readFloatBits(in, b) >> 32));

        case VALUE_DOUBLE:
            return Double.longBitsToDouble(readFloatBits(in, b));
        case VALUE_METHOD_TYPE:
            return getProto((int) readUIntBits(in, b));
        case VALUE_METHOD_HANDLE:
            return getMethodHandle((int) readUIntBits(in, b));

        case VALUE_STRING:
            return getString((int) readUIntBits(in, b));

        case VALUE_TYPE: {
            int type_id = (int) readUIntBits(in, b);
            return new DexType(getType(type_id));
        }
        case VALUE_FIELD: {
            int field_id = (int) readUIntBits(in, b);
            return getField(field_id);
        }
        case VALUE_METHOD: {
            int method_id = (int) readUIntBits(in, b);
            return getMethod(method_id);

        }
        case VALUE_ENUM: {
            return getField((int) readUIntBits(in, b));
        }
        case VALUE_ARRAY: {
            return read_encoded_array(in);
        }
        case VALUE_ANNOTATION: {
            return read_encoded_annotation(in);
        }
        case VALUE_NULL:
            return null;
        case VALUE_BOOLEAN: {
            return ((b >> 5) & 0x3) != 0;
        }
        default:
            throw new DexException("Not support yet.");
        }
    }

    private MethodHandle getMethodHandle(int i) {
        methodHandleIdIn.position(i * 8);
        int method_handle_type = methodHandleIdIn.getShort() & 0xFFFF;
        methodHandleIdIn.getShort();//unused
        int field_or_method_id = methodHandleIdIn.getShort() & 0xFFFF;

        switch (method_handle_type) {
        case MethodHandle.INSTANCE_GET:
        case MethodHandle.INSTANCE_PUT:
        case MethodHandle.STATIC_GET:
        case MethodHandle.STATIC_PUT:
            return new MethodHandle(method_handle_type, getField(field_or_method_id));

        case MethodHandle.INVOKE_INSTANCE:
        case MethodHandle.INVOKE_STATIC:
        case MethodHandle.INVOKE_CONSTRUCTOR:
        case MethodHandle.INVOKE_DIRECT:
        case MethodHandle.INVOKE_INTERFACE:
            return new MethodHandle(method_handle_type, getMethod(field_or_method_id));
        default:
            throw new RuntimeException();
        }
    }

    private void acceptClass(DexClassVisitor dcv, int source_file_idx, int annotations_off, int class_data_off,
            int static_values_off, int config) {
        if ((config & SKIP_DEBUG) == 0) {
            // 获取源文件
            if (source_file_idx != -1) {
                dcv.visitSource(this.getString(source_file_idx));
            }
        }

        Map<Integer, Integer> fieldAnnotationPositions;
        Map<Integer, Integer> methodAnnotationPositions;
        Map<Integer, Integer> paramAnnotationPositions;
        if ((config & SKIP_ANNOTATION) == 0) {
            // 获取注解
            fieldAnnotationPositions = new HashMap<>();
            methodAnnotationPositions = new HashMap<>();
            paramAnnotationPositions = new HashMap<>();
            if (annotations_off != 0) { // annotations_directory_item

                annotationsDirectoryItemIn.position(annotations_off);

                int class_annotations_off = annotationsDirectoryItemIn.getInt();
                int field_annotation_size = annotationsDirectoryItemIn.getInt();
                int method_annotation_size = annotationsDirectoryItemIn.getInt();
                int parameter_annotation_size = annotationsDirectoryItemIn.getInt();

                for (int i = 0; i < field_annotation_size; i++) {
                    int field_idx = annotationsDirectoryItemIn.getInt();
                    int field_annotations_offset = annotationsDirectoryItemIn.getInt();
                    fieldAnnotationPositions.put(field_idx, field_annotations_offset);
                }
                for (int i = 0; i < method_annotation_size; i++) {
                    int method_idx = annotationsDirectoryItemIn.getInt();
                    int method_annotation_offset = annotationsDirectoryItemIn.getInt();
                    methodAnnotationPositions.put(method_idx, method_annotation_offset);
                }
                for (int i = 0; i < parameter_annotation_size; i++) {
                    int method_idx = annotationsDirectoryItemIn.getInt();
                    int parameter_annotation_offset = annotationsDirectoryItemIn.getInt();
                    paramAnnotationPositions.put(method_idx, parameter_annotation_offset);
                }

                if (class_annotations_off != 0) {
                    try {
                        read_annotation_set_item(class_annotations_off, dcv);
                    } catch (Exception e) {
                        throw new DexException("error on reading Annotation of class ", e);
                    }
                }
            }
        } else {
            fieldAnnotationPositions = null;
            methodAnnotationPositions = null;
            paramAnnotationPositions = null;
        }

        if (class_data_off != 0) {
            ByteBuffer in = classDataIn;
            in.position(class_data_off);

            int static_fields = readULeb128i(in);
            int instance_fields = readULeb128i(in);
            int direct_methods = readULeb128i(in);
            int virtual_methods = readULeb128i(in);
            {
                int lastIndex = 0;
                {
                    Object[] constant = null;
                    if ((config & SKIP_FIELD_CONSTANT) == 0) {
                        if (static_values_off != 0) {
                            constant = read_encoded_array_item(static_values_off);
                        }
                    }
                    for (int i = 0; i < static_fields; i++) {
                        Object value = null;
                        if (constant != null && i < constant.length) {
                            value = constant[i];
                        }
                        lastIndex = acceptField(in, lastIndex, dcv, fieldAnnotationPositions, value, config);
                    }
                }
                lastIndex = 0;
                for (int i = 0; i < instance_fields; i++) {
                    lastIndex = acceptField(in, lastIndex, dcv, fieldAnnotationPositions, null, config);
                }
                lastIndex = 0;
                boolean firstMethod = true;
                for (int i = 0; i < direct_methods; i++) {
                    lastIndex = acceptMethod(in, lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions,
                            config, firstMethod);
                    firstMethod = false;
                }
                lastIndex = 0;
                firstMethod = true;
                for (int i = 0; i < virtual_methods; i++) {
                    lastIndex = acceptMethod(in, lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions,
                            config, firstMethod);
                    firstMethod = false;
                }
            }

        }
    }

    private Object[] read_encoded_array_item(int static_values_off) {
        encodedArrayItemIn.position(static_values_off);
        return read_encoded_array(encodedArrayItemIn);
    }

    private Object[] read_encoded_array(ByteBuffer in) {
        int size = readULeb128i(in);
        Object[] constant = new Object[size];
        for (int i = 0; i < size; i++) {
            constant[i] = readEncodedValue(in);
        }
        return constant;
    }

    private void read_annotation_set_item(int offset, DexAnnotationAble daa) { // annotation_set_item
        ByteBuffer in = annotationSetItemIn;
        in.position(offset);
        int size = in.getInt();
        for (int j = 0; j < size; j++) {
            int annotation_off = in.getInt();
            read_annotation_item(annotation_off, daa);
        }
    }

    private void read_annotation_item(int annotation_off, DexAnnotationAble daa) {
        ByteBuffer in = annotationItemIn;
        in.position(annotation_off);
        int visibility = 0xFF & in.get();
        DexAnnotationNode annotation = read_encoded_annotation(in);
        annotation.visibility = Visibility.values()[visibility];
        annotation.accept(daa);
    }

    private DexAnnotationNode read_encoded_annotation(ByteBuffer in) {
        int type_idx = readULeb128i(in);
        int size = readULeb128i(in);
        String _typeString = getType(type_idx);
        DexAnnotationNode ann = new DexAnnotationNode(_typeString, Visibility.RUNTIME);
        for (int i = 0; i < size; i++) {
            int name_idx = readULeb128i(in);
            String nameString = getString(name_idx);
            Object value = readEncodedValue(in);
            ann.items.add(new DexAnnotationNode.Item(nameString, value));
        }
        return ann;
    }

    private Field getField(int id) {
        fieldIdIn.position(id * 8);
        int owner_idx = 0xFFFF & fieldIdIn.getShort();
        int type_idx = 0xFFFF & fieldIdIn.getShort();
        int name_idx = fieldIdIn.getInt();
        return new Field(getType(owner_idx), getString(name_idx), getType(type_idx));
    }

    private String[] getTypeList(int offset) {
        if (offset == 0) {
            return new String[0];
        }
        typeListIn.position(offset);
        int size = typeListIn.getInt();
        String[] types = new String[size];
        for (int i = 0; i < size; i++) {
            types[i] = getType(0xFFFF & typeListIn.getShort());
        }
        return types;
    }

    private Proto getProto(int proto_idx) {
        String[] parameterTypes;
        String returnType;

        protoIdIn.position(proto_idx * 12 + 4); // move to position and skip shorty_idx

        int return_type_idx = protoIdIn.getInt();
        int parameters_off = protoIdIn.getInt();

        returnType = getType(return_type_idx);

        parameterTypes = getTypeList(parameters_off);
        return new Proto(parameterTypes, returnType);
    }

    private Method getMethod(int id) {
        methoIdIn.position(id * 8);
        int owner_idx = 0xFFFF & methoIdIn.getShort();
        int proto_idx = 0xFFFF & methoIdIn.getShort();
        int name_idx = methoIdIn.getInt();
        return new Method(getType(owner_idx), getString(name_idx), getProto(proto_idx));
    }

    private String getString(int id) {
        if (id == -1) {
            return null;
        }
        int offset = stringIdIn.getInt(id * 4);
        stringDataIn.position(offset);
        int length = readULeb128i(stringDataIn);
        try {
            StringBuilder buff = new StringBuilder((int) (length * 1.5));
            return Mutf8.decode(stringDataIn, buff);
        } catch (UTFDataFormatException e) {
            throw new DexException(e, "fail to load string %d@%08x", id, offset);
        }
    }

    private String getType(int id) {
        if (id == -1) {
            return null;
        }
        return getString(typeIdIn.getInt(id * 4));
    }

    private static boolean isPowerOfTwo(int i) {
        return (i & (i - 1)) == 0;
    }

    private static int removeHiddenAccess(int accessFlags) {
        // Refer to art/libdexfile/dex/hidden_api_access_flags.h
        if (!isPowerOfTwo(accessFlags & DexConstants.ACC_VISIBILITY_FLAGS)) {
            accessFlags ^= DexConstants.ACC_VISIBILITY_FLAGS;
        }
        accessFlags &= ~((accessFlags & DexConstants.ACC_NATIVE) != 0 ?
                DexConstants.ACC_DEX_HIDDEN_BIT_NATIVE : DexConstants.ACC_DEX_HIDDEN_BIT);
        return accessFlags;
    }

    private int acceptField(ByteBuffer in, int lastIndex, DexClassVisitor dcv,
            Map<Integer, Integer> fieldAnnotationPositions, Object value, int config) {
        int diff = readULeb128i(in);
        int field_access_flags = removeHiddenAccess(readULeb128i(in));
        int field_id = lastIndex + diff;
        Field field = getField(field_id);
        // //////////////////////////////////////////////////////////////
        DexFieldVisitor dfv = dcv.visitField(field_access_flags, field, value);
        if (dfv != null) {
            if ((config & SKIP_ANNOTATION) == 0) {
                Integer annotation_offset = fieldAnnotationPositions.get(field_id);
                if (annotation_offset != null) {
                    try {
                        read_annotation_set_item(annotation_offset, dfv);
                    } catch (Exception e) {
                        throw new DexException(e, "while accept annotation in field:%s.", field.toString());
                    }
                }
            }
            dfv.visitEnd();
        }
        // //////////////////////////////////////////////////////////////
        return field_id;
    }

    private int acceptMethod(ByteBuffer in, int lastIndex, DexClassVisitor cv, Map<Integer, Integer> methodAnnos,
            Map<Integer, Integer> parameterAnnos, int config, boolean firstMethod) {
        int offset = in.position();
        int diff = readULeb128i(in);
        int method_access_flags = removeHiddenAccess(readULeb128i(in));
        int code_off = readULeb128i(in);
        int method_id = lastIndex + diff;
        Method method = getMethod(method_id);

        // issue 200, methods may have same signature, we only need to keep the first one
        if (!firstMethod && diff == 0) { // detect a duplicated method
            WARN("GLITCH: duplicated method %s @%08x", method.toString(), offset);
            if ((config & KEEP_ALL_METHODS) == 0) {
                WARN("WARN: skip method %s @%08x", method.toString(), offset);
                return method_id;
            }
        }

        // issue 195, a <clinit> or <init> but not marked as ACC_CONSTRUCTOR,
        if (0 == (method_access_flags & DexConstants.ACC_CONSTRUCTOR)
                && (method.getName().equals("<init>") || method.getName().equals("<clinit>"))) {
            WARN("GLITCH: method %s @%08x not marked as ACC_CONSTRUCTOR", method.toString(), offset);
        }

        try {
            DexMethodVisitor dmv = cv.visitMethod(method_access_flags, method);
            if (dmv != null) {
                if ((config & SKIP_ANNOTATION) == 0) {
                    Integer annotation_offset = methodAnnos.get(method_id);
                    if (annotation_offset != null) {
                        try {
                            read_annotation_set_item(annotation_offset, dmv);
                        } catch (Exception e) {
                            throw new DexException(e, "while accept annotation in method:%s.", method.toString());
                        }
                    }
                    Integer parameter_annotation_offset = parameterAnnos.get(method_id);
                    if (parameter_annotation_offset != null) {
                        try {
                            read_annotation_set_ref_list(parameter_annotation_offset, dmv);
                        } catch (Exception e) {
                            throw new DexException(e, "while accept parameter annotation in method:%s.",
                                    method.toString());
                        }
                    }
                }
                if (code_off != 0) {
                    boolean keep = true;
                    if (0 != (SKIP_CODE & config)) {
                        keep = 0 != (KEEP_CLINIT & config) && method.getName().equals("<clinit>");
                    }
                    if(keep) {
                        DexCodeVisitor dcv = dmv.visitCode();
                        if (dcv != null) {
                            try {
                                acceptCode(code_off, dcv, config, (method_access_flags & DexConstants.ACC_STATIC) != 0,
                                        method);
                            } catch (Exception e) {
                                throw new DexException(e, "while accept code in method:[%s] @%08x", method.toString(),
                                        code_off);
                            }
                        }
                    }
                }
                dmv.visitEnd();
            }
        } catch (Exception e) {
            throw new DexException(e, "while accept method:[%s]", method.toString());
        }

        return method_id;
    }

    private void read_annotation_set_ref_list(int parameter_annotation_offset, DexMethodVisitor dmv) {
        ByteBuffer in = annotationSetRefListIn;
        in.position(parameter_annotation_offset);

        int size = in.getInt();
        for (int j = 0; j < size; j++) {
            int param_annotation_offset = in.getInt();
            if (param_annotation_offset == 0) {
                continue;
            }
            DexAnnotationAble dpav = dmv.visitParameterAnnotation(j);
            try {
                if (dpav != null) {
                    read_annotation_set_item(param_annotation_offset, dpav);
                }
            } catch (Exception e) {
                throw new DexException(e, "While accepting parameter annotation in parameter: [%d]", j);
            }
        }
    }

    /**
     * the size of class in dex file
     * 
     * @return class_defs_size
     */
    public final int getClassSize() {
        return class_defs_size;
    }

    static class BadOpException extends RuntimeException {

        private static final long serialVersionUID = 5354839427958139635L;

        public BadOpException(String fmt, Object... args) {
            super(String.format(fmt, args));
        }
    }

    private void findLabels(byte[] insns, BitSet nextBit, BitSet badOps, Map<Integer, DexLabel> labelsMap, Set<Integer> handlers,
            Method method) {
        Queue<Integer> q = new LinkedList<>();
        q.add(0);
        q.addAll(handlers);
        handlers.clear();
        while (!q.isEmpty()) {
            int offset = q.poll();
            if (nextBit.get(offset)) {
                continue;
            } else {
                nextBit.set(offset);
            }
            try {
                travelInsn(labelsMap, q, insns, offset);
            } catch (IndexOutOfBoundsException indexOutOfRange){
                badOps.set(offset);
                WARN("GLITCH: %04x %s | not enough space for reading instruction", offset, method.toString());
            } catch (BadOpException badOp){
                badOps.set(offset);
                WARN("GLITCH: %04x %s | %s", offset, method.toString(), badOp.getMessage());
            }
        }
    }

    private void travelInsn(Map<Integer, DexLabel> labelsMap, Queue<Integer> q, byte[] insns, int offset) {
        int u1offset = offset * 2;
        if (u1offset >= insns.length) {
            throw new IndexOutOfBoundsException();
        }
        int opcode = 0xFF & insns[u1offset];
        Op op = Op.ops[opcode];
        if (op == null || op.format == null) {
            throw new BadOpException("zero-width instruction op=0x%02x", opcode);
        }
        int target;
        boolean canContinue = true;
        if (op.canBranch()) {
            switch (op.format) {
            case kFmt10t:
                target = offset + insns[u1offset + 1];
                if (target < 0 || target * 2 > insns.length ) {
                    throw new BadOpException("jump out of insns %s -> %04x", op, target);
                }
                q.add(target);
                order(labelsMap, target);
                break;
            case kFmt20t:
            case kFmt21t:
                target = offset + sshort(insns, u1offset + 2);
                if (target < 0 || target * 2 > insns.length ) {
                    throw new BadOpException("jump out of insns %s -> %04x", op, target);
                }
                q.add(target);
                order(labelsMap, target);
                break;
            case kFmt22t:
                target = offset + sshort(insns, u1offset + 2);

                int u = ubyte(insns, u1offset + 1);
                boolean cmpSameReg = (u & 0x0F) == ((u >> 4) & 0x0F);
                boolean skipTarget = false;
                if (cmpSameReg) {
                    switch (op) {
                    case IF_EQ:
                    case IF_GE:
                    case IF_LE:
                        // means always jump, equals to goto
                        canContinue = false;
                        break;
                    case IF_NE:
                    case IF_GT:
                    case IF_LT:
                        // means always not jump
                        skipTarget = true;
                        break;
                    default:
                        break;
                    }
                }
                if (!skipTarget) {
                    if (target < 0 || target * 2 > insns.length ) {
                        throw new BadOpException("jump out of insns %s -> %04x", op, target);
                    }
                    q.add(target);
                    order(labelsMap, target);
                }
                break;
            case kFmt30t:
            case kFmt31t:
                target = offset + sint(insns, u1offset + 2);
                if (target < 0 || target * 2 > insns.length ) {
                    throw new BadOpException("jump out of insns %s -> %04x", op, target);
                }
                q.add(target);
                order(labelsMap, target);
                break;
            default:
                break;
            }
        }
        if (op.canSwitch()) {
            order(labelsMap, offset + op.format.size);// default
            int u1SwitchData = 2 * (offset + sint(insns, u1offset + 2));
            if (u1SwitchData + 2 < insns.length) {

                    switch (insns[u1SwitchData + 1]) {
                        case 0x01: // packed-switch-data
                        {
                            int size = ushort(insns, u1SwitchData + 2);
                            int b = u1SwitchData + 8;// targets
                            for (int i = 0; i < size; i++) {
                                target = offset + sint(insns, b + i * 4);
                                if (target < 0 || target * 2 > insns.length ) {
                                    throw new BadOpException("jump out of insns %s -> %04x", op, target);
                                }
                                q.add(target);
                                order(labelsMap, target);
                            }
                            break;
                        }
                        case 0x02:// sparse-switch-data
                        {
                            int size = ushort(insns, u1SwitchData + 2);
                            int b = u1SwitchData + 4 + 4 * size;// targets
                            for (int i = 0; i < size; i++) {
                                target = offset + sint(insns, b + i * 4);
                                if (target < 0 || target * 2 > insns.length ) {
                                    throw new BadOpException("jump out of insns %s -> %04x", op, target);
                                }
                                q.add(target);
                                order(labelsMap, target);
                            }
                            break;
                        }
                        default:
                            throw new BadOpException("bad payload for %s", op);
                    }
            } else {
                throw new BadOpException("bad payload offset for %s", op);
            }
        }

        if (canContinue) {
            int idx = Integer.MAX_VALUE;
            switch (op.indexType) {
            case kIndexStringRef:
                if (op.format == InstructionFormat.kFmt31c) {
                    idx = uint(insns, u1offset + 2);
                } else {// other
                    idx = ushort(insns, u1offset + 2);
                }
                canContinue = idx >= 0 && idx < string_ids_size;
                break;
            case kIndexTypeRef:
                idx = ushort(insns, u1offset + 2);
                canContinue = idx < type_ids_size;
                break;
            case kIndexMethodRef:
                idx = ushort(insns, u1offset + 2);
                canContinue = idx < method_ids_size;
                break;
            case kIndexFieldRef:
                idx = ushort(insns, u1offset + 2);
                canContinue = idx < field_ids_size;
                break;
            case kIndexCallSiteRef:
                idx = ushort(insns, u1offset + 2);
                canContinue = idx < call_site_ids_size;
                break;
            case kIndexMethodAndProtoRef:
                idx = ushort(insns, u1offset + 2);
                int idx2 = ushort(insns, u1offset + 6);
                canContinue = idx < method_ids_size && idx2 < proto_ids_size;
                break;
            default:
            }
            if (!canContinue) {
                throw new BadOpException("index-out-of-range for %s index: %d", op, idx);
            }
        }

        if (canContinue && op.canContinue()) {
            if (op == Op.NOP) {
                switch (insns[u1offset + 1]) {
                case 0x00:
                    q.add(offset + Op.NOP.format.size);
                    break;
                case 0x01: {
                    int size = ushort(insns, u1offset + 2);
                    q.add(offset + (size * 2) + 4);
                    break;
                }
                case 0x02: {
                    int size = ushort(insns, u1offset + 2);
                    q.add(offset + (size * 4) + 2);
                    break;
                }
                case 0x03: {
                    int element_width = ushort(insns, u1offset + 2);
                    int size = uint(insns, u1offset + 4);
                    q.add(offset + (size * element_width + 1) / 2 + 4);
                    break;
                }
                }
            } else {
                q.add(offset + op.format.size);
            }
        }
    }

    private void findTryCatch(ByteBuffer in, DexCodeVisitor dcv, int tries_size, int insn_size,
            Map<Integer, DexLabel> labelsMap, Set<Integer> handlers) {
        int encoded_catch_handler_list = in.position() + tries_size * 8;
        ByteBuffer handlerIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < tries_size; i++) { // try_item
            int start_addr = in.getInt();
            int insn_count = 0xFFFF & in.getShort();
            int handler_offset = 0xFFFF & in.getShort();
            if (start_addr > insn_size) {
                continue;
            }
            order(labelsMap, start_addr);
            int end = start_addr + insn_count;
            order(labelsMap, end);

            handlerIn.position(encoded_catch_handler_list + handler_offset);// move to encoded_catch_handler

            boolean catchAll = false;
            int listSize = readLeb128i(handlerIn);
            int handlerCount = listSize;
            if (listSize <= 0) {
                listSize = -listSize;
                handlerCount = listSize + 1;
                catchAll = true;
            }
            DexLabel[] labels = new DexLabel[handlerCount];
            String[] types = new String[handlerCount];
            for (int k = 0; k < listSize; k++) {
                int type_id = readULeb128i(handlerIn);
                int handler = readULeb128i(handlerIn);
                order(labelsMap, handler);
                handlers.add(handler);
                types[k] = getType(type_id);
                labels[k] = labelsMap.get(handler);
            }
            if (catchAll) {
                int handler = readULeb128i(handlerIn);
                order(labelsMap, handler);
                handlers.add(handler);
                labels[listSize] = labelsMap.get(handler);
            }
            dcv.visitTryCatch(labelsMap.get(start_addr), labelsMap.get(end), labels, types);
        }
    }

    /* package */void acceptCode(int code_off, DexCodeVisitor dcv, int config, boolean isStatic, Method method) {
        ByteBuffer in = codeItemIn;
        in.position(code_off);
        int registers_size = 0xFFFF & in.getShort();
        in.getShort();// ins_size ushort
        in.getShort();// outs_size ushort
        int tries_size = 0xFFFF & in.getShort();
        int debug_info_off = in.getInt();
        int insns = in.getInt();

        byte[] insnsArray = new byte[insns * 2];
        in.get(insnsArray);
        dcv.visitRegister(registers_size);
        BitSet nextInsn = new BitSet();
        Map<Integer, DexLabel> labelsMap = new TreeMap<>();
        Set<Integer> handlers = new HashSet<>();
        // 处理异常处理
        if (tries_size > 0) {
            if ((insns & 0x01) != 0) {// skip padding
                in.getShort();
            }
            if (0 == (config & SKIP_EXCEPTION)) {
                findTryCatch(in, dcv, tries_size, insns, labelsMap, handlers);
            }
        }
        // 处理debug信息
        if (debug_info_off != 0 && (0 == (config & SKIP_DEBUG))) {
            DexDebugVisitor ddv = dcv.visitDebug();
            if (ddv != null) {
                read_debug_info(debug_info_off, registers_size, isStatic, method, labelsMap, ddv);
                ddv.visitEnd();
            }
        }

        BitSet badOps = new BitSet();
        findLabels(insnsArray, nextInsn, badOps, labelsMap, handlers, method);
        acceptInsn(insnsArray, dcv, nextInsn, badOps, labelsMap);
        dcv.visitEnd();
    }

    // 处理指令
    private void acceptInsn(byte[] insns, DexCodeVisitor dcv, BitSet nextInsn, BitSet badOps, Map<Integer, DexLabel> labelsMap) {
        Iterator<Integer> labelOffsetIterator = labelsMap.keySet().iterator();
        Integer nextLabelOffset = labelOffsetIterator.hasNext() ? labelOffsetIterator.next() : null;
        Op[] values = Op.ops;
        for (int offset = nextInsn.nextSetBit(0); offset >= 0; offset = nextInsn.nextSetBit(offset + 1)) {
            // issue 65, a label may `inside` an instruction
            // visit all label with offset <= currentOffset
            while (nextLabelOffset != null) {
                if (nextLabelOffset <= offset) {
                    dcv.visitLabel(labelsMap.get(nextLabelOffset));
                    nextLabelOffset = labelOffsetIterator.hasNext() ? labelOffsetIterator.next() : null;
                } else {
                    // the label is after this instruction
                    break;
                }
            }

            if(badOps.get(offset)){
                dcv.visitStmt0R(Op.BAD_OP);
                continue;
            }

            int u1offset = offset * 2;
            int opcode = 0xFF & insns[u1offset];

            Op op = values[opcode];

            int a, b, c, target;
            switch (op.format) {
            // case kFmt00x: break;
            case kFmt10x:
                dcv.visitStmt0R(op);
                break;

            case kFmt11x:
                dcv.visitStmt1R(op, 0xFF & insns[u1offset + 1]);
                break;
            case kFmt12x:
                a = ubyte(insns, u1offset + 1);
                dcv.visitStmt2R(op, a & 0xF, a >> 4);
                break;
            // case kFmt20bc:break;
            case kFmt10t:
                target = offset + insns[u1offset + 1];
                dcv.visitJumpStmt(op, -1, -1, labelsMap.get(target));
                break;
            case kFmt20t:
                target = offset + sshort(insns, u1offset + 2);
                dcv.visitJumpStmt(op, -1, -1, labelsMap.get(target));
                break;
            case kFmt21t:
                target = offset + sshort(insns, u1offset + 2);
                dcv.visitJumpStmt(op, ubyte(insns, u1offset + 1), -1, labelsMap.get(target));
                break;
            case kFmt22t:
                target = offset + sshort(insns, u1offset + 2);
                a = ubyte(insns, u1offset + 1);
                b = a & 0x0F;
                c = a >> 4;
                boolean ignore = false;
                if (b == c) {
                    switch (op) {
                    case IF_EQ:
                    case IF_GE:
                    case IF_LE:
                        // means always jump, equals to goto
                        dcv.visitJumpStmt(Op.GOTO, 0, 0, labelsMap.get(target));
                        ignore = true;
                        break;
                    case IF_NE:
                    case IF_GT:
                    case IF_LT:
                        // means always not jump
                        ignore = true;
                        break;
                    default:
                        break;
                    }
                }
                if (!ignore) {
                    dcv.visitJumpStmt(op, b, c, labelsMap.get(target));
                }
                break;
            case kFmt30t:
                target = offset + sint(insns, u1offset + 2);
                dcv.visitJumpStmt(op, -1, -1, labelsMap.get(target));
                break;
            case kFmt31t:
                target = offset + sint(insns, u1offset + 2);
                a = ubyte(insns, u1offset + 1);
                int u1SwitchData = 2 * target;
                if (op == Op.FILL_ARRAY_DATA) {
                    int element_width = ushort(insns, u1SwitchData + 2);
                    int size = uint(insns, u1SwitchData + 4);
                    switch (element_width) {
                    case 1: {
                        byte[] data = new byte[size];
                        System.arraycopy(insns, u1SwitchData + 8, data, 0, size);
                        dcv.visitFillArrayDataStmt(Op.FILL_ARRAY_DATA, a, data);
                    }
                        break;
                    case 2: {
                        short[] data = new short[size];
                        for (int i = 0; i < size; i++) {
                            data[i] = (short) sshort(insns, u1SwitchData + 8 + 2 * i);
                        }
                        dcv.visitFillArrayDataStmt(Op.FILL_ARRAY_DATA, a, data);
                    }
                        break;
                    case 4: {
                        int[] data = new int[size];
                        for (int i = 0; i < size; i++) {
                            data[i] = sint(insns, u1SwitchData + 8 + 4 * i);
                        }
                        dcv.visitFillArrayDataStmt(Op.FILL_ARRAY_DATA, a, data);
                    }
                        break;
                    case 8: {
                        long[] data = new long[size];
                        for (int i = 0; i < size; i++) {
                            int t = u1SwitchData + 8 + 8 * i;
                            long z = 0;
                            z |= (ushort(insns, t));
                            z |= ((long) ushort(insns, t + 2)) << 16;
                            z |= ((long) ushort(insns, t + 4)) << 32;
                            z |= ((long) ushort(insns, t + 6)) << 48;
                            data[i] = z;
                        }
                        dcv.visitFillArrayDataStmt(Op.FILL_ARRAY_DATA, a, data);
                    }
                        break;
                    }
                } else if (op == Op.SPARSE_SWITCH) {
                    int size = sshort(insns, u1SwitchData + 2);
                    int[] keys = new int[size];
                    DexLabel[] labels = new DexLabel[size];
                    int z = u1SwitchData + 4;
                    for (int i = 0; i < size; i++) {
                        keys[i] = sint(insns, z + i * 4);
                    }
                    z += size * 4;
                    for (int i = 0; i < size; i++) {
                        labels[i] = labelsMap.get(offset + sint(insns, z + i * 4));
                    }
                    dcv.visitSparseSwitchStmt(Op.SPARSE_SWITCH, a, keys, labels);
                } else {
                    int size = sshort(insns, u1SwitchData + 2);
                    int first_key = sint(insns, u1SwitchData + 4);
                    DexLabel[] labels = new DexLabel[size];
                    int z = u1SwitchData + 8;
                    for (int i = 0; i < size; i++) {
                        labels[i] = labelsMap.get(offset + sint(insns, z));
                        z += 4;
                    }
                    dcv.visitPackedSwitchStmt(op, a, first_key, labels);
                }
                break;
            case kFmt21c:
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                switch (op.indexType) {
                case kIndexStringRef:
                    dcv.visitConstStmt(op, a, getString(b));
                    break;
                case kIndexFieldRef:
                    dcv.visitFieldStmt(op, a, -1, getField(b));
                    break;
                case kIndexTypeRef:
                    if (op == Op.CONST_CLASS) {
                        dcv.visitConstStmt(Op.CONST_CLASS, a, new DexType(getType(b)));
                    } else {
                        dcv.visitTypeStmt(op, a, -1, getType(b));
                    }
                    break;
                case kIndexMethodHandleRef:
                    dcv.visitConstStmt(op, a, getMethodHandle(b));
                    break;
                case kIndexProtoRef:
                    dcv.visitConstStmt(op, a, getProto(b));
                    break;
                default:
                    break;
                }
                break;
            case kFmt22c:
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                switch (op.indexType) {
                case kIndexFieldRef:
                    dcv.visitFieldStmt(op, a & 0xF, a >> 4, getField(b));
                    break;
                case kIndexTypeRef:
                    dcv.visitTypeStmt(op, a & 0xF, a >> 4, getType(b));
                    break;
                default:
                    break;
                }
                break;
            case kFmt31c:
                if (op.indexType == InstructionIndexType.kIndexStringRef) {
                    a = ubyte(insns, u1offset + 1);
                    b = uint(insns, u1offset + 2);
                    dcv.visitConstStmt(op, a, getString(b));
                }
                break;
            case kFmt35c: {
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                int dc = ubyte(insns, u1offset + 4); // DC
                int fe = ubyte(insns, u1offset + 5); // FE

                int[] regs = new int[a >> 4];
                switch (a >> 4) {
                case 5:
                    regs[4] = a & 0xF;// G
                case 4:
                    regs[3] = 0xF & (fe >> 4);// F
                case 3:
                    regs[2] = 0xF & (fe);// E
                case 2:
                    regs[1] = 0xF & (dc >> 4);// D
                case 1:
                    regs[0] = 0xF & (dc);// C
                }
                if (op.indexType == InstructionIndexType.kIndexTypeRef) {
                    dcv.visitFilledNewArrayStmt(op, regs, getType(b));
                } else if (op.indexType == InstructionIndexType.kIndexCallSiteRef) {
                    dcv.visitMethodStmt(op, regs, getCallSite(b));
                } else {
                    dcv.visitMethodStmt(op, regs, getMethod(b));
                }
            }
                break;
            case kFmt3rc: {
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                c = ushort(insns, u1offset + 4);
                int[] regs = new int[a];
                for (int i = 0; i < a; i++) {
                    regs[i] = c + i;
                }
                if (op.indexType == InstructionIndexType.kIndexTypeRef) {
                    dcv.visitFilledNewArrayStmt(op, regs, getType(b));
                } else if (op.indexType == InstructionIndexType.kIndexCallSiteRef) {
                    dcv.visitMethodStmt(op, regs, getCallSite(b));
                } else {
                    dcv.visitMethodStmt(op, regs, getMethod(b));
                }
            }
                break;
            case kFmt45cc: {
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                int dc = ubyte(insns, u1offset + 4); // DC
                int fe = ubyte(insns, u1offset + 5); // FE
                int h = ushort(insns, u1offset + 6);

                int[] regs = new int[a >> 4];
                switch (a >> 4) {
                case 5:
                    regs[4] = a & 0xF;// G
                case 4:
                    regs[3] = 0xF & (fe >> 4);// F
                case 3:
                    regs[2] = 0xF & (fe);// E
                case 2:
                    regs[1] = 0xF & (dc >> 4);// D
                case 1:
                    regs[0] = 0xF & (dc);// C
                }
                dcv.visitMethodStmt(op, regs, getMethod(b), getProto(h));
            }
            break;
            case kFmt4rcc: {
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                c = ushort(insns, u1offset + 4);
                int h = ushort(insns, u1offset + 6);
                int[] regs = new int[a];
                for (int i = 0; i < a; i++) {
                    regs[i] = c + i;
                }
                dcv.visitMethodStmt(op, regs, getMethod(b), getProto(h));
            }
            break;
            case kFmt22x:
                a = ubyte(insns, u1offset + 1);
                b = ushort(insns, u1offset + 2);
                dcv.visitStmt2R(op, a, b);
                break;
            case kFmt23x:
                a = ubyte(insns, u1offset + 1);
                b = ubyte(insns, u1offset + 2);
                c = ubyte(insns, u1offset + 3);
                dcv.visitStmt3R(op, a, b, c);
                break;
            case kFmt32x:
                a = ushort(insns, u1offset + 2);
                b = ushort(insns, u1offset + 4);
                dcv.visitStmt2R(op, a, b);
                break;
            case kFmt11n:
                a = insns[u1offset + 1];
                dcv.visitConstStmt(op, a & 0xF, a >> 4);
                break;
            case kFmt21h:
                a = ubyte(insns, u1offset + 1);
                b = sshort(insns, u1offset + 2);
                if (op == Op.CONST_HIGH16) {
                    dcv.visitConstStmt(Op.CONST_HIGH16, a, b << 16);
                } else {
                    dcv.visitConstStmt(op, a, ((long) b) << 48);
                }
                break;
            case kFmt21s:
                a = ubyte(insns, u1offset + 1);
                b = sshort(insns, u1offset + 2);
                if (op == Op.CONST_16) {
                    dcv.visitConstStmt(Op.CONST_16, a, b);
                } else {
                    dcv.visitConstStmt(op, a, (long) b);
                }
                break;
            case kFmt22b:
                a = ubyte(insns, u1offset + 1);
                b = ubyte(insns, u1offset + 2);
                c = sbyte(insns, u1offset + 3);
                dcv.visitStmt2R1N(op, a, b, c);
                break;
            case kFmt22s:
                a = ubyte(insns, u1offset + 1);
                b = sshort(insns, u1offset + 2);
                dcv.visitStmt2R1N(op, a & 0xF, a >> 4, b);
                break;
            // case kFmt22cs:break;
            case kFmt31i:
                a = ubyte(insns, u1offset + 1);
                b = sint(insns, u1offset + 2);
                if (op == Op.CONST) {
                    dcv.visitConstStmt(Op.CONST, a, b);
                } else {
                    dcv.visitConstStmt(op, a, (long) b);
                }
                break;
            case kFmt51l:
                a = ubyte(insns, u1offset + 1);
                long z = 0;
                z |= ushort(insns, u1offset + 2);
                z |= ((long) ushort(insns, u1offset + 4)) << 16;
                z |= ((long) ushort(insns, u1offset + 6)) << 32;
                z |= ((long) ushort(insns, u1offset + 8)) << 48;
                dcv.visitConstStmt(op, a, z);
                break;
            }
        }

        while (nextLabelOffset != null) {
            dcv.visitLabel(labelsMap.get(nextLabelOffset));
            if (labelOffsetIterator.hasNext()) {
                nextLabelOffset = labelOffsetIterator.next();
            } else {
                break;
            }
        }
    }

    private CallSite getCallSite(int b) {
        callSiteIdIn.position(b * 4);
        int call_site_off = callSiteIdIn.getInt();

        Object[] call_site_items = read_encoded_array_item(call_site_off);
        Object[] constArgs;
        if (call_site_items.length > 3) {
            constArgs = Arrays.copyOfRange(call_site_items, 3, call_site_items.length);
        } else {
            constArgs = new Object[0];
        }

        return new CallSite(
                String.format("call_site_%d", b),
                (MethodHandle) call_site_items[0],
                (String) call_site_items[1],
                (Proto) call_site_items[2],
                constArgs);
    }

    /**
     * An entry in the resulting locals table
     */
    static private class LocalEntry {
        public String name, type, signature;

        private LocalEntry(String name, String type) {
            this.name = name;
            this.type = type;
        }

        private LocalEntry(String name, String type, String signature) {
            this.name = name;
            this.type = type;
            this.signature = signature;
        }
    }
}
