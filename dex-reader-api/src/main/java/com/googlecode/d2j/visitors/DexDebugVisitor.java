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
package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexLabel;

public class DexDebugVisitor {
    protected DexDebugVisitor visitor;

    public DexDebugVisitor() {
    }

    public DexDebugVisitor(DexDebugVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * 
     * @param parameterIndex
     *            start with the first index of DexMethod.getParameterTypes(), no 'this'
     * @param name
     */
    public void visitParameterName(int parameterIndex, String name) {
        if (visitor != null) {
            visitor.visitParameterName(parameterIndex, name);
        }
    }

    public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
        if (visitor != null) {
            visitor.visitStartLocal(reg, label, name, type, signature);
        }
    }

    public void visitLineNumber(int line, DexLabel label) {
        if (visitor != null) {
            visitor.visitLineNumber(line, label);
        }
    }

    public void visitEndLocal(int reg, DexLabel label) {
        if (visitor != null) {
            visitor.visitEndLocal(reg, label);
        }
    }

    public void visitSetFile(String file) {
        if (visitor != null) {
            visitor.visitSetFile(file);
        }
    }

    public void visitPrologue(DexLabel dexLabel) {
        if (visitor != null) {
            visitor.visitPrologue(dexLabel);
        }
    }

    public void visitEpiogue(DexLabel dexLabel) {
        if (visitor != null) {
            visitor.visitEpiogue(dexLabel);
        }
    }

    public void visitRestartLocal(int reg, DexLabel label) {
        if (visitor != null) {
            visitor.visitRestartLocal(reg, label);
        }
    }

    public void visitEnd() {
        if (visitor != null) {
            visitor.visitEnd();
        }
    }
}
