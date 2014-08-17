/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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

import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;

import java.util.ArrayList;
import java.util.List;

public class DexFileNode extends DexFileVisitor {
    public List<DexClassNode> clzs = new ArrayList<>();

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        DexClassNode cn = new DexClassNode(access_flags, className, superClass, interfaceNames);
        clzs.add(cn);
        return cn;
    }

    public void accept(DexClassVisitor dcv) {
        for (DexClassNode cn : clzs) {
            cn.accept(dcv);
        }
    }

    public void accept(DexFileVisitor dfv) {
        for (DexClassNode cn : clzs) {
            cn.accept(dfv);
        }
    }
}
