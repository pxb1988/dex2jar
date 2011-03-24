/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import soot.RefType;
import soot.SootClass;
import soot.options.Options;

import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;
import static com.googlecode.dex2jar.soot.SootUtil.*;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class SootDexFileVisitor implements DexFileVisitor {
    protected Map<String, Integer> accessFlagsMap;

    static {
        Options.v().set_allow_phantom_refs(true);
    }

    /**
     * @param accessFlagsMap
     * @param classVisitorFactory
     */
    public SootDexFileVisitor(Map<String, Integer> accessFlagsMap) {
        this.accessFlagsMap = accessFlagsMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
        access_flags |= Opcodes.ACC_SUPER;// 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
        SootClass sootClass = soot.Scene.v().getSootClass(toJavaClassName(className));

        sootClass.setPhantom(false);
        sootClass.setModifiers(access_flags);

        if (superClass != null) {
            sootClass.setSuperclass(RefType.v(Type.getType(superClass).getClassName()).getSootClass());
        }
        if (interfaceNames != null) {
            for (String itf : interfaceNames) {
                sootClass.addInterface(RefType.v(Type.getType(itf).getClassName()).getSootClass());
            }
        }
        return new SootClassAdapter(accessFlagsMap, sootClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {
    }

}
