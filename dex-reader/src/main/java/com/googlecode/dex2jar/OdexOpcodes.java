package com.googlecode.dex2jar;

public interface OdexOpcodes extends DexOpcodes {
    int OP_THROW_VERIFICATION_ERROR = 0x0000ed;
    int OP_EXECUTE_INLINE = 0x0000ee;
    int OP_INVOKE_SUPER_QUICK = 0x0000fa;
    int OP_INVOKE_VIRTUAL_QUICK = 0x0000f8;
    int OP_IGET_QUICK = 0x0000f2;
    int OP_IGET_WIDE_QUICK = 0x0000f3;
    int OP_IGET_OBJECT_QUICK = 0x0000f4;
    int OP_IPUT_QUICK = 0x0000f5;
    int OP_IPUT_WIDE_QUICK = 0x0000f6;
    int OP_IPUT_OBJECT_QUICK = 0x0000f7;
}
