/*
 * dex2jar - Tools to work with android .dex and java .class files
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
package com.googlecode.dex2jar.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.InnerClassNode;

/**
 * 
 * sort visit to {@link #visitInnerClass(String, String, String, int)}
 * 
 * @author Panxiaobo
 * 
 */
public class OrderInnerOutterInsnNodeClassAdapter extends ClassAdapter implements Comparator<InnerClassNode> {
    public OrderInnerOutterInsnNodeClassAdapter(ClassVisitor cv) {
        super(cv);
    }

    private final List<InnerClassNode> innerClassNodes = new ArrayList<InnerClassNode>(5);

    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        InnerClassNode icn = new InnerClassNode(name, outerName, innerName, access);
        innerClassNodes.add(icn);
    }

    @Override
    public void visitEnd() {
        Collections.sort(this.innerClassNodes, this);
        if (innerClassNodes.size() > 0) {
            for (InnerClassNode icn : innerClassNodes) {
                icn.accept(cv);
            }
        }
        super.visitEnd();
    }

    @Override
    public int compare(InnerClassNode o1, InnerClassNode o2) {
        return o1.name.compareTo(o2.name);
    }
}
