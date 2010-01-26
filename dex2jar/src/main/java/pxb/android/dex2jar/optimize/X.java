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
package pxb.android.dex2jar.optimize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
@Deprecated
@SuppressWarnings("unchecked")
public class X implements Opcodes {
	public static void transform(Method m, MethodNode method) {

		if (method.instructions.getFirst() != null) {
			List<LabelNode> labels = new ArrayList<LabelNode>();
			Set<LabelNode> frames = new HashSet<LabelNode>();

			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				if (p instanceof LabelNode) {
					labels.add((LabelNode) p);
				}
			}

			for (Iterator<TryCatchBlockNode> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
				TryCatchBlockNode tcbn = it.next();
				frames.add(tcbn.handler);
			}

			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				if (p instanceof JumpInsnNode) {
					if (p.getOpcode() == GOTO) {
						frames.add(((JumpInsnNode) p).label);
					}
				} else if (p instanceof LookupSwitchInsnNode) {
					LookupSwitchInsnNode lsi = (LookupSwitchInsnNode) p;
					frames.add(lsi.dflt);
					for (Iterator<LabelNode> it = lsi.labels.iterator(); it.hasNext();) {
						frames.add(it.next());
					}
				} else if (p instanceof TableSwitchInsnNode) {
					TableSwitchInsnNode tsi = (TableSwitchInsnNode) p;
					frames.add(tsi.dflt);
					for (Iterator<LabelNode> it = tsi.labels.iterator(); it.hasNext();) {
						frames.add(it.next());
					}
				}
			}
			AnalyzerAdapter a = new AnalyzerAdapter(m.getOwner(), method.access, method.name, method.desc, new EmptyVisitor());
			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				p.accept(a);
				if (p instanceof LabelNode) {
					if (frames.contains(p)) {
						String type = null;
						for (Iterator<TryCatchBlockNode> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
							TryCatchBlockNode tcbn = it.next();
							if (p.equals(tcbn.handler)) {
								type = tcbn.type;
								if (type == null) {
									type = "Ljava/lang/Throwable;";
								}
								break;
							}
						}
						if (type != null) {
							a.visitFrame(Opcodes.F_NEW, a.locals.size(), a.locals.toArray(), 1, new Object[] { type });
						} else {
							a.visitFrame(Opcodes.F_NEW, a.locals.size(), a.locals.toArray(new Object[0]), 0, null);
						}
					}

				}
			}
		}
	}
}
