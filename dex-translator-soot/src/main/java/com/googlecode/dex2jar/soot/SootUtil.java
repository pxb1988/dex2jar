/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot;

import org.objectweb.asm.Type;

public class SootUtil {

    public static soot.Type toSootType(String clz) {
        return toSootType(Type.getType(clz));
    }

    public static soot.Type toSootType(org.objectweb.asm.Type type) {
        switch (type.getSort()) {
        case Type.ARRAY:
            return soot.ArrayType.v(toSootType(type.getElementType()), type.getDimensions());
        case Type.BOOLEAN:
            return soot.BooleanType.v();
        case Type.BYTE:
            return soot.ByteType.v();
        case Type.CHAR:
            return soot.CharType.v();
        case Type.DOUBLE:
            return soot.DoubleType.v();
        case Type.FLOAT:
            return soot.FloatType.v();
        case Type.INT:
            return soot.IntType.v();
        case Type.LONG:
            return soot.LongType.v();
        case Type.OBJECT:
            return soot.RefType.v(type.getClassName());
        case Type.SHORT:
            return soot.ShortType.v();
        case Type.VOID:
            return soot.VoidType.v();
        }
        throw new RuntimeException();
    }
}
