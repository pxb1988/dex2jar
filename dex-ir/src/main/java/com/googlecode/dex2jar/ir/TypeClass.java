/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public String name;
    public boolean fixed;

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
                    // FIXME check out the infect of move-result-object
                    // FIXME div-int
                    throw new RuntimeException("can not merge " + thizCls + " and " + clz);
                } else {
                    return thizCls;
                }
            } else {
                if (clz.fixed) {
                    return clz;
                } else { // both not fixed
                    switch (thizCls) {
                    case ZIL:
                        switch (clz) {
                        case ZIFL:
                            return thizCls;
                        case IF:
                            return TypeClass.INT;
                        case ZIF:
                        case ZI:
                            return ZI;

                        case JD:
                            throw new RuntimeException();
                        default:
                        }
                    case ZIFL:
                        switch (clz) {
                        case ZIL:
                        case IF:
                        case ZIF:
                        case ZI:
                            return clz;

                        case JD:
                            throw new RuntimeException();
                        default:
                        }
                    case IF:
                        switch (clz) {
                        case ZIL:
                        case ZI:
                            return TypeClass.INT;
                        case ZIFL:
                        case ZIF:
                            return thizCls;

                        case JD:
                            throw new RuntimeException();
                        default:
                        }
                    case ZIF:
                        switch (clz) {
                        case IF:
                            return clz;
                        case ZIL:
                        case ZI:
                            return ZI;
                        case ZIFL:
                            return thizCls;

                        case JD:
                            throw new RuntimeException();
                        default:
                        }
                    case ZI:
                        switch (clz) {
                        case IF:
                            return INT;
                        case ZIL:
                        case ZIFL:
                        case ZIF:
                            return thizCls;
                        case JD:
                            throw new RuntimeException();
                        default:
                        }
                    case JD:
                        throw new RuntimeException();
                    default:
                    }
                }
            }
        }

        throw new RuntimeException();
    }

    public String toString() {
        return name;
    }

}
