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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.StmtList;

/**
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class IrMethod {

    public int access;
    public Type[] args;
    public List<Local> locals = new ArrayList<Local>();
    public String name;

    public Type owner;

    public Type ret;

    public StmtList stmts = new StmtList();

    public List<Trap> traps = new ArrayList<Trap>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("// ").append(this.owner == null ? null : this.owner.getClassName()).append("\n");
        sb.append(ToStringUtil.getAccDes(access)).append(ret == null ? null : ToStringUtil.toShortClassName(ret))
                .append(' ').append(this.name).append('(');
        if (args != null) {
            boolean first = true;
            for (Type arg : args) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(ToStringUtil.toShortClassName(arg));
            }
        }
        sb.append(") {\n\n").append(stmts).append("\n");
        if (traps.size() > 0) {
            sb.append("=============\n");
            for (Trap trap : traps) {
                sb.append(trap).append('\n');
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
