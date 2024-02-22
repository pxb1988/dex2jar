package com.googlecode.d2j.reader;

/* package */ enum InstructionIndexType {

    kIndexUnknown, //

    kIndexNone, // has no index

    kIndexVaries, // "It depends." Used for throw-verification-error

    kIndexTypeRef, // type reference index

    kIndexStringRef, // string reference index

    kIndexMethodRef, // method reference index

    kIndexFieldRef, // field reference index

    kIndexInlineMethod, // inline method index (for inline linked methods)

    kIndexVtableOffset, // vtable offset (for static linked methods)

    kIndexFieldOffset, // field offset (for static linked fields)

    kIndexMethodAndProtoRef, // 038

    kIndexCallSiteRef, // 038

    kIndexMethodHandleRef, // 039, constant method handle reference index

    kIndexProtoRef, // 039, prototype reference index

}
