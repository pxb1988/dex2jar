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

import java.util.Map;

import org.objectweb.asm.ClassVisitor;

import pxb.android.dex2jar.ClassVisitorFactory;
import pxb.android.dex2jar.asm.TypeNameAdapter;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id: V3.java 90 2010-03-09 05:31:33Z pxb1988 $
 */
public class V3 implements DexFileVisitor {
	protected ClassVisitorFactory cvf;
	Map<String, Integer> accessFlagsMap;

	/**
	 * @param accessFlagsMap
	 * @param classVisitorFactory
	 */
	public V3(Map<String, Integer> accessFlagsMap, ClassVisitorFactory classVisitorFactory) {
		this.accessFlagsMap = accessFlagsMap;
		this.cvf = classVisitorFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
		final ClassVisitor cv = cvf.create(TypeNameAdapter.x(className));
		if (cv == null)
			return null;
		return new V3ClassAdapter(accessFlagsMap, cv, access_flags, className, superClass, interfaceNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

}
