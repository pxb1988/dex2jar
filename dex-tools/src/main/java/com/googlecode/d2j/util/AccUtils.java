package com.googlecode.d2j.util;

import org.objectweb.asm.Opcodes;

public class AccUtils {
    public static boolean isBridge(int acc) {
        return (acc & Opcodes.ACC_BRIDGE) != 0;
    }

    public static boolean isEnum(int acc) {
        return (acc & Opcodes.ACC_ENUM) != 0;
    }

    public static boolean isFinal(int acc) {
        return (acc & Opcodes.ACC_FINAL) != 0;
    }

    public static boolean isPrivate(int acc) {
        return (acc & Opcodes.ACC_PRIVATE) != 0;
    }

    public static boolean isProtected(int acc) {
        return (acc & Opcodes.ACC_PROTECTED) != 0;
    }

    public static boolean isPublic(int acc) {
        return (acc & Opcodes.ACC_PUBLIC) != 0;
    }

    public static boolean isStatic(int acc) {
        return (acc & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isSynthetic(int acc) {
        return (acc & Opcodes.ACC_SYNTHETIC) != 0;
    }

}
