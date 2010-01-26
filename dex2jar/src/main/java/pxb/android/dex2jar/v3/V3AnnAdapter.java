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
package pxb.android.dex2jar.v3;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.v3.Ann.Item;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class V3AnnAdapter implements DexAnnotationVisitor {

	Ann ann;
	private static final Logger logger = LoggerFactory.getLogger(V3AnnAdapter.class);

	public static void accept(List<Item> items, AnnotationVisitor av) {
		if (av == null)
			return;
		for (Item item : items) {
			Object v = item.value;
			if (v instanceof Ann) {
				Ann a = (Ann) v;
				if (a.type != null) {
					AnnotationVisitor av1 = av.visitAnnotation(item.name, a.type);
					accept(a.items, av1);
					av1.visitEnd();
				} else {// array
					AnnotationVisitor av1 = av.visitArray(item.name);
					accept(a.items, av1);
					av1.visitEnd();
				}
			} else if (v instanceof Field) {
				Field e = (Field) v;
				av.visitEnum(item.name, e.getType(), e.getName());
			} else if (v instanceof Method) {
				logger.warn("Find Method Annotation:{}", v);
			} else {
				av.visit(item.name, v);
			}
		}
	}

	/**
	 * @param ann
	 */
	public V3AnnAdapter(Ann ann) {
		super();
		this.ann = ann;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String,
	 * java.lang.Object)
	 */
	public void visit(String name, Object value) {
		ann.items.add(new Item(name, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java
	 * .lang.String, java.lang.String)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, String desc) {
		Ann ann = new Ann(desc, -1);
		this.ann.items.add(new Item(name, ann));
		return new V3AnnAdapter(ann);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang
	 * .String)
	 */
	public DexAnnotationVisitor visitArray(String name) {
		Ann ann = new Ann(null, -1);
		this.ann.items.add(new Item(name, ann));
		return new V3AnnAdapter(ann);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void visitEnum(String name, String desc, String value) {
	}

}
