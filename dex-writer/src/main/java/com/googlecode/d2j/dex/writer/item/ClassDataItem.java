package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Idx;
import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClassDataItem extends BaseItem {

    public final List<EncodedField> staticFields = new ArrayList<>(5);

    public final List<EncodedField> instanceFields = new ArrayList<>(5);

    public final List<EncodedMethod> directMethods = new ArrayList<>(5);

    public final List<EncodedMethod> virtualMethods = new ArrayList<>(5);

    @Override
    public int place(int offset) {
        offset += lengthOfUleb128(staticFields.size());
        offset += lengthOfUleb128(instanceFields.size());
        offset += lengthOfUleb128(directMethods.size());
        offset += lengthOfUleb128(virtualMethods.size());
        offset = placeField(offset, staticFields);
        offset = placeField(offset, instanceFields);
        offset = placeMethod(offset, directMethods);
        offset = placeMethod(offset, virtualMethods);
        return offset;
    }

    private int placeMethod(int offset, List<EncodedMethod> methods) {
        if (methods.isEmpty()) {
            return offset;
        }
        int lastIdx = 0;
        for (EncodedMethod f : methods) {
            offset += lengthOfUleb128(f.method.index - lastIdx);
            offset += lengthOfUleb128(f.accessFlags);
            offset += lengthOfUleb128(f.code == null ? 0 : f.code.offset);
            lastIdx = f.method.index;
        }
        return offset;
    }

    private int placeField(int offset, List<EncodedField> fields) {
        if (fields.isEmpty()) {
            return offset;
        }
        int lastIdx = 0;
        for (EncodedField f : fields) {
            offset += lengthOfUleb128(f.field.index - lastIdx);
            offset += lengthOfUleb128(f.accessFlags);

            lastIdx = f.field.index;
        }
        return offset;
    }

    @Override
    public void write(DataOut out) {
        out.uleb128("static_fields_size", staticFields.size());
        out.uleb128("instance_fields_size", instanceFields.size());
        out.uleb128("ditect_methods_size", directMethods.size());
        out.uleb128("virtual_methods_size", virtualMethods.size());
        writeField(out, staticFields);
        writeField(out, instanceFields);
        writeMethod(out, directMethods);
        writeMethod(out, virtualMethods);
    }

    private void writeMethod(DataOut out, List<EncodedMethod> methods) {
        if (methods == null || methods.isEmpty()) {
            return;
        }
        int lastIdx = 0;
        for (EncodedMethod f : methods) {
            out.uleb128("method_idx_diff", f.method.index - lastIdx);
            out.uleb128("access_flags", f.accessFlags);
            out.uleb128("code_off", f.code == null ? 0 : f.code.offset);
            lastIdx = f.method.index;
        }
    }

    private void writeField(DataOut out, List<EncodedField> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        int lastIdx = 0;
        for (EncodedField f : fields) {
            out.uleb128("field_idx_diff", f.field.index - lastIdx);
            out.uleb128("access_flags", f.accessFlags);
            lastIdx = f.field.index;
        }
    }

    public int getMemberSize() {
        return instanceFields.size() + staticFields.size() + directMethods.size() + virtualMethods.size();
    }

    public void prepare(ConstPool cp) {
        Comparator<EncodedField> fc = Comparator.comparing(arg0 -> arg0.field);
        Comparator<EncodedMethod> mc = Comparator.comparing(arg0 -> arg0.method);
        instanceFields.sort(fc);
        staticFields.sort(fc);
        directMethods.sort(mc);
        virtualMethods.sort(mc);

    }

    public static class EncodedField {

        public int accessFlags;

        @Idx
        public FieldIdItem field;

        public EncodedValue staticValue;

        public AnnotationSetItem annotationSetItem;

    }

    public static class EncodedMethod {

        public int accessFlags;

        @Idx
        public MethodIdItem method;

        @Off
        public CodeItem code;

        //
        public AnnotationSetItem annotationSetItem;

        public AnnotationSetRefListItem parameterAnnotation;

    }

}
