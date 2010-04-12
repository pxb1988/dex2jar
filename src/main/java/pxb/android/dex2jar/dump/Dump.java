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
package pxb.android.dex2jar.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.ProxyOutputStream;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.reader.DexFileReader;
import pxb.android.dex2jar.visitors.DexClassAdapter;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;
import pxb.android.dex2jar.visitors.DexMethodAdapter;
import pxb.android.dex2jar.visitors.DexMethodVisitor;
import pxb.android.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class Dump implements DexFileVisitor {
	private int class_count = 0;
	private DexFileVisitor dfv;
	private PrintWriter out;

	public interface WriterManager {
		PrintWriter get(String name);
	}

	public static String getAccDes(int acc) {
		StringBuilder sb = new StringBuilder();
		if ((acc & Opcodes.ACC_PUBLIC) != 0) {
			sb.append("public ");
		}
		if ((acc & Opcodes.ACC_PROTECTED) != 0) {
			sb.append("protected ");
		}
		if ((acc & Opcodes.ACC_PRIVATE) != 0) {
			sb.append("private ");
		}
		if ((acc & Opcodes.ACC_STATIC) != 0) {
			sb.append("static ");
		}
		if ((acc & Opcodes.ACC_ABSTRACT) != 0 && (acc & Opcodes.ACC_INTERFACE) == 0) {
			sb.append("abstract ");
		}
		if ((acc & Opcodes.ACC_ANNOTATION) != 0) {
			sb.append("annotation ");
		}
		if ((acc & Opcodes.ACC_BRIDGE) != 0) {
			sb.append("bridge ");
		}
		if ((acc & Opcodes.ACC_DEPRECATED) != 0) {
			sb.append("deprecated ");
		}
		if ((acc & Opcodes.ACC_ENUM) != 0) {
			sb.append("enum ");
		}
		if ((acc & Opcodes.ACC_FINAL) != 0) {
			sb.append("final ");
		}
		if ((acc & Opcodes.ACC_INTERFACE) != 0) {
			sb.append("interace ");
		}
		if ((acc & Opcodes.ACC_NATIVE) != 0) {
			sb.append("native ");
		}
		if ((acc & Opcodes.ACC_STRICT) != 0) {
			sb.append("strict ");
		}
		if ((acc & Opcodes.ACC_SYNCHRONIZED) != 0) {
			sb.append("synchronized ");
		}
		if ((acc & Opcodes.ACC_TRANSIENT) != 0) {
			sb.append("transient ");
		}
		if ((acc & Opcodes.ACC_VARARGS) != 0) {
			sb.append("varargs ");
		}
		if ((acc & Opcodes.ACC_VOLATILE) != 0) {
			sb.append("volatile ");
		}
		return sb.toString();
	}

	/**
	 * @param dfv
	 */
	public Dump(DexFileVisitor dfv, WriterManager writerManager) {
		super();
		this.dfv = dfv;
		this.writerManager = writerManager;
	}

	WriterManager writerManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {

		String javaClassName = Type.getType(className).getClassName();
		out = writerManager.get(javaClassName);
		out.printf("//class:%04d  access:0x%04x\n", class_count++, access_flags);
		out.print(getAccDes(access_flags));
		if ((access_flags & Opcodes.ACC_INTERFACE) == 0) {
			out.print("class ");
		}
		out.print(javaClassName);

		if (!"Ljava/lang/Object;".equals(superClass)) {
			out.print(" extends ");
			out.print(Type.getType(superClass).getClassName());
		}
		if (interfaceNames != null && interfaceNames.length > 0) {
			out.print(" implements ");
			out.print(Type.getType(interfaceNames[0]).getClassName());
			for (int i = 1; i < interfaceNames.length; i++) {
				out.print(',');
				out.print(Type.getType(interfaceNames[i]).getClassName());
			}
		}
		out.println();
		DexClassVisitor dcv = dfv.visit(access_flags, className, superClass, interfaceNames);
		if (dcv == null)
			return null;
		return new DexClassAdapter(dcv) {

			public DexFieldVisitor visitField(Field field, Object value) {
				out.printf("//field:%04d  access:0x%04x\n", field_count++, field.getAccessFlags());
				out.printf("//%s\n", field);
				out.printf("%s %s %s", getAccDes(field.getAccessFlags()), Type.getType(field.getType()).getClassName(), field.getName());
				if (value != null) {
					out.print('=');
					out.print(value);
				}
				out.println(';');

				return dcv.visitField(field, value);
			}

			@Override
			public void visitEnd() {
				out.flush();
				out.close();
				out = null;
				super.visitEnd();
			}

			int method_count = 0;
			int field_count = 0;

			public DexMethodVisitor visitMethod(final Method method) {
				out.println();
				out.printf("//method:%04d  access:0x%04x\n", method_count++, method.getAccessFlags());
				out.printf("//%s\n", method);

				out.printf("%s%s %s(", getAccDes(method.getAccessFlags()), Type.getType(method.getType().getReturnType()).getClassName(), method.getName());
				String ps[] = method.getType().getParameterTypes();
				if (ps != null && ps.length > 0) {
					out.print(Type.getType(ps[0]).getClassName());
					for (int i = 1; i < ps.length; i++) {
						out.print(',');
						out.print(Type.getType(ps[i]).getClassName());
					}
				}
				out.println(')');

				DexMethodVisitor dmv = dcv.visitMethod(method);
				if (dmv == null) {
					return null;
				}
				return new DexMethodAdapter(dmv) {
					public DexCodeVisitor visitCode() {
						DexCodeVisitor dcv = mv.visitCode();
						if (dcv == null)
							return null;
						return new DumpDexCodeAdapter(dcv, method, out);
					}
				};
			}
		};
	}

	public static void main(String... args) throws IOException {
		if (args.length < 2) {
			System.out.println("Dump in.dex out.dump.jar");
			return;
		}
		doFile(new File(args[0]), new File(args[1]));
	}

	public static void doFile(File srcDex) throws IOException {
		doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + ".dump.jar"));
	}

	public static void doFile(File srcDex, File destJar) throws IOException {
		final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destJar)));
		new DexFileReader(srcDex).accept(new Dump(new EmptyVisitor(), new WriterManager() {

			public PrintWriter get(String name) {
				try {
					String s = name.replace('.', '/') + ".dump.txt";
					ZipEntry zipEntry = new ZipEntry(s);
					zos.putNextEntry(zipEntry);
					return new PrintWriter(new ProxyOutputStream(zos) {
						@Override
						public void close() throws IOException {
							zos.closeEntry();
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}));
		zos.finish();
		zos.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
		dfv.visitEnd();
	}

}
