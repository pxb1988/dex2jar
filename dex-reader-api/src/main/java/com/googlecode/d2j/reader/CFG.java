package com.googlecode.d2j.reader;

public interface CFG {
    public static final int kInstrCanBranch = 1; // conditional or unconditional branch
    public static final int kInstrCanContinue = 1 << 1; // flow can continue to next statement
    public static final int kInstrCanSwitch = 1 << 2; // switch
    public static final int kInstrCanThrow = 1 << 3; // could cause an exception to be thrown
    public static final int kInstrCanReturn = 1 << 4; // returns, no additional statements
    public static final int kInstrInvoke = 1 << 5; // a flavor of invoke

}
