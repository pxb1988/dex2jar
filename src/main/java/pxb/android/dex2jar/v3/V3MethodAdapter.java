/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.optimize.A;
import pxb.android.dex2jar.optimize.B;
import pxb.android.dex2jar.optimize.C;
import pxb.android.dex2jar.optimize.LdcOptimizeAdapter;
import pxb.android.dex2jar.optimize.MethodTransformer;
import pxb.android.dex2jar.org.objectweb.asm.tree.MethodNode;
import pxb.android.dex2jar.v3.Ann.Item;
import pxb.android.dex2jar.visitors.DexAnnotationAble;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class V3MethodAdapter implements DexMethodVisitor, Opcodes {
	protected List<Ann> anns = new ArrayList<Ann>();
	protected boolean build = false;
	protected ClassVisitor cv;
	protected Method method;
	protected MethodVisitor mv;
	protected List<Ann>[] paramAnns;
	protected MethodNode methodNode;

	/**
	 * @param cv
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	public V3MethodAdapter(ClassVisitor cv, Method method) {
		super();
		this.cv = cv;
		this.method = method;
		List<Ann>[] paramAnns = new List[method.getType().getParameterTypes().length];
		for (int i = 0; i < paramAnns.length; i++) {
			paramAnns[i] = new ArrayList<Ann>();
		}
		this.paramAnns = paramAnns;
	}

	protected void build() {
		if (!build) {
			String[] exceptions = null;
			for (Iterator<Ann> it = anns.iterator(); it.hasNext();) {
				Ann ann = it.next();
				if ("Ldalvik/annotation/Throws;".equals(ann.type)) {
					it.remove();
					for (Item item : ann.items) {
						if (item.name.equals("value")) {
							Ann values = (Ann) item.value;
							exceptions = new String[values.items.size()];
							int count = 0;
							for (Item i : values.items) {
								exceptions[count++] = i.value.toString();
							}
						}
					}
				}
			}
			MethodVisitor mv = cv.visitMethod(method.getAccessFlags(), method.getName(), method.getType().getDesc(), null, exceptions);
			if (mv != null) {
				methodNode = new MethodNode(method.getAccessFlags(), method.getName(), method.getType().getDesc(), null, exceptions);
				for (Ann ann : anns) {
					AnnotationVisitor av = mv.visitAnnotation(ann.type, ann.visible == 1);
					V3AnnAdapter.accept(ann.items, av);
					av.visitEnd();
				}
				for (int i = 0; i < paramAnns.length; i++) {
					for (Ann ann : paramAnns[i]) {
						AnnotationVisitor av = mv.visitParameterAnnotation(i, ann.type, ann.visible == 1);
						V3AnnAdapter.accept(ann.items, av);
						av.visitEnd();
					}
				}
			}
			this.mv = mv;
			build = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		Ann ann = new Ann(name, visitable);
		anns.add(ann);
		return new V3AnnAdapter(ann);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
	 */
	public DexCodeVisitor visitCode() {
		build();
		if (mv == null)
			return null;
		return new V3CodeAdapter(method, methodNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitEnd()
	 */
	public void visitEnd() {
		build();
		if (mv != null) {
			if (methodNode.instructions.size() > 2) {
				List<? extends MethodTransformer> trs = Arrays.asList(new A(), new B(method), new C(method));
				for (MethodTransformer tr : trs) {
					tr.transform(methodNode);
				}
			}
		}
		methodNode.accept(new LocalVariablesSorter(method.getAccessFlags(), method.getType().getDesc(), new LdcOptimizeAdapter(mv)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
	 */
	public DexAnnotationAble visitParamesterAnnotation(int index) {
		final List<Ann> panns = paramAnns[index];
		return new DexAnnotationAble() {
			public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
				Ann ann = new Ann(name, visitable);
				panns.add(ann);
				return new V3AnnAdapter(ann);
			}
		};
	}

}
