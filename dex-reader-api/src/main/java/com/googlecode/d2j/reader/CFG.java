package com.googlecode.d2j.reader;

public interface CFG {

    int K_INSTR_CAN_BRANCH = 1; // conditional or unconditional branch

    int K_INSTR_CAN_CONTINUE = 1 << 1; // flow can continue to next statement

    int K_INSTR_CAN_SWITCH = 1 << 2; // switch

    int K_INSTR_CAN_THROW = 1 << 3; // could cause an exception to be thrown

    int K_INSTR_CAN_RETURN = 1 << 4; // returns, no additional statements

    int K_INSTR_INVOKE = 1 << 5; // a flavor of invoke

}
