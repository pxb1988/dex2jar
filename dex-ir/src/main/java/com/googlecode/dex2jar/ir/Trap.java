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

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Trap {
    public LabelStmt start, end, handlers[];
    public String types[];

    public Trap() {
        super();
    }

    public Trap(LabelStmt start, LabelStmt end, LabelStmt handlers[], String types[]) {
        super();
        this.start = start;
        this.end = end;
        this.handlers = handlers;
        this.types = types;
    }

    public Trap clone(LabelAndLocalMapper mapper) {
        int size = handlers.length;
        LabelStmt[] cloneHandlers = new LabelStmt[size];
        String[] cloneTypes = new String[size];
        for (int i = 0; i < size; i++) {
            cloneHandlers[i] = handlers[i].clone(mapper);
            cloneTypes[i] = types[i];
        }
        return new Trap(start.clone(mapper), end.clone(mapper), cloneHandlers, cloneTypes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format(".catch %s - %s : ", start.getDisplayName(),
                end.getDisplayName()));
        for (int i = 0; i < handlers.length; i++) {
            sb.append(types[i] == null ? "all" : types[i]).append(" > ").append(handlers[i].getDisplayName())
                    .append(",");
        }
        return sb.toString();
    }
}
