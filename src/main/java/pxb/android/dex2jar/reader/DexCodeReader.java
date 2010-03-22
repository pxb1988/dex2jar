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
package pxb.android.dex2jar.reader;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.DexOpcodeDump;
import pxb.android.dex2jar.DexOpcodeUtil;
import pxb.android.dex2jar.DexOpcodes;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexCodeVisitor;

/**
 * 用于读取方法的指令
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id: DexCodeReader.java 90 2010-03-09 05:31:33Z pxb1988 $
 */
public class DexCodeReader implements DexOpcodes {
	private static final Logger log = LoggerFactory.getLogger(DexCodeReader.class);
	/**
	 * dex文件
	 */
	private Dex dex;
	/**
	 * 输入流
	 */
	private DataIn in;
	/**
	 * 标签映射,指令位置->指令编号
	 */
	private Map<Integer, Integer> labels = new HashMap<Integer, Integer>();
	/**
	 * 用于记录标签的编号，递增
	 */
	private int labels_index = 0;

	/**
	 * 方法的描述
	 */
	private Method method;

	/**
	 * @param dex
	 *            dex文件
	 * @param in
	 *            输入流
	 * @param method
	 *            方法的描述
	 */
	public DexCodeReader(Dex dex, DataIn in, Method method) {
		this.dex = dex;
		this.in = in;
		this.method = method;
	}

	/**
	 * 处理指令
	 * 
	 * @param dcv
	 */
	public void accept(DexCodeVisitor dcv) {
		DataIn in = this.in;
		DexOpcodeAdapter tadoa = new DexOpcodeAdapter(dex, dcv, this.labels);
		int total_registers_size = in.readShortx();
		int in_register_size = in.readShortx();
		// int outs_size =
		in.readShortx();
		int tries_size = in.readShortx();
		int debug_off = in.readIntx();
		int instruction_size = in.readIntx();
		// 处理方法的参数
		{
			int args[];
			int args_index;
			int i = total_registers_size - in_register_size;
			if ((method.getAccessFlags() & Opcodes.ACC_STATIC) == 0) {
				args = new int[method.getType().getParameterTypes().length + 1];
				args[0] = i++;
				args_index = 1;
			} else {
				args = new int[method.getType().getParameterTypes().length];
				args_index = 0;
			}
			for (String type : method.getType().getParameterTypes()) {
				args[args_index++] = i++;
				if ("D".equals(type) || "J".equals(type)) {// 为Double/Long型特殊处理
					i++;
				}
			}
			dcv.visitInitLocal(args);
		}

		// 处理异常处理
		if (tries_size > 0) {
			in.push();
			in.skip(instruction_size * 2);
			if (in.needPadding()) {
				in.skip(2);
			}
			for (int i = 0; i < tries_size; i++) {
				int start = in.readIntx();
				int offset = in.readShortx();
				int handlers = in.readShortx();
				in.push();
				in.skip((tries_size - i - 1) * 8 + handlers);
				boolean catchAll = false;
				int listSize = (int) in.readSignedLeb128();
				if (listSize <= 0) {
					listSize = -listSize;
					catchAll = true;
				}
				for (int k = 0; k < listSize; k++) {
					int type_id = (int) in.readUnsignedLeb128();
					int handler = (int) in.readUnsignedLeb128();
					order(start);
					order(start + offset);
					order(handler);
					String type = dex.getType(type_id);
					dcv.visitTryCatch(this.labels.get(start), this.labels.get(start + offset), this.labels.get(handler), type);
				}
				if (catchAll) {
					int handler = (int) in.readUnsignedLeb128();
					order(start);
					order(start + offset);
					order(handler);
					dcv.visitTryCatch(this.labels.get(start), this.labels.get(start + offset), this.labels.get(handler), null);

				}
				in.pop();
			}
			in.pop();
		}
		// 处理debug信息
		if (debug_off != 0) {
			// in.pushMove(debug_off);
			// new DexDebugInfoReader(in, dex,total_registers_size).accept(dcv);
			// in.pop();
		}
		// 查找标签
		in.push();
		for (int i = 0; i < instruction_size;) {
			int opcode = in.readByte() & 0xff;
			int size = DexOpcodeUtil.getSize(opcode);
			switch (size) {
			case 1: {
				int a = in.readByte();
				switch (opcode) {
				case OP_GOTO:
					order(i + ((byte) a));
					break;
				}
				i += 1;
				break;
			}
			case 2: {
				in.skip(1);
				short b = in.readShortx();
				switch (opcode) {
				case OP_GOTO_16:
				case OP_IF_EQZ:
				case OP_IF_NEZ:
				case OP_IF_LTZ:
				case OP_IF_GEZ:
				case OP_IF_GTZ:
				case OP_IF_LEZ:
				case OP_IF_EQ:
				case OP_IF_NE:
				case OP_IF_LT:
				case OP_IF_GE:
				case OP_IF_GT:
				case OP_IF_LE:
					order(i + b);
					break;
				}
				i += 2;
				break;
			}
			case 3: {
				in.skip(5);
				i += 3;
				break;
			}
			case 0:// OP_NOP
				i = instruction_size;
				break;
			case -1: {
				in.skip(1);
				int offset = in.readIntx();
				in.push();
				in.skip((offset - 3) * 2);
				switch (opcode) {
				case OP_SPARSE_SWITCH: {
					{
						in.readShortx();
						int switch_size = in.readShortx();
						for (int j = 0; j < switch_size; j++) {
							in.readIntx();
						}
						for (int j = 0; j < switch_size; j++) {
							order(i + in.readIntx());
						}
						order(i + 3);
					}
				}
					break;
				case OP_PACKED_SWITCH: {
					{
						in.skip(2);
						int switch_size = in.readShortx();
						in.skip(4);
						for (int j = 0; j < switch_size; j++) {
							int targetOffset = in.readIntx();
							order(i + targetOffset);
						}
						order(i + 3);
					}

				}
					break;
				case OP_FILL_ARRAY_DATA:
					break;
				}
				in.pop();
				i += 3;
			}
				break;
			case 5: {
				in.skip(9);
				i += 5;
			}
				break;
			}
		}
		in.pop();

		// 处理指令
		for (int i = 0; i < instruction_size;) {
			int opcode = in.readByte() & 0xff;
			if (labels.containsKey(i))
				dcv.visitLabel(labels.get(i));
			tadoa.visitOffset(i);
			int size = DexOpcodeUtil.getSize(opcode);
			switch (size) {
			case 1: {
				int a = in.readByte();
				if (log.isDebugEnabled()) {
					log.debug(String.format("%04x| %02x%02x           %s", i, opcode, a, DexOpcodeDump.dump(opcode)));
				}
				tadoa.visit(opcode, a);
				i += 1;
				break;
			}
			case 2: {
				int a = in.readByte();
				short b = in.readShortx();
				if (log.isDebugEnabled()) {
					log.debug(String.format("%04x| %02x%02x %04x      %s", i, opcode, a, Short.reverseBytes(b), DexOpcodeDump.dump(opcode)));
				}
				tadoa.visit(opcode, a, b);
				i += 2;
				break;
			}
			case 3: {
				int a = in.readByte();
				short b = in.readShortx();
				short c = in.readShortx();
				if (log.isDebugEnabled()) {
					log.debug(String.format("%04x| %02x%02x %04x %04x %s", i, opcode, a, Short.reverseBytes(b), Short.reverseBytes(c), DexOpcodeDump
							.dump(opcode)));
				}
				tadoa.visit(opcode, a, b, c);
				i += 3;
				break;
			}
			case 0:// OP_NOP
				i = instruction_size;
				break;
			case -1: {
				int reg = in.readByte();
				int offset = in.readIntx();
				in.push();
				in.skip((offset - 3) * 2);
				switch (opcode) {
				case OP_SPARSE_SWITCH: {
					{
						in.readShortx();
						int switch_size = in.readShortx();
						int cases[] = new int[switch_size];
						int label[] = new int[switch_size];
						for (int j = 0; j < switch_size; j++) {
							cases[j] = in.readIntx();
						}
						for (int j = 0; j < switch_size; j++) {
							label[j] = labels.get(i + in.readIntx());
						}
						dcv.visitLookupSwitchInsn(opcode, reg, this.labels.get(i + 3), cases, label);
					}
				}
					break;
				case OP_PACKED_SWITCH: {
					{
						in.readShortx();
						int switch_size = in.readShortx();
						int first_case = in.readIntx();
						int last_case = first_case - 1 + switch_size;
						int _labels[] = new int[switch_size];
						for (int j = 0; j < switch_size; j++) {
							int targetOffset = in.readIntx();
							_labels[j] = this.labels.get(i + targetOffset);
						}
						dcv.visitTableSwitchInsn(opcode, reg, first_case, last_case, this.labels.get(i + 3), _labels);
					}

				}
					break;
				case OP_FILL_ARRAY_DATA: {
					{
						in.readShortx();
						int elemWidth = in.readShortx();
						int initLength = in.readIntx();
						Object[] values = new Object[initLength];

						switch (elemWidth) {
						case 1:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readByte();
							}
							break;
						case 2:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readShortx();
							}
							break;
						case 4:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readIntx();
							}
							break;
						case 8:
							for (int j = 0; j < initLength; j++) {
								values[j] = in.readLongx();
							}
							break;
						}

						dcv.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
					}

				}
					break;
				}
				in.pop();
				i += 3;
			}
				break;
			case 5: {
				int reg = in.readByte();
				long value = in.readLongx();
				dcv.visitLdcInsn(opcode, value, reg);
				i += 5;
			}
				break;
			default:
				throw new RuntimeException(String.format("Not support Opcode :0x%02x=%s @[0x%04x]", opcode, DexOpcodeDump.dump(opcode), i));
			}
		}
		dcv.visitEnd();
	}

	/**
	 * 预定一个标签位置
	 * 
	 * @param offset
	 *            指令位置
	 */
	private void order(int offset) {
		if (!labels.containsKey(offset)) {
			labels.put(offset, this.labels_index++);
		}
	}
}
