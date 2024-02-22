package com.googlecode.d2j;

/**
 * constants in dex file
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public interface DexConstants {

    int ACC_PUBLIC = 0x0001; // class, field, method

    int ACC_PRIVATE = 0x0002; // class, field, method

    int ACC_PROTECTED = 0x0004; // class, field, method

    int ACC_STATIC = 0x0008; // field, method

    int ACC_FINAL = 0x0010; // class, field, method

    // int ACC_SUPER = 0x0020; // class

    int ACC_SYNCHRONIZED = 0x0020; // method

    int ACC_VOLATILE = 0x0040; // field

    int ACC_BRIDGE = 0x0040; // method

    int ACC_VARARGS = 0x0080; // method

    int ACC_TRANSIENT = 0x0080; // field

    int ACC_NATIVE = 0x0100; // method

    int ACC_INTERFACE = 0x0200; // class

    int ACC_ABSTRACT = 0x0400; // class, method

    int ACC_STRICT = 0x0800; // method

    int ACC_SYNTHETIC = 0x1000; // class, field, method

    int ACC_ANNOTATION = 0x2000; // class

    int ACC_ENUM = 0x4000; // class(?) field inner

    int ACC_CONSTRUCTOR = 0x10000; // constructor method (class or instance initializer)

    int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    int ACC_VISIBILITY_FLAGS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED;

    int ACC_DEX_HIDDEN_BIT = 0x00000020; // field, method (not native)

    int ACC_DEX_HIDDEN_BIT_NATIVE = 0x00000200; // method (native)

    String ANNOTATION_DEFAULT_TYPE = "Ldalvik/annotation/AnnotationDefault;";

    String ANNOTATION_SIGNATURE_TYPE = "Ldalvik/annotation/Signature;";

    String ANNOTATION_THROWS_TYPE = "Ldalvik/annotation/Throws;";

    String ANNOTATION_ENCLOSING_CLASS_TYPE = "Ldalvik/annotation/EnclosingClass;";

    String ANNOTATION_ENCLOSING_METHOD_TYPE = "Ldalvik/annotation/EnclosingMethod;";

    String ANNOTATION_INNER_CLASS_TYPE = "Ldalvik/annotation/InnerClass;";

    String ANNOTATION_MEMBER_CLASSES_TYPE = "Ldalvik/annotation/MemberClasses;";

    static int toMiniAndroidApiLevel(int dexVersion) {
        if (dexVersion <= DEX_035 || dexVersion <= DEX_036) {
            return 0;
        } else if (dexVersion == DEX_037) {
            return 24;
        } else if (dexVersion == DEX_038) {
            return 26;
        } else {
            return 28;
        }
    }

    int DEX_035 = 0x00303335;

    int DEX_036 = 0x00303336;
    // android 7.0, api 24
    int DEX_037 = 0x00303337;
    // android 8.0, api 26
    int DEX_038 = 0x00303338;
    // android 9.0, api 28
    int DEX_039 = 0x00303339;

    int DEX_040 = 0x00303340;

}
