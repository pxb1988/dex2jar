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

import static pxb.android.dex2jar.reader.Constant.x0246;
import static pxb.android.dex2jar.reader.Constant.x3;
import static pxb.android.dex2jar.reader.Constant.xf;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;
import pxb.android.dex2jar.visitors.DexAnnotationAble;

/**
 * 读取注解
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class DexAnnotationReader {
	private Dex dex;
	private static final int VALUE_BYTE = 0;
	private static final int VALUE_SHORT = 2;
	private static final int VALUE_CHAR = 3;
	private static final int VALUE_INT = 4;
	private static final int VALUE_LONG = 6;
	private static final int VALUE_FLOAT = 16;
	private static final int VALUE_DOUBLE = 17;
	private static final int VALUE_STRING = 23;
	private static final int VALUE_TYPE = 24;
	private static final int VALUE_FIELD = 25;
	private static final int VALUE_METHOD = 26;
	private static final int VALUE_ENUM = 27;
	private static final int VALUE_ARRAY = 28;
	private static final int VALUE_ANNOTATION = 29;
	private static final int VALUE_NULL = 30;
	private static final int VALUE_BOOLEAN = 31;

	/**
	 * @param dex
	 *          dex文件
	 */
	public DexAnnotationReader(Dex dex) {
		super();
		this.dex = dex;
	}

	/**
	 * 处理
	 * 
	 * @param in
	 *          输入流
	 * @param daa
	 */
	public void accept(DataIn in, DexAnnotationAble daa) {
		int size = in.readIntx();
		for (int j = 0; j < size; j++) {
			int field_annotation_offset = in.readIntx();
			in.pushMove(field_annotation_offset);
			int visible_i = in.readByte();
			int type_idx = (int) in.readUnsignedLeb128();
			String type = dex.getType(type_idx);
			AnnotationVisitor dav = daa.visitAnnotation(type, visible_i == 1);
			if (dav != null) {
				int sizex = (int) in.readUnsignedLeb128();
				for (int k = 0; k < sizex; k++) {
					int name_idx = (int) in.readUnsignedLeb128();
					String name = dex.getString(name_idx);
					acceptAnnotation(dex, in, name, dav);
				}
				dav.visitEnd();
			}
			in.pop();
		}
	}

	/**
	 * 
	 * @param dex
	 * @param in
	 * @param name
	 * @param dav
	 */
	private static void acceptAnnotation(Dex dex, DataIn in, String name, AnnotationVisitor dav) {
		int b = in.readByte();
		int type = b & 0x1f;
		Object value = null;
		switch (type) {
		case VALUE_BYTE:
			value = new Byte((byte) x0246(in, b));
			break;
		case VALUE_SHORT:
			value = new Short((short) x0246(in, b));
			break;
		case VALUE_INT:
			value = new Integer((int) x0246(in, b));
			break;
		case VALUE_LONG:
			value = new Long(x0246(in, b));
			break;
		case VALUE_CHAR:
			value = new Character((char) x3(in, b));
			break;
		case VALUE_STRING:
			value = dex.getString((int) x3(in, b));
			break;
		case VALUE_FLOAT:
			value = Float.intBitsToFloat((int) (xf(in, b) >> 32));
			break;
		case VALUE_DOUBLE:
			value = Double.longBitsToDouble(xf(in, b));
			break;
		case VALUE_NULL:
			value = Type.VOID_TYPE;// null
			break;
		case VALUE_BOOLEAN: {
			value = new Boolean(((b >> 5) & 0x3) != 0);
			break;
		}
		case VALUE_TYPE: {
			int type_id = (int) x3(in, b);
			value = Type.getType(dex.getType(type_id));
			break;
		}
		case VALUE_ENUM: {
			value = dex.getField((int) x3(in, b));
		}
			break;
		case VALUE_METHOD: {
			int method_id = (int) x3(in, b);
			value = dex.getMethod(method_id);
			break;
		}
		case VALUE_FIELD: {
			int field_id = (int) x3(in, b);
			value = dex.getField(field_id);
			break;
		}
		}

		if (value != null) {
			if (dav != null)
				dav.visit(name, value);
			return;
		}
		switch (type) {
		case VALUE_ARRAY: {
			int size = in.readByte();
			AnnotationVisitor _dav = null;
			if (dav != null) {
				_dav = dav.visitArray(name);
			}
			for (int i = 0; i < size; i++) {
				acceptAnnotation(dex, in, null, _dav);
			}
			if (_dav != null)
				_dav.visitEnd();
			return;
		}
		case VALUE_ANNOTATION: {
			int _type = (int) in.readUnsignedLeb128();
			String _typeString = dex.getType(_type);
			AnnotationVisitor _dav = dav.visitAnnotation(name, _typeString);
			int size = (int) in.readUnsignedLeb128();
			for (int i = 0; i < size; i++) {
				int nameid = (int) in.readUnsignedLeb128();
				acceptAnnotation(dex, in, dex.getString(nameid), _dav);
			}
		}
			break;

		default:
			throw new RuntimeException("Not support yet.");
		}
	}

}
