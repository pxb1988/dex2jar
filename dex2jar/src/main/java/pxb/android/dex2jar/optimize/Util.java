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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class Util implements Opcodes {
	public static boolean needBreak(AbstractInsnNode ins) {
		switch (ins.getType()) {
		case AbstractInsnNode.JUMP_INSN:
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
		case AbstractInsnNode.TABLESWITCH_INSN:
		case AbstractInsnNode.LABEL:
			return true;
		}
		return false;
	}

	public static int var(AbstractInsnNode p) {
		return ((VarInsnNode) p).var;
	}

	public static void var(AbstractInsnNode p, int r) {
		((VarInsnNode) p).var = r;
	}

	public static boolean isWrite(AbstractInsnNode p) {
		if (p instanceof VarInsnNode) {
			VarInsnNode q = (VarInsnNode) p;
			switch (q.getOpcode()) {
			case ISTORE:
			case LSTORE:
			case DSTORE:
			case FSTORE:
			case ASTORE:
				return true;
			}
		}
		return false;
	}

	public static boolean isSameVar(AbstractInsnNode p, AbstractInsnNode q) {
		return ((VarInsnNode) p).var == ((VarInsnNode) q).var;
	}

	public static boolean isRead(AbstractInsnNode p) {
		if (p instanceof VarInsnNode) {
			VarInsnNode q = (VarInsnNode) p;
			switch (q.getOpcode()) {
			case ILOAD:
			case DLOAD:
			case LLOAD:
			case FLOAD:
			case ALOAD:
				return true;
			}
		}
		return false;
	}

	public static boolean isEnd(AbstractInsnNode p) {
		switch (p.getOpcode()) {
		case ATHROW:
		case RETURN:
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
			return true;
		}
		return false;
	}
}
