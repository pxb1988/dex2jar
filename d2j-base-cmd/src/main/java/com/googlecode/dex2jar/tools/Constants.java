package com.googlecode.dex2jar.tools;

import org.objectweb.asm.Opcodes;

public class Constants {

    public static final int[] JAVA_VERSIONS = new int[]{
            0,
            Opcodes.V1_1,
            Opcodes.V1_2,
            Opcodes.V1_3,
            Opcodes.V1_4,
            Opcodes.V1_5,
            Opcodes.V1_6,
            Opcodes.V1_7,
            Opcodes.V1_8,
            Opcodes.V9,
            Opcodes.V10,
            Opcodes.V11,
            Opcodes.V12,
            Opcodes.V13,
            Opcodes.V14,
            Opcodes.V15,
            Opcodes.V16,
            Opcodes.V17,
            Opcodes.V18
    };

    public static final int ASM_VERSION = Opcodes.ASM9;

}
