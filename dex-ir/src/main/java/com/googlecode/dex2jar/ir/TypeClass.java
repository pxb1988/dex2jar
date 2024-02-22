package com.googlecode.dex2jar.ir;

public enum TypeClass {

    BOOLEAN("Z", true), //
    INT("I", true), //
    FLOAT("F", true), //
    DOUBLE("D", true), //
    LONG("J", true), //
    OBJECT("L", true), //
    VOID("V", true), //
    UNKNOWN("?"), //
    ZIL("s"), //
    ZIFL("z"), //
    ZIF("m"), //
    ZI("n"), //
    IF("i"), //
    JD("w"); //

    public final String name;
    public final boolean fixed;

    TypeClass(String use, boolean fixed) {
        this.name = use;
        this.fixed = fixed;
    }

    TypeClass(String use) {
        this.name = use;
        this.fixed = false;
    }

    public static TypeClass clzOf(String desc) {
        switch (desc.charAt(0)) {
        case 'Z':
            return BOOLEAN;
        case 'B':
        case 'C':
        case 'S':
        case 'I':
            return INT;
        case 'F':
            return FLOAT;
        case 'D':
            return DOUBLE;
        case 'J':
            return LONG;
        case 'L':
        case '[':
            return OBJECT;
        case 'V':
            return VOID;
        case 'z':
            return ZIFL;
        case 's':
            return ZIL;
        case 'i':
            return IF;
        case 'm':
            return ZIF;
        case 'n':
            return ZI;
        case 'w':
            return JD;
        default:
            return UNKNOWN;
        }
    }

    public static TypeClass merge(TypeClass thizCls, TypeClass clz) {
        if (thizCls == clz) {
            return thizCls;
        }
        if (thizCls == TypeClass.UNKNOWN) {
            return clz;
        } else if (clz == TypeClass.UNKNOWN) {
            // do nothing
            return thizCls;
        } else {
            if (thizCls.fixed) {
                if (clz.fixed) {
                    // special case for merge I and Z
                    // https://bitbucket.org/pxb1988/dex2jar/issues/1/javalangruntimeexception-can-not-merge-i
                    // http://sourceforge.net/p/dex2jar/tickets/237/
                    if ((thizCls == INT && clz == BOOLEAN) || (thizCls == BOOLEAN && clz == INT)) {
                        return INT;
                    }
                    throw new RuntimeException("Can't merge " + thizCls + " and " + clz);
                } else {
                    return thizCls;
                }
            } else if (clz.fixed) {
                return clz;
            } else { // both not fixed
                return merge0(thizCls, clz);
            }
        }
    }

    /**
     * X     ZIL   ZIFL ZIF  ZI  IF
     * ZIL   X     ZIL  ZI   ZI  I
     * ZIFL  ZIL   X    ZIF  ZI  IF
     * ZIF   ZI    ZIF  X    ZI  IF
     * ZI    ZI    ZI   ZI   X   I
     * IF    I     IF   IF   I   X
     */
    private static TypeClass merge0(TypeClass a, TypeClass b) {
        if (a == JD || b == JD) {
            throw new RuntimeException("Can't merge " + a + " and " + b);
        }
        switch (a) {
        case ZIL:
            switch (b) {
            case ZIFL:
                return ZIL;
            case IF:
                return INT;
            case ZIF:
            case ZI:
                return ZI;
            default:
            }
        case ZIFL:
            return b;
        case IF:
            switch (b) {
            case ZIL:
            case ZI:
                return INT;
            case ZIFL:
            case ZIF:
                return IF;
            default:
            }
        case ZIF:
            switch (b) {
            case IF:
                return IF;
            case ZIL:
            case ZI:
                return ZI;
            case ZIFL:
                return ZIF;
            default:
            }
        case ZI:
            if (b == TypeClass.IF) {
                return INT;
            } else {
                return ZI;
            }
        default:
        }
        throw new RuntimeException();
    }

    public String toString() {
        return name;
    }

}
