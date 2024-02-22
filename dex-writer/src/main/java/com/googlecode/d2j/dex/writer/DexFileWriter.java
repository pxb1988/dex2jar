package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.io.ByteBufferOut;
import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetRefListItem;
import com.googlecode.d2j.dex.writer.item.AnnotationsDirectoryItem;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.CallSiteIdItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ClassDefItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.dex.writer.item.DebugInfoItem;
import com.googlecode.d2j.dex.writer.item.FieldIdItem;
import com.googlecode.d2j.dex.writer.item.HeadItem;
import com.googlecode.d2j.dex.writer.item.MapListItem;
import com.googlecode.d2j.dex.writer.item.MethodHandleItem;
import com.googlecode.d2j.dex.writer.item.MethodIdItem;
import com.googlecode.d2j.dex.writer.item.ProtoIdItem;
import com.googlecode.d2j.dex.writer.item.SectionItem;
import com.googlecode.d2j.dex.writer.item.SectionItem.SectionType;
import com.googlecode.d2j.dex.writer.item.StringDataItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.dex.writer.item.TypeIdItem;
import com.googlecode.d2j.dex.writer.item.TypeListItem;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;

public class DexFileWriter extends DexFileVisitor {

    private static final boolean DEBUG = false;

    MapListItem mapItem;

    HeadItem headItem;

    public ConstPool cp = new ConstPool();

    private static DataOut wrapDumpOut(final DataOut out0) {
        return (DataOut) Proxy.newProxyInstance(
                DexFileWriter.class.getClassLoader(),
                new Class[]{DataOut.class}, new InvocationHandler() {
                    int indent = 0;

                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {

                        if (method.getParameterTypes().length > 0
                                && method.getParameterTypes()[0]
                                .equals(String.class)) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < indent; i++) {
                                sb.append("  ");
                            }
                            sb.append(String.format("%05d ", out0.offset()));
                            sb.append(method.getName()).append(" [");
                            for (Object arg : args) {
                                if (arg instanceof byte[]) {
                                    byte[] data = (byte[]) arg;
                                    sb.append("0x[");
                                    int start = 0;
                                    int size = data.length;
                                    if (args.length > 2) {
                                        start = (Integer) args[2];
                                        size = (Integer) args[3];
                                    }
                                    for (int i = 0; i < size; i++) {
                                        sb.append(String.format("%02x",
                                                data[start + i] & 0xff));
                                        if (i != size - 1) {
                                            sb.append(", ");
                                        }
                                    }

                                    sb.append("], ");
                                } else {
                                    sb.append(arg).append(", ");
                                }

                            }
                            sb.append("]");
                            System.out.println(sb);
                        }
                        if (method.getName().equals("begin")) {
                            indent++;
                        }
                        if (method.getName().equals("end")) {
                            indent--;
                        }
                        return method.invoke(out0, args);
                    }
                });

    }

    void buildMapListItem() {

        // begin ===========
        // satisfy 'bool DexFileVerifier::CheckMap()' on art/runtime/dex_file_verifier.cc
        // make sure the items are not empty
        if (cp.classDefs.isEmpty()) {
            System.err.println("WARN: no classdef on the dex");
        }
        if (cp.methods.isEmpty()) {
            cp.uniqMethod("Ljava/lang/Object;", "<init>", new String[0], "V");
        }
        if (cp.fields.isEmpty()) {
            cp.uniqField("Ljava/lang/System;", "out", "Ljava/io/PrintStream;");
        }
        if (cp.protos.isEmpty()) {
            cp.uniqProto(new String[0], "V");
        }
        if (cp.types.isEmpty()) {
            cp.uniqType("V");
        }
        if (cp.strings.isEmpty()) {
            cp.uniqString("V");
        }
        // end ===========

        mapItem = new MapListItem();
        headItem = new HeadItem();
        headItem.version = cp.dexVersion;
        SectionItem<HeadItem> headSection = new SectionItem<>(SectionType.TYPE_HEADER_ITEM);
        headSection.items.add(headItem);
        SectionItem<MapListItem> mapSection = new SectionItem<>(SectionType.TYPE_MAP_LIST);
        mapSection.items.add(mapItem);
        SectionItem<StringIdItem> stringIdSection = new SectionItem<>(
                SectionType.TYPE_STRING_ID_ITEM, cp.strings.values());
        SectionItem<TypeIdItem> typeIdSection = new SectionItem<>(
                SectionType.TYPE_TYPE_ID_ITEM, cp.types.values());
        SectionItem<ProtoIdItem> protoIdSection = new SectionItem<>(
                SectionType.TYPE_PROTO_ID_ITEM, cp.protos.values());
        SectionItem<FieldIdItem> fieldIdSection = new SectionItem<>(
                SectionType.TYPE_FIELD_ID_ITEM, cp.fields.values());
        SectionItem<MethodIdItem> methodIdSection = new SectionItem<>(
                SectionType.TYPE_METHOD_ID_ITEM, cp.methods.values());
        SectionItem<MethodHandleItem> methodHandlerSection = new SectionItem<>(
                SectionType.TYPE_METHOD_HANDLE_ITEM, cp.methodHandlers.values());
        SectionItem<ClassDefItem> classDefSection = new SectionItem<>(
                SectionType.TYPE_CLASS_DEF_ITEM, cp.buildSortedClassDefItems());
        SectionItem<TypeListItem> typeListSection = new SectionItem<>(
                SectionType.TYPE_TYPE_LIST, cp.typeLists.values());
        SectionItem<AnnotationSetRefListItem> annotationSetRefListItemSection = new SectionItem<>(
                SectionType.TYPE_ANNOTATION_SET_REF_LIST,
                cp.annotationSetRefListItems.values());
        SectionItem<AnnotationSetItem> annotationSetSection = new SectionItem<>(
                SectionType.TYPE_ANNOTATION_SET_ITEM,
                cp.annotationSetItems.values());
        SectionItem<ClassDataItem> classDataItemSection = new SectionItem<>(
                SectionType.TYPE_CLASS_DATA_ITEM, cp.classDataItems);
        SectionItem<CodeItem> codeItemSection = new SectionItem<>(
                SectionType.TYPE_CODE_ITEM, cp.codeItems);
        SectionItem<StringDataItem> stringDataItemSection = new SectionItem<>(
                SectionType.TYPE_STRING_DATA_ITEM, cp.stringDatas);
        SectionItem<DebugInfoItem> debugInfoSection = new SectionItem<>(
                SectionType.TYPE_DEBUG_INFO_ITEM, cp.debugInfoItems);
        SectionItem<AnnotationItem> annotationItemSection = new SectionItem<>(
                SectionType.TYPE_ANNOTATION_ITEM, cp.annotationItems.values());
        SectionItem<EncodedArray> encodedArrayItemSection = new SectionItem<>(
                SectionType.TYPE_ENCODED_ARRAY_ITEM, cp.encodedArrayItems.values());
        SectionItem<CallSiteIdItem> callSiteIdItemSectionItem = new SectionItem<>(
                SectionType.TYPE_CALL_SITE_ID_ITEM, cp.callSiteIdItems.values());
        SectionItem<AnnotationsDirectoryItem> annotationsDirectoryItemSection = new SectionItem<>(
                SectionType.TYPE_ANNOTATIONS_DIRECTORY_ITEM,
                cp.annotationsDirectoryItems);

        {
            headItem.mapSection = mapSection;
            headItem.stringIdSection = stringIdSection;
            headItem.typeIdSection = typeIdSection;
            headItem.protoIdSection = protoIdSection;
            headItem.fieldIdSection = fieldIdSection;
            headItem.methodIdSection = methodIdSection;
            headItem.classDefSection = classDefSection;
        }

        List<SectionItem<?>> dataSectionItems = new ArrayList<>();
        {
            dataSectionItems.add(mapSection); // data section
            dataSectionItems.add(typeListSection); // data section
            dataSectionItems.add(annotationSetRefListItemSection); // data
            // section
            dataSectionItems.add(annotationSetSection); // data section
            // make codeItem Before classDataItem
            dataSectionItems.add(codeItemSection); // data section
            dataSectionItems.add(classDataItemSection); // data section
            dataSectionItems.add(stringDataItemSection); // data section
            dataSectionItems.add(debugInfoSection); // data section
            dataSectionItems.add(annotationItemSection); // data section
            dataSectionItems.add(encodedArrayItemSection); // data section
            dataSectionItems.add(annotationsDirectoryItemSection); // data section
        }

        List<SectionItem<?>> items = mapItem.items;
        {
            items.add(headSection);
            items.add(stringIdSection);
            items.add(typeIdSection);
            items.add(protoIdSection);
            items.add(fieldIdSection);
            items.add(methodIdSection);
            items.add(classDefSection);
            if (callSiteIdItemSectionItem.items.size() > 0) {
                items.add(callSiteIdItemSectionItem);
            }
            if (methodHandlerSection.items.size() > 0) {
                items.add(methodHandlerSection);
            }

            items.addAll(dataSectionItems);
        }
        // cp is useless now since all value are copied now
        cp.clean();
        cp = null;
    }

    public byte[] toByteArray() {

        // init structure for writing
        buildMapListItem();

        // place all item into file, we can know the size now
        final int size = place();

        ByteBuffer buffer = ByteBuffer.allocate(size);
        DataOut out = new ByteBufferOut(buffer);

        if (DEBUG) {
            out = wrapDumpOut(out);
        }
        // write it
        write(out);

        if (size != buffer.position()) {
            throw new RuntimeException("generated different file size, planned " + size
                    + ", but is " + buffer.position());
        }

        // update the CRC/ sha1 checksum in dex header
        updateChecksum(buffer, size);

        return buffer.array();
    }

    public static void updateChecksum(ByteBuffer buffer, int size) {
        byte[] data = buffer.array();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }

        digest.update(data, 32, size - 32);
        byte[] sha1 = digest.digest();
        System.arraycopy(sha1, 0, data, 12, sha1.length);

        Adler32 adler32 = new Adler32();
        adler32.update(data, 12, size - 12);
        int v = (int) adler32.getValue();
        buffer.position(8);
        buffer.putInt(v);
    }

    private void write(DataOut out) {
        List<SectionItem<?>> list = new ArrayList<>(mapItem.items);
        // mapItem is useless now
        this.mapItem = null;
        for (int i = 0; i < list.size(); i++) {
            SectionItem<?> section = list.get(i);
            list.set(i, null);
            BaseItem.addPadding(out, out.offset(),
                    section.sectionType.alignment);
            if (out.offset() != section.offset) {
                throw new RuntimeException(section.sectionType
                        + " start with different position, planned:"
                        + section.offset + ", but is:" + out.offset());
            }
            section.write(out);
        }
    }

    private int place() {
        // 2. order
        mapItem.cleanZeroSizeEntry();

        // 3. place
        int offset = 0;
        // int index = 0;
        for (SectionItem<?> section : mapItem.items) {

            offset = BaseItem.padding(offset, section.sectionType.alignment);
            section.offset = offset;
            // section.index = index;
            // index++;
            offset = section.place(offset);
        }
        int size = offset;
        { // fix size
            headItem.fileSize = size;
            // headItem is useless now
            this.headItem = null;
        }
        return size;
    }

    @Override
    public DexClassVisitor visit(int accessFlags, String name,
                                 String superClass, String[] itfClass) {
        ClassDefItem defItem = cp.putClassDefItem(accessFlags, name, superClass,
                itfClass);
        return new ClassWriter(defItem, cp);
    }

}
