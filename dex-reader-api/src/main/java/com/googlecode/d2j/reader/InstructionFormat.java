package com.googlecode.d2j.reader;

public enum InstructionFormat {
    // kFmt00x(0), // unknown format (also used for "breakpoint" opcode)
    kFmt10x(1), // op
    kFmt12x(1), // op vA, vB
    kFmt11n(1), // op vA, #+B
    kFmt11x(1), // op vAA
    kFmt10t(1), // op +AA
    // kFmt20bc(2), // [opt] op AA, thing@BBBB
    kFmt20t(2), // op +AAAA
    kFmt22x(2), // op vAA, vBBBB
    kFmt21t(2), // op vAA, +BBBB
    kFmt21s(2), // op vAA, #+BBBB
    kFmt21h(2), // op vAA, #+BBBB00000[00000000]
    kFmt21c(2), // op vAA, thing@BBBB
    kFmt23x(2), // op vAA, vBB, vCC
    kFmt22b(2), // op vAA, vBB, #+CC
    kFmt22t(2), // op vA, vB, +CCCC
    kFmt22s(2), // op vA, vB, #+CCCC
    kFmt22c(2), // op vA, vB, thing@CCCC
    // kFmt22cs(2), // [opt] op vA, vB, field offset CCCC
    kFmt30t(3), // op +AAAAAAAA
    kFmt32x(3), // op vAAAA, vBBBB
    kFmt31i(3), // op vAA, #+BBBBBBBB
    kFmt31t(3), // op vAA, +BBBBBBBB
    kFmt31c(3), // op vAA, string@BBBBBBBB
    kFmt35c(3), // op {vC,vD,vE,vF,vG}, thing@BBBB
    // kFmt35ms(3), // [opt] invoke-virtual+super
    kFmt3rc(3), // op {vCCCC .. v(CCCC+AA-1)}, thing@BBBB
    // kFmt3rms(3), // [opt] invoke-virtual+super/range
    kFmt51l(5), // op vAA, #+BBBBBBBBBBBBBBBB
    // kFmt35mi(3), // [opt] inline invoke
    // kFmt3rmi(3), // [opt] inline invoke/range

    ;
    public int size;

    InstructionFormat(int size) {
        this.size = size;
    }
};
