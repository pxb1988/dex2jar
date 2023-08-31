package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class HeadItem extends BaseItem {

    public static final int V035 = 0x00353330;

    public static final int V036 = 0x00363330;

    public int version = V035;

    public SectionItem<MapListItem> mapSection;

    public SectionItem<StringIdItem> stringIdSection;

    public SectionItem<TypeIdItem> typeIdSection;

    public SectionItem<ProtoIdItem> protoIdSection;

    public SectionItem<FieldIdItem> fieldIdSection;

    public SectionItem<MethodIdItem> methodIdSection;

    public SectionItem<ClassDefItem> classDefSection;

    public int fileSize = -1;

    public void write(DataOut out) {
        out.uint("magic", 0x0A786564);
        out.uint("version", version);
        out.skip4("checksum");
        out.skip("signature", 20);
        out.uint("file_size", fileSize);
        out.uint("head_size", 0x70);
        out.uint("endian_tag", 0x12345678);
        out.skip("link_size,link_off", 8);
        out.uint("map_off", mapSection.items.isEmpty() ? 0 : mapSection.offset);
        out.uint("string_ids_size", stringIdSection.items.size());
        out.uint("string_ids_off", stringIdSection.items.isEmpty() ? 0 : stringIdSection.offset);

        out.uint("type_ids_size", typeIdSection.items.size());
        out.uint("type_ids_off", typeIdSection.items.isEmpty() ? 0 : typeIdSection.offset);

        out.uint("proto_ids_size", protoIdSection.items.size());
        out.uint("proto_ids_off", protoIdSection.items.isEmpty() ? 0 : protoIdSection.offset);

        out.uint("field_ids_size", fieldIdSection.items.size());
        out.uint("field_ids_off", fieldIdSection.items.isEmpty() ? 0 : fieldIdSection.offset);

        out.uint("method_ids_size", methodIdSection.items.size());
        out.uint("method_ids_off", methodIdSection.items.isEmpty() ? 0 : methodIdSection.offset);
        out.uint("class_defs_size", classDefSection.items.size());
        out.uint("class_defs_off", classDefSection.items.isEmpty() ? 0 : classDefSection.offset);

        out.uint("data_size", fileSize - mapSection.offset);   // every thing after map is data section
        out.uint("data_off", mapSection.offset); // map is the first in data section

    }

    @Override
    public int place(int offset) {
        return offset + 0x70;
    }

}
