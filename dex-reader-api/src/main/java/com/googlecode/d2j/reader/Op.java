package com.googlecode.d2j.reader;

import static com.googlecode.d2j.reader.InstructionFormat.*;
import static com.googlecode.d2j.reader.InstructionIndexType.*;

public enum Op implements CFG {
    NOP(0x00, "nop", kFmt10x, kIndexNone, kInstrCanContinue, false), //
    MOVE(0x01, "move", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MOVE_FROM16(0x02, "move/from16", kFmt22x, kIndexNone, kInstrCanContinue, true), //
    MOVE_16(0x03, "move/16", kFmt32x, kIndexNone, kInstrCanContinue, true), //
    MOVE_WIDE(0x04, "move-wide", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MOVE_WIDE_FROM16(0x05, "move-wide/from16", kFmt22x, kIndexNone, kInstrCanContinue, true), //
    MOVE_WIDE_16(0x06, "move-wide/16", kFmt32x, kIndexNone, kInstrCanContinue, true), //
    MOVE_OBJECT(0x07, "move-object", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MOVE_OBJECT_FROM16(0x08, "move-object/from16", kFmt22x, kIndexNone, kInstrCanContinue, true), //
    MOVE_OBJECT_16(0x09, "move-object/16", kFmt32x, kIndexNone, kInstrCanContinue, true), //
    MOVE_RESULT(0x0a, "move-result", kFmt11x, kIndexNone, kInstrCanContinue, true), //
    MOVE_RESULT_WIDE(0x0b, "move-result-wide", kFmt11x, kIndexNone, kInstrCanContinue, true), //
    MOVE_RESULT_OBJECT(0x0c, "move-result-object", kFmt11x, kIndexNone, kInstrCanContinue, true), //
    MOVE_EXCEPTION(0x0d, "move-exception", kFmt11x, kIndexNone, kInstrCanContinue, true), //
    RETURN_VOID(0x0e, "return-void", kFmt10x, kIndexNone, kInstrCanReturn, false), //
    RETURN(0x0f, "return", kFmt11x, kIndexNone, kInstrCanReturn, false), //
    RETURN_WIDE(0x10, "return-wide", kFmt11x, kIndexNone, kInstrCanReturn, false), //
    RETURN_OBJECT(0x11, "return-object", kFmt11x, kIndexNone, kInstrCanReturn, false), //
    CONST_4(0x12, "const/4", kFmt11n, kIndexNone, kInstrCanContinue, true), //
    CONST_16(0x13, "const/16", kFmt21s, kIndexNone, kInstrCanContinue, true), //
    CONST(0x14, "const", kFmt31i, kIndexNone, kInstrCanContinue, true), //
    CONST_HIGH16(0x15, "const/high16", kFmt21h, kIndexNone, kInstrCanContinue, true), //
    CONST_WIDE_16(0x16, "const-wide/16", kFmt21s, kIndexNone, kInstrCanContinue, true), //
    CONST_WIDE_32(0x17, "const-wide/32", kFmt31i, kIndexNone, kInstrCanContinue, true), //
    CONST_WIDE(0x18, "const-wide", kFmt51l, kIndexNone, kInstrCanContinue, true), //
    CONST_WIDE_HIGH16(0x19, "const-wide/high16", kFmt21h, kIndexNone, kInstrCanContinue, true), //
    CONST_STRING(0x1a, "const-string", kFmt21c, kIndexStringRef, kInstrCanContinue | kInstrCanThrow, true), //
    CONST_STRING_JUMBO(0x1b, "const-string/jumbo", kFmt31c, kIndexStringRef, kInstrCanContinue | kInstrCanThrow, true), //
    CONST_CLASS(0x1c, "const-class", kFmt21c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    MONITOR_ENTER(0x1d, "monitor-enter", kFmt11x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    MONITOR_EXIT(0x1e, "monitor-exit", kFmt11x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    CHECK_CAST(0x1f, "check-cast", kFmt21c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    INSTANCE_OF(0x20, "instance-of", kFmt22c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    ARRAY_LENGTH(0x21, "array-length", kFmt12x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    NEW_INSTANCE(0x22, "new-instance", kFmt21c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    NEW_ARRAY(0x23, "new-array", kFmt22c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    FILLED_NEW_ARRAY(0x24, "filled-new-array", kFmt35c, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow, true), //
    FILLED_NEW_ARRAY_RANGE(0x25, "filled-new-array/range", kFmt3rc, kIndexTypeRef, kInstrCanContinue | kInstrCanThrow,
            true), //
    FILL_ARRAY_DATA(0x26, "fill-array-data", kFmt31t, kIndexNone, kInstrCanContinue, false), //
    THROW(0x27, "throw", kFmt11x, kIndexNone, kInstrCanThrow, false), //
    GOTO(0x28, "goto", kFmt10t, kIndexNone, kInstrCanBranch, false), //
    GOTO_16(0x29, "goto/16", kFmt20t, kIndexNone, kInstrCanBranch, false), //
    GOTO_32(0x2a, "goto/32", kFmt30t, kIndexNone, kInstrCanBranch, false), //
    PACKED_SWITCH(0x2b, "packed-switch", kFmt31t, kIndexNone, kInstrCanContinue | kInstrCanSwitch, false), //
    SPARSE_SWITCH(0x2c, "sparse-switch", kFmt31t, kIndexNone, kInstrCanContinue | kInstrCanSwitch, false), //
    CMPL_FLOAT(0x2d, "cmpl-float", kFmt23x, kIndexNone, kInstrCanContinue, false), //
    CMPG_FLOAT(0x2e, "cmpg-float", kFmt23x, kIndexNone, kInstrCanContinue, false), //
    CMPL_DOUBLE(0x2f, "cmpl-double", kFmt23x, kIndexNone, kInstrCanContinue, false), //
    CMPG_DOUBLE(0x30, "cmpg-double", kFmt23x, kIndexNone, kInstrCanContinue, false), //
    CMP_LONG(0x31, "cmp-long", kFmt23x, kIndexNone, kInstrCanContinue, false), //
    IF_EQ(0x32, "if-eq", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_NE(0x33, "if-ne", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_LT(0x34, "if-lt", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_GE(0x35, "if-ge", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_GT(0x36, "if-gt", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_LE(0x37, "if-le", kFmt22t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_EQZ(0x38, "if-eqz", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_NEZ(0x39, "if-nez", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_LTZ(0x3a, "if-ltz", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_GEZ(0x3b, "if-gez", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_GTZ(0x3c, "if-gtz", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
    IF_LEZ(0x3d, "if-lez", kFmt21t, kIndexNone, kInstrCanBranch | kInstrCanContinue, false), //
//    UNUSED_3E(0x3e, "unused-3e", null, kIndexUnknown, 0, false), //
//    UNUSED_3F(0x3f, "unused-3f", null, kIndexUnknown, 0, false), //
//    UNUSED_40(0x40, "unused-40", null, kIndexUnknown, 0, false), //
//    UNUSED_41(0x41, "unused-41", null, kIndexUnknown, 0, false), //
//    UNUSED_42(0x42, "unused-42", null, kIndexUnknown, 0, false), //
//    UNUSED_43(0x43, "unused-43", null, kIndexUnknown, 0, false), //
    AGET(0x44, "aget", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_WIDE(0x45, "aget-wide", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_OBJECT(0x46, "aget-object", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_BOOLEAN(0x47, "aget-boolean", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_BYTE(0x48, "aget-byte", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_CHAR(0x49, "aget-char", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AGET_SHORT(0x4a, "aget-short", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    APUT(0x4b, "aput", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_WIDE(0x4c, "aput-wide", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_OBJECT(0x4d, "aput-object", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_BOOLEAN(0x4e, "aput-boolean", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_BYTE(0x4f, "aput-byte", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_CHAR(0x50, "aput-char", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    APUT_SHORT(0x51, "aput-short", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, false), //
    IGET(0x52, "iget", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_WIDE(0x53, "iget-wide", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_OBJECT(0x54, "iget-object", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_BOOLEAN(0x55, "iget-boolean", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_BYTE(0x56, "iget-byte", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_CHAR(0x57, "iget-char", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IGET_SHORT(0x58, "iget-short", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    IPUT(0x59, "iput", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_WIDE(0x5a, "iput-wide", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_OBJECT(0x5b, "iput-object", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_BOOLEAN(0x5c, "iput-boolean", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_BYTE(0x5d, "iput-byte", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_CHAR(0x5e, "iput-char", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    IPUT_SHORT(0x5f, "iput-short", kFmt22c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SGET(0x60, "sget", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_WIDE(0x61, "sget-wide", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_OBJECT(0x62, "sget-object", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_BOOLEAN(0x63, "sget-boolean", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_BYTE(0x64, "sget-byte", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_CHAR(0x65, "sget-char", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SGET_SHORT(0x66, "sget-short", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, true), //
    SPUT(0x67, "sput", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_WIDE(0x68, "sput-wide", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_OBJECT(0x69, "sput-object", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_BOOLEAN(0x6a, "sput-boolean", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_BYTE(0x6b, "sput-byte", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_CHAR(0x6c, "sput-char", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    SPUT_SHORT(0x6d, "sput-short", kFmt21c, kIndexFieldRef, kInstrCanContinue | kInstrCanThrow, false), //
    INVOKE_VIRTUAL(0x6e, "invoke-virtual", kFmt35c, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow | kInstrInvoke,
            true), //
    INVOKE_SUPER(0x6f, "invoke-super", kFmt35c, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow | kInstrInvoke,
            true), //
    INVOKE_DIRECT(0x70, "invoke-direct", kFmt35c, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow | kInstrInvoke,
            true), //
    INVOKE_STATIC(0x71, "invoke-static", kFmt35c, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow | kInstrInvoke,
            true), //
    INVOKE_INTERFACE(0x72, "invoke-interface", kFmt35c, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
//    UNUSED_73(0x73, "unused-73", null, kIndexUnknown, 0, false), //
    INVOKE_VIRTUAL_RANGE(0x74, "invoke-virtual/range", kFmt3rc, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
    INVOKE_SUPER_RANGE(0x75, "invoke-super/range", kFmt3rc, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
    INVOKE_DIRECT_RANGE(0x76, "invoke-direct/range", kFmt3rc, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
    INVOKE_STATIC_RANGE(0x77, "invoke-static/range", kFmt3rc, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
    INVOKE_INTERFACE_RANGE(0x78, "invoke-interface/range", kFmt3rc, kIndexMethodRef, kInstrCanContinue | kInstrCanThrow
            | kInstrInvoke, true), //
//    UNUSED_79(0x79, "unused-79", null, kIndexUnknown, 0, false), //
//    UNUSED_7A(0x7a, "unused-7a", null, kIndexUnknown, 0, false), //
    NEG_INT(0x7b, "neg-int", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    NOT_INT(0x7c, "not-int", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    NEG_LONG(0x7d, "neg-long", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    NOT_LONG(0x7e, "not-long", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    NEG_FLOAT(0x7f, "neg-float", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    NEG_DOUBLE(0x80, "neg-double", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_LONG(0x81, "int-to-long", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_FLOAT(0x82, "int-to-float", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_DOUBLE(0x83, "int-to-double", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    LONG_TO_INT(0x84, "long-to-int", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    LONG_TO_FLOAT(0x85, "long-to-float", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    LONG_TO_DOUBLE(0x86, "long-to-double", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    FLOAT_TO_INT(0x87, "float-to-int", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    FLOAT_TO_LONG(0x88, "float-to-long", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    FLOAT_TO_DOUBLE(0x89, "float-to-double", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DOUBLE_TO_INT(0x8a, "double-to-int", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DOUBLE_TO_LONG(0x8b, "double-to-long", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DOUBLE_TO_FLOAT(0x8c, "double-to-float", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_BYTE(0x8d, "int-to-byte", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_CHAR(0x8e, "int-to-char", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    INT_TO_SHORT(0x8f, "int-to-short", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    ADD_INT(0x90, "add-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SUB_INT(0x91, "sub-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    MUL_INT(0x92, "mul-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    DIV_INT(0x93, "div-int", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_INT(0x94, "rem-int", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_INT(0x95, "and-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    OR_INT(0x96, "or-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    XOR_INT(0x97, "xor-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SHL_INT(0x98, "shl-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SHR_INT(0x99, "shr-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    USHR_INT(0x9a, "ushr-int", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    ADD_LONG(0x9b, "add-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SUB_LONG(0x9c, "sub-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    MUL_LONG(0x9d, "mul-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    DIV_LONG(0x9e, "div-long", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_LONG(0x9f, "rem-long", kFmt23x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_LONG(0xa0, "and-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    OR_LONG(0xa1, "or-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    XOR_LONG(0xa2, "xor-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SHL_LONG(0xa3, "shl-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SHR_LONG(0xa4, "shr-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    USHR_LONG(0xa5, "ushr-long", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    ADD_FLOAT(0xa6, "add-float", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SUB_FLOAT(0xa7, "sub-float", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    MUL_FLOAT(0xa8, "mul-float", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    DIV_FLOAT(0xa9, "div-float", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    REM_FLOAT(0xaa, "rem-float", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    ADD_DOUBLE(0xab, "add-double", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    SUB_DOUBLE(0xac, "sub-double", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    MUL_DOUBLE(0xad, "mul-double", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    DIV_DOUBLE(0xae, "div-double", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    REM_DOUBLE(0xaf, "rem-double", kFmt23x, kIndexNone, kInstrCanContinue, true), //
    ADD_INT_2ADDR(0xb0, "add-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SUB_INT_2ADDR(0xb1, "sub-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MUL_INT_2ADDR(0xb2, "mul-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DIV_INT_2ADDR(0xb3, "div-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_INT_2ADDR(0xb4, "rem-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_INT_2ADDR(0xb5, "and-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    OR_INT_2ADDR(0xb6, "or-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    XOR_INT_2ADDR(0xb7, "xor-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SHL_INT_2ADDR(0xb8, "shl-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SHR_INT_2ADDR(0xb9, "shr-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    USHR_INT_2ADDR(0xba, "ushr-int/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    ADD_LONG_2ADDR(0xbb, "add-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SUB_LONG_2ADDR(0xbc, "sub-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MUL_LONG_2ADDR(0xbd, "mul-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DIV_LONG_2ADDR(0xbe, "div-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_LONG_2ADDR(0xbf, "rem-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_LONG_2ADDR(0xc0, "and-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    OR_LONG_2ADDR(0xc1, "or-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    XOR_LONG_2ADDR(0xc2, "xor-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SHL_LONG_2ADDR(0xc3, "shl-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SHR_LONG_2ADDR(0xc4, "shr-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    USHR_LONG_2ADDR(0xc5, "ushr-long/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    ADD_FLOAT_2ADDR(0xc6, "add-float/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SUB_FLOAT_2ADDR(0xc7, "sub-float/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MUL_FLOAT_2ADDR(0xc8, "mul-float/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DIV_FLOAT_2ADDR(0xc9, "div-float/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    REM_FLOAT_2ADDR(0xca, "rem-float/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    ADD_DOUBLE_2ADDR(0xcb, "add-double/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    SUB_DOUBLE_2ADDR(0xcc, "sub-double/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    MUL_DOUBLE_2ADDR(0xcd, "mul-double/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    DIV_DOUBLE_2ADDR(0xce, "div-double/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    REM_DOUBLE_2ADDR(0xcf, "rem-double/2addr", kFmt12x, kIndexNone, kInstrCanContinue, true), //
    ADD_INT_LIT16(0xd0, "add-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    RSUB_INT(0xd1, "rsub-int", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    MUL_INT_LIT16(0xd2, "mul-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    DIV_INT_LIT16(0xd3, "div-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_INT_LIT16(0xd4, "rem-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_INT_LIT16(0xd5, "and-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    OR_INT_LIT16(0xd6, "or-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    XOR_INT_LIT16(0xd7, "xor-int/lit16", kFmt22s, kIndexNone, kInstrCanContinue, true), //
    ADD_INT_LIT8(0xd8, "add-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    RSUB_INT_LIT8(0xd9, "rsub-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    MUL_INT_LIT8(0xda, "mul-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    DIV_INT_LIT8(0xdb, "div-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    REM_INT_LIT8(0xdc, "rem-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue | kInstrCanThrow, true), //
    AND_INT_LIT8(0xdd, "and-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    OR_INT_LIT8(0xde, "or-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    XOR_INT_LIT8(0xdf, "xor-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    SHL_INT_LIT8(0xe0, "shl-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    SHR_INT_LIT8(0xe1, "shr-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    USHR_INT_LIT8(0xe2, "ushr-int/lit8", kFmt22b, kIndexNone, kInstrCanContinue, true), //
    BAD_OP(-1, "bad-opcode", null, kIndexNone, 0, false), //
    ;
    public int opcode;
    public InstructionFormat format;
    /* package */InstructionIndexType indexType;
    /* package */int flags;
    public String displayName;
    public final static Op ops[] = new Op[256];
    public boolean changeFrame;
    static {
        Op[] ops = Op.ops;
        for (Op op : Op.values()) {
            if (op.opcode >= 0) {
                ops[op.opcode] = op;
            }
        }
    }

    public boolean canBranch() {
        return 0 != (flags & kInstrCanBranch);
    }

    public boolean canContinue() {
        return 0 != (flags & kInstrCanContinue);
    }

    public boolean canReturn() {
        return 0 != (flags & kInstrCanReturn);
    }

    public boolean canSwitch() {
        return 0 != (flags & kInstrCanSwitch);
    }

    public boolean canThrow() {
        return 0 != (flags & kInstrCanThrow);
    }

    Op(int op, String displayName, InstructionFormat fmt, InstructionIndexType indexType, int flags, boolean changeFrame) {
        this.opcode = op;
        this.displayName = displayName;
        this.format = fmt;
        this.indexType = indexType;
        this.flags = flags;
    }

    public String toString() {
        return displayName;
    }
}