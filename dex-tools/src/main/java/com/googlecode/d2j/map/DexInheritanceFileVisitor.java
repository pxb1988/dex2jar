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
package com.googlecode.d2j.map;


import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

public class DexInheritanceFileVisitor extends DexFileVisitor {
    final InheritanceTree tree;

    public DexInheritanceFileVisitor(InheritanceTree tree) {
        this.tree = tree;
    }


    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        final InheritanceTree.Clz clz = tree.addClz(access_flags, className);
        if(clz == null) { // skip the class
            return null;
        }
        if (superClass != null) {
            clz.relateSuper(superClass);
        }
        if (interfaceNames != null) {
            for (String s : interfaceNames) {
                clz.relateInterface(s);
            }
        }
        return new DexClassVisitor() {
            @Override
            public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
                clz.addField(accessFlags, field.getName(), field.getType());
                return null;
            }

            @Override
            public DexMethodVisitor visitMethod(int accessFlags, Method method) {
                clz.addMethod(accessFlags, method.getName(), method.getParameterTypes(), method.getReturnType());
                return null;
            }
        };
    }
}
