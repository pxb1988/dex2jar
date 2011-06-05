package pxb.xjimple;

public abstract class Value {
    public static enum VT {

        ADD, AND, ARRAY, CAST, CMP, CMPG, CMPL, DIV, EQ, FIELD, GE, GT, INSTANCEOF, INTERFACE_INVOKE, LE, LENGTH, LT, MUL, NE, NEG, NEW, NEW_ARRAY, NEW_MUTI_ARRAY, OR, REM, SHL, SHR, SPECIAL_INVOKE, //
        STATIC_INVOKE, SUB, USHR, VIRTUAL_INVOKE, XOR, LOCAL, CONSTANT, THIS_REF, PARAMETER_REF, EXCEPTION_REF
    }

    final public VT vt;

    /**
     * @param vt
     */
    public Value(VT vt) {
        super();
        this.vt = vt;
    }
}
