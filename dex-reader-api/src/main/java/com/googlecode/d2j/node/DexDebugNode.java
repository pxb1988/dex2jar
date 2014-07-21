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
package com.googlecode.d2j.node;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.visitors.DexDebugVisitor;

public class DexDebugNode extends DexDebugVisitor {
    public List<DexDebugOpNode> debugNodes = new ArrayList<>();
    public List<String> parameterNames;
    public String fineName;

    protected void addDebug(DexDebugOpNode dexDebugNode) {
        debugNodes.add(dexDebugNode);
    }

    @Override
    public void visitSetFile(String file) {
        this.fineName = file;
    }

    @Override
    public void visitRestartLocal(int reg, DexLabel label) {
        addDebug(new DexDebugOpNode.RestartLocal(label, reg));
    }

    @Override
    public void visitParameterName(final int parameterIndex, final String name) {
        if (parameterNames == null) {
            parameterNames = new ArrayList<>();
        }
        while (parameterNames.size() <= parameterIndex) {
            parameterNames.add(null);
        }
        parameterNames.set(parameterIndex, name);
    }

    @Override
    public void visitLineNumber(final int line, final DexLabel label) {
        addDebug(new DexDebugOpNode.LineNumber(label, line));
    }

    @Override
    public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
        addDebug(new DexDebugOpNode.StartLocalNode(label, reg, name, type, signature));
    }

    @Override
    public void visitEndLocal(int reg, DexLabel label) {
        addDebug(new DexDebugOpNode.EndLocal(label, reg));
    }

    public void accept(DexDebugVisitor v) {
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.size(); i++) {
                String name = parameterNames.get(i);
                if (name != null) {
                    v.visitParameterName(i, name);
                }
            }
        }
        if (debugNodes != null) {
            for (DexDebugOpNode n : debugNodes) {
                n.accept(v);
            }
        }
        if (fineName != null) {
            v.visitSetFile(fineName);
        }
    }

    @Override
    public void visitPrologue(DexLabel dexLabel) {
        addDebug(new DexDebugOpNode.Prologue(dexLabel));
    }

    @Override
    public void visitEpiogue(DexLabel dexLabel) {
        addDebug(new DexDebugOpNode.Epiogue(dexLabel));
    }

    public abstract static class DexDebugOpNode {
        public DexLabel label;

        protected DexDebugOpNode(DexLabel label) {
            this.label = label;
        }

        public abstract void accept(DexDebugVisitor cv);

        public static class StartLocalNode extends DexDebugOpNode {
            public int reg;
            public String name;
            public String type;
            public String signature;

            public StartLocalNode(DexLabel label, int reg, String name, String type, String signature) {
                super(label);
                this.reg = reg;
                this.name = name;
                this.type = type;
                this.signature = signature;
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitStartLocal(reg, label, name, type, signature);
            }
        }

        public static class EndLocal extends DexDebugOpNode {
            public int reg;

            public EndLocal(DexLabel label, int reg) {
                super(label);
                this.reg = reg;
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitEndLocal(reg, label);
            }
        }

        public static class Epiogue extends DexDebugOpNode {

            public Epiogue(DexLabel label) {
                super(label);
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitEpiogue(label);
            }
        }

        public static class Prologue extends DexDebugOpNode {

            public Prologue(DexLabel label) {
                super(label);
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitPrologue(label);
            }
        }

        public static class RestartLocal extends DexDebugOpNode {
            public int reg;

            public RestartLocal(DexLabel label, int reg) {
                super(label);
                this.reg = reg;
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitRestartLocal(reg, label);
            }
        }

        public static class LineNumber extends DexDebugOpNode {
            public int line;

            public LineNumber(DexLabel label, int line) {
                super(label);
                this.line = line;
            }

            @Override
            public void accept(DexDebugVisitor cv) {
                cv.visitLineNumber(line, label);
            }
        }
    }
}
