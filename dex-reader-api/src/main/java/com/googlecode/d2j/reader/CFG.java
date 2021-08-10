package com.googlecode.d2j.reader;

public interface CFG {
    int kInstrCanBranch = 1; // conditional or unconditional branch
    int kInstrCanContinue = 1 << 1; // flow can continue to next statement
    int kInstrCanSwitch = 1 << 2; // switch
    int kInstrCanThrow = 1 << 3; // could cause an exception to be thrown
    int kInstrCanReturn = 1 << 4; // returns, no additional statements
    int kInstrInvoke = 1 << 5; // a flavor of invoke

}
