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
package pxb.android.dex2jar.optimize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.LdcInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.MethodInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.AnalyzerException;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicValue;
import pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value;

/**
 * @author Panxiaobo
 * 
 */
public class DexInterpreter extends BasicInterpreter {

	public Map<Value, Set<AbstractInsnNode>> getM() {
		return m;
	}

	public class MayObject implements Value {
		@Override
		public String toString() {
			return this.type == null ? "X" : type.equals(Type.INT_TYPE) ? "Y" : "N";
		}

		public Type type;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.objectweb.asm.tree.analysis.Value#getSize()
		 */
		public int getSize() {
			return 1;
		}
	}

	@Override
	public Value binaryOperation(AbstractInsnNode insn, Value value1, Value value2) throws AnalyzerException {
		// TODO Auto-generated method stub
		return super.binaryOperation(insn, value1, value2);
	}

	@Override
	public Value copyOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
		if (value instanceof MayObject) {
			addTo(value, insn);
			if (insn.getOpcode() == ALOAD) {
				// it is a object
				((MayObject) value).type = Type.INT_TYPE;
				return value;
			}
		} else if (((BasicValue) value).isReference()) {
			C.replace(insn);
		}
		return super.copyOperation(insn, value);
	}

	@Override
	public Value merge(Value v, Value w) {
		if (v instanceof MayObject) {
			return v;
		}
		if (w instanceof MayObject)
			return w;
		return super.merge(v, w);
	}

	void setv(Type sure, Value may) {
		if (may instanceof MayObject) {
			MayObject mayObject = (MayObject) may;
			if (sure.equals(Type.INT_TYPE)) {
				mayObject.type = Type.INT_TYPE;
			} else {
				mayObject.type = sure;
			}
		}

	}

	@Override
	public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {

		if (insn.getOpcode() == MULTIANEWARRAY) {
			MultiANewArrayInsnNode mn = (MultiANewArrayInsnNode) insn;
		} else {
			MethodInsnNode m = ((MethodInsnNode) insn);
			Iterator<Value> it = values.iterator();

			if (m.getOpcode() != INVOKESTATIC) {
				it.hasNext();
				setv(Type.getType(m.owner), it.next());
			}
			Type ts[] = Type.getArgumentTypes(m.desc);
			for (Type t : ts) {
				it.hasNext();
				setv(t, it.next());
			}
		}
		return super.naryOperation(insn, values);
	}

	@Override
	public Value newValue(Type type) {
		// TODO Auto-generated method stub
		return super.newValue(type);
	}

	@Override
	public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
		// TODO Auto-generated method stub
		super.returnOperation(insn, value, expected);
	}

	@Override
	public Value ternaryOperation(AbstractInsnNode insn, Value value1, Value value2, Value value3) throws AnalyzerException {
		// value1 [value2] =value3

		if (value1 instanceof MayObject) {
			Type type = null;
			switch (insn.getOpcode()) {
			case IASTORE:
				type = Type.getType("[I");
				break;
			case LASTORE:
				type = Type.getType("[J");
				break;
			case FASTORE:
				type = Type.getType("[F");
				break;
			case DASTORE:
				type = Type.getType("[D");
				break;

			case BASTORE:
				type = Type.getType("[B");
				break;
			case CASTORE:
				type = Type.getType("[C");
				break;
			case SASTORE:
				type = Type.getType("[S");
				break;
			case AASTORE:
				if (value3 instanceof MayObject) {
					if (((MayObject) value3).type == null) {
						type = Type.getType("[Ljava/lang/Object;");// some type of array
					} else {
						type = Type.getType("[" + ((MayObject) value3).type.getDescriptor());
					}
				} else {
					type = Type.getType("[" + ((BasicValue) value3).getType().getDescriptor());
				}

				break;
			}

			((MayObject) value1).type = type;
		}

		if (value2 instanceof MayObject) {// 肯定是int类型
			((MayObject) value2).type = Type.INT_TYPE;
		}

		if (value3 instanceof MayObject) {
			Type type = null;
			switch (insn.getOpcode()) {
			case IASTORE:
				type = Type.INT_TYPE;
				break;
			case LASTORE:
				type = Type.LONG_TYPE;
				break;
			case FASTORE:
				type = Type.FLOAT_TYPE;
				break;
			case DASTORE:
				type = Type.DOUBLE_TYPE;
				break;

			case BASTORE:
				type = Type.BYTE_TYPE;
				break;
			case CASTORE:
				type = Type.CHAR_TYPE;
				break;
			case SASTORE:
				type = Type.SHORT_TYPE;
				break;
			case AASTORE:
				if (value1 instanceof MayObject) {
					if (((MayObject) value1).type == null) {
						type = Type.getType("Ljava/lang/Object;");// some type of object
					} else {
						type = ((MayObject) value1).type.getElementType();
					}
				} else {
					type = ((BasicValue) value1).getType().getElementType();
				}
				break;
			}

			((MayObject) value3).type = type;
		}

		return super.ternaryOperation(insn, value1, value2, value3);
	}

	Map<Value, Set<AbstractInsnNode>> m = new HashMap();

	@Override
	public Value unaryOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
		if (insn.getOpcode() == IFEQ || insn.getOpcode() == IFNE) {
			if (value instanceof MayObject) {
				addTo(value, insn);
			} else {
				if (((BasicValue) value).isReference()) {
					C.replace(insn);
				}
			}
		}
		return super.unaryOperation(insn, value);
	}

	void addTo(Value value, AbstractInsnNode insn) {
		Set<AbstractInsnNode> s = m.get(value);
		if (s == null) {
			s = new HashSet();
			m.put(value, s);
		}
		s.add(insn);
	}

	@Override
	public Value newOperation(AbstractInsnNode insn) throws AnalyzerException {
		if (insn.getOpcode() == LDC) {
			Object cst = ((LdcInsnNode) insn).cst;
			if (cst instanceof Integer && ((Integer) cst).intValue() == 0) {
				Value v = new MayObject();
				addTo(v, insn);
				return v;
			}
		}
		return super.newOperation(insn);
	}

}
