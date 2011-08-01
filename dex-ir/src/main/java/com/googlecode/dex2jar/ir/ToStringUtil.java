/*
 * Copyright (c) 2009-2011 Panxiaobo
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * TODO DOC
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ToStringUtil {
    public static String getAccDes(int acc) {
        StringBuilder sb = new StringBuilder();
        if ((acc & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((acc & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((acc & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((acc & Opcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((acc & Opcodes.ACC_ABSTRACT) != 0 && (acc & Opcodes.ACC_INTERFACE) == 0) {
            sb.append("abstract ");
        }
        if ((acc & Opcodes.ACC_ANNOTATION) != 0) {
            sb.append("annotation ");
        }
        if ((acc & Opcodes.ACC_BRIDGE) != 0) {
            sb.append("bridge ");
        }
        if ((acc & Opcodes.ACC_DEPRECATED) != 0) {
            sb.append("deprecated ");
        }
        if ((acc & Opcodes.ACC_ENUM) != 0) {
            sb.append("enum ");
        }
        if ((acc & Opcodes.ACC_FINAL) != 0) {
            sb.append("final ");
        }
        if ((acc & Opcodes.ACC_INTERFACE) != 0) {
            sb.append("interace ");
        }
        if ((acc & Opcodes.ACC_NATIVE) != 0) {
            sb.append("native ");
        }
        if ((acc & Opcodes.ACC_STRICT) != 0) {
            sb.append("strict ");
        }
        if ((acc & Opcodes.ACC_SYNCHRONIZED) != 0) {
            sb.append("synchronized ");
        }
        if ((acc & Opcodes.ACC_TRANSIENT) != 0) {
            sb.append("transient ");
        }
        if ((acc & Opcodes.ACC_VARARGS) != 0) {
            sb.append("varargs ");
        }
        if ((acc & Opcodes.ACC_VOLATILE) != 0) {
            sb.append("volatile ");
        }
        return sb.toString();
    }

    public static String toShortClassName(Type t) {
        String cn = t.getClassName();
        int i = cn.lastIndexOf('.');
        return i > 0 ? cn.substring(i + 1) : cn;
    }
}
