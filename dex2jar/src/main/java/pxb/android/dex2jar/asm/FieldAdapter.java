/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.dex2jar.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class FieldAdapter implements FieldVisitor {
	protected FieldVisitor fv;

	/**
	 * @param fv
	 */
	public FieldAdapter(FieldVisitor fv) {
		super();
		this.fv = fv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String,
	 * boolean)
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return fv.visitAnnotation(desc, visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.asm.FieldVisitor#visitAttribute(org.objectweb.asm.Attribute )
	 */
	public void visitAttribute(Attribute attr) {
		fv.visitAttribute(attr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.FieldVisitor#visitEnd()
	 */
	public void visitEnd() {
		fv.visitEnd();
	}

}
