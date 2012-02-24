/*
 * Copyright (c) 2009-2012 Panxiaobo
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

import java.util.Map;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Trap {
    public LabelStmt start, end, handler;
    public Type type;

    public Trap() {
        super();
    }

    public Trap(LabelStmt start, LabelStmt end, LabelStmt handler, Type type) {
        super();
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public Trap clone(Map<LabelStmt, LabelStmt> map) {
        return new Trap(start.clone(map), end.clone(map), handler.clone(map), type);
    }

    @Override
    public String toString() {
        return String.format(".catch %s - %s > %s // %s", start.getDisplayName(), end.getDisplayName(), handler.getDisplayName(),
                type == null ? "all" : ToStringUtil.toShortClassName(type));
    }

}
