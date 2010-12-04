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

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

import pxb.android.dex2jar.optimize.c.CBasicInterpreter;
import pxb.android.dex2jar.optimize.c.CBasicValue;

/**
 * @author Panxiaobo
 * 
 */
public class DexInterpreter extends CBasicInterpreter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * newValue(org.objectweb.asm.Type)
	 */
	@Override
	public Value newValue(Type type) {
		// if (type == Type.INT_TYPE || type == Type.LONG_TYPE) {
		// return new BasicValue(null);
		// }
		return super.newValue(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * binaryOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public Value binaryOperation(AbstractInsnNode insn, Value value1, Value value2) throws AnalyzerException {
		switch (insn.getOpcode()) {
		case PUTFIELD:
			FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
			if (((CBasicValue) value1).getType() == null) {
				((CBasicValue) value1).setType(Type.getType(fieldInsnNode.owner));
			}
			if (((CBasicValue) value2).getType() == null) {
				((CBasicValue) value2).setType(Type.getType(fieldInsnNode.desc));
			}
			break;
		// case IALOAD:
		// case BALOAD:
		// case CALOAD:
		// case SALOAD:
		case IADD:
		case ISUB:
		case IMUL:
		case IDIV:
		case IREM:
		case ISHL:
		case ISHR:
		case IUSHR:
		case IAND:
		case IOR:
		case IXOR:
			if (((CBasicValue) value1).getType() == null) {
				((CBasicValue) value1).setType(Type.INT_TYPE);
			}
			if (((CBasicValue) value2).getType() == null) {
				((CBasicValue) value2).setType(Type.INT_TYPE);
			}
			break;
		case FALOAD:
		case FADD:
		case FSUB:
		case FMUL:
		case FDIV:
		case FREM:
		case FCMPL:
		case FCMPG:
			if (((CBasicValue) value1).getType() == null) {
				((CBasicValue) value1).setType(Type.FLOAT_TYPE);
			}
			if (((CBasicValue) value2).getType() == null) {
				((CBasicValue) value2).setType(Type.FLOAT_TYPE);
			}
			break;
		case LALOAD:
		case LADD:
		case LSUB:
		case LMUL:
		case LDIV:
		case LREM:
		case LSHL:
		case LSHR:
		case LUSHR:
		case LAND:
		case LOR:
		case LXOR:
		case LCMP:
			if (((CBasicValue) value1).getType() == null) {
				((CBasicValue) value1).setType(Type.LONG_TYPE);
			}
			if (((CBasicValue) value2).getType() == null) {
				((CBasicValue) value2).setType(Type.LONG_TYPE);
			}
			break;
		case DALOAD:
		case DADD:
		case DSUB:
		case DMUL:
		case DDIV:
		case DREM:
		case DCMPL:
		case DCMPG:
			if (((CBasicValue) value1).getType() == null) {
				((CBasicValue) value1).setType(Type.DOUBLE_TYPE);
			}
			if (((CBasicValue) value2).getType() == null) {
				((CBasicValue) value2).setType(Type.DOUBLE_TYPE);
			}
			break;
		// case AALOAD:
		// return BasicValue.REFERENCE_VALUE;
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
			if (((CBasicValue) value1).getType() == null && ((CBasicValue) value2).getType() != null) {
				((CBasicValue) value1).setType(((CBasicValue) value2).getType());
			}
			if (((CBasicValue) value2).getType() == null && ((CBasicValue) value1).getType() != null) {
				((CBasicValue) value2).setType(((CBasicValue) value1).getType());
			}
			break;
		case IF_ACMPEQ:
		case IF_ACMPNE:
		}

		return super.binaryOperation(insn, value1, value2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * copyOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public Value copyOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {

//		if(Util.isRead(insn)&& value ==null){
//			System.out.println();
//		}
		
		switch (insn.getOpcode()) {
		case FLOAD:
		case FSTORE:
			if (((CBasicValue) value).getType() == null) {
				((CBasicValue) value).setType(Type.FLOAT_TYPE);
				break;
			}
		case DLOAD:
		case DSTORE:
			if (((CBasicValue) value).getType() == null) {
				((CBasicValue) value).setType(Type.DOUBLE_TYPE);
				break;
			}
		}

		return super.copyOperation(insn, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * ternaryOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public Value ternaryOperation(AbstractInsnNode insn, Value value1, Value value2, Value value3) throws AnalyzerException {
		// TODO Auto-generated method stub
		return super.ternaryOperation(insn, value1, value2, value3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * newOperation(pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode)
	 */
	@Override
	public Value newOperation(AbstractInsnNode insn) throws AnalyzerException {
		switch (insn.getOpcode()) {
		case ACONST_NULL:
			return newValue(Type.getObjectType("null"));
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			return new CBasicValue(null);
		case LCONST_0:
		case LCONST_1:
			return new CBasicValue(null);
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			return CBasicValue.FLOAT_VALUE;
		case DCONST_0:
		case DCONST_1:
			return CBasicValue.DOUBLE_VALUE;
		case BIPUSH:
		case SIPUSH:
			return new CBasicValue(null);
		case LDC:
			Object cst = ((LdcInsnNode) insn).cst;
			if (cst instanceof Integer) {
				return new CBasicValue(null);
			} else if (cst instanceof Float) {
				return CBasicValue.FLOAT_VALUE;
			} else if (cst instanceof Long) {
				return new CBasicValue(null);
			} else if (cst instanceof Double) {
				return CBasicValue.DOUBLE_VALUE;
			} else if (cst instanceof Type) {
				return newValue(Type.getObjectType("java/lang/Class"));
			} else {
				return newValue(Type.getType(cst.getClass()));
			}
		case JSR:
			return CBasicValue.RETURNADDRESS_VALUE;
		case GETSTATIC:
			return newValue(Type.getType(((FieldInsnNode) insn).desc));
		case NEW:
			return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
		default:
			throw new Error("Internal error.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * unaryOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public Value unaryOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
		if (((CBasicValue) value).getType() == null) {
			switch (insn.getOpcode()) {
			case INEG:
			case IINC:
			case I2D:
			case I2L:
			case I2F:
			case I2B:
			case I2C:
			case I2S:
			case IRETURN:
			case NEWARRAY:
			case ANEWARRAY:
			case TABLESWITCH:
			case LOOKUPSWITCH:
				((CBasicValue) value).setType(Type.INT_TYPE);
				break;
			case L2D:
			case L2F:
			case L2I:
			case LRETURN:
			case LNEG:
				((CBasicValue) value).setType(Type.LONG_TYPE);
				break;

			case DNEG:
			case D2F:
			case D2L:
			case D2I:
			case DRETURN:
				((CBasicValue) value).setType(Type.DOUBLE_TYPE);
				break;
			case FNEG:
			case F2D:
			case F2L:
			case F2I:
			case FRETURN:
				((CBasicValue) value).setType(Type.FLOAT_TYPE);
				break;
			case PUTSTATIC:
				((CBasicValue) value).setType(Type.getType(((FieldInsnNode) insn).desc));
				break;
			case GETFIELD:
				((CBasicValue) value).setType(Type.getType(((FieldInsnNode) insn).owner));
				break;
			}
		}
		if (insn.getOpcode() == GETFIELD) {
			return new CBasicValue(Type.getType(((FieldInsnNode) insn).desc));
		}
		return super.unaryOperation(insn, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * merge(pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public Value merge(Value v, Value w) {

		if (!(w == null || v == null)) {
			if (((CBasicValue) v).getType() == null ^ ((CBasicValue) w).getType() == null) {
				if (((CBasicValue) v).getType() == null) {
					((CBasicValue) v).setType(((CBasicValue) w).getType());
				}
				if (((CBasicValue) w).getType() == null) {
					((CBasicValue) w).setType(((CBasicValue) v).getType());
				}
			}
		}
		return super.merge(v, w);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * naryOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * java.util.List)
	 */
	@Override
	public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {
		switch (insn.getOpcode()) {
		case INVOKESTATIC: {
			Type[] args = Type.getArgumentTypes(((MethodInsnNode) insn).desc);
			for (int i = 0; i < args.length; i++) {
				Value v = (Value) values.get(i);
				if (((CBasicValue) v).getType() == null) {
					((CBasicValue) v).setType(args[i]);
				}
			}
			break;
		}
		case INVOKEDYNAMIC:
		case INVOKEINTERFACE:
		case INVOKESPECIAL:
		case INVOKEVIRTUAL: {
			Type[] args = Type.getArgumentTypes(((MethodInsnNode) insn).desc);
			Value v = (Value) values.get(0);
			if (((CBasicValue) v).getType() == null) {
				((CBasicValue) v).setType(Type.getType(((MethodInsnNode) insn).owner));
			}
			for (int i = 0; i < args.length; i++) {
				v = (Value) values.get(i + 1);
				if (((CBasicValue) v).getType() == null) {
					((CBasicValue) v).setType(args[i]);
				}
			}
			break;
		}
		}

		if (insn.getOpcode() == MULTIANEWARRAY) {
			return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
		} else {
			return new CBasicValue(Type.getReturnType(((MethodInsnNode) insn).desc));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.BasicInterpreter#
	 * returnOperation
	 * (pxb.android.dex2jar.org.objectweb.asm.tree.AbstractInsnNode,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value,
	 * pxb.android.dex2jar.org.objectweb.asm.tree.analysis.Value)
	 */
	@Override
	public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
		if (((CBasicValue) value).getType() == null) {
			((CBasicValue) value).setType(((CBasicValue) expected).getType());
		}

		super.returnOperation(insn, value, expected);
	}

}
