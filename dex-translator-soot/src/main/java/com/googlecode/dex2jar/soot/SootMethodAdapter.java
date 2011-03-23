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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class SootMethodAdapter implements DexMethodVisitor, Opcodes {
    final protected List<Annotation> anns = new ArrayList<Annotation>();
    final protected Method method;
    SootMethod sootMethod;
    final protected List<Annotation>[] paramAnns;
    private static final Logger log = LoggerFactory.getLogger(SootMethodAdapter.class);
    SootClass owner;

    /**
     * @param cv
     * @param method
     */
    @SuppressWarnings("unchecked")
    public SootMethodAdapter(Method method, SootClass sootClass) {
        super();
        this.owner = sootClass;
        this.method = method;
        String[] argTypes = method.getType().getParameterTypes();
        soot.Type[] sootArgTypes = new soot.Type[argTypes.length];
        List<Annotation>[] paramAnns = new List[argTypes.length];
        for (int i = 0; i < paramAnns.length; i++) {
            paramAnns[i] = new ArrayList<Annotation>();
            sootArgTypes[i] = SootUtil.toSootType(argTypes[i]);
        }
        sootMethod = new SootMethod(method.getName(), Arrays.asList(sootArgTypes), SootUtil.toSootType(method
                .getType().getReturnType()));
        sootMethod.setModifiers(method.getAccessFlags());
        sootMethod.setDeclaringClass(owner);
        owner.addMethod(sootMethod);
  
        this.paramAnns = paramAnns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang .String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Annotation ann = new Annotation(name, visitable);
        anns.add(ann);
        return new SootAnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {
        return new SootCodeAdapter(method, sootMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    @SuppressWarnings("unchecked")
    public void visitEnd() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
     */
    public DexAnnotationAble visitParamesterAnnotation(int index) {
        final List<Annotation> panns = paramAnns[index];
        return new DexAnnotationAble() {
            public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
                Annotation ann = new Annotation(name, visitable);
                panns.add(ann);
                return new SootAnnAdapter(ann);
            }
        };
    }

}
