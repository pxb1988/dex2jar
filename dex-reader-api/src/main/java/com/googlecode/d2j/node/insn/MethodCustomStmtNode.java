/*
 * Copyright (c) 2009-2017 Panxiaobo
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
package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class MethodCustomStmtNode extends AbstractMethodStmtNode {
    public final String name;
    public final Proto proto;
    public final MethodHandle bsm;
    public final Object[] bsmArgs;

    public MethodCustomStmtNode(Op op, int[] args, String name, Proto proto, MethodHandle bsm, Object[] bsmArgs) {
        super(op, args);
        this.proto = proto;
        this.name = name;
        this.bsm = bsm;
        this.bsmArgs = bsmArgs;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitMethodStmt(op, args, name, proto, bsm, bsmArgs);
    }

    @Override
    public Proto getProto() {
        return proto;
    }
}
