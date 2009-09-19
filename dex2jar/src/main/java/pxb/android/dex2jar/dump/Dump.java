/**
 * 
 */
package pxb.android.dex2jar.dump;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.Opcodes;

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
 * 
 */
public class Dump implements DexFileVisitor {
	private static final Logger log = LoggerFactory.getLogger(Dump.class);
	int class_count = 0;
	DexFileVisitor dfv;

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
		if ((acc & Opcodes.ACC_ABSTRACT) != 0) {
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
	public Dump(DexFileVisitor dfv) {
		super();
		this.dfv = dfv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
		log.info("");
		log.info("");
		log.info(String.format("%-20s:%s", "class:", class_count++));
		log.info(String.format("%-20s:0x%04x    //%s", "access", access_flags, getAccDes(access_flags)));
		log.info(String.format("%-20s:%s", "class", Type.getType(className).getClassName()));
		log.info(String.format("%-20s:%s", "super", Type.getType(superClass).getClassName()));
		if (interfaceNames == null) {
			interfaceNames = new String[0];
		}
		log.info(String.format("%-20s:%-2d", "interfaces", interfaceNames.length));
		for (int i = 0; i < interfaceNames.length; i++)
			log.info(String.format("%-20s:[%2d]%s", "", i, Type.getType(interfaceNames[i]).getClassName()));

		DexClassVisitor dcv = dfv.visit(access_flags, className, superClass, interfaceNames);
		if (dcv == null)
			return null;
		return new DexClassAdapter(dcv) {

			public DexFieldVisitor visitField(Field field, Object value) {
				log.info("");
				log.info(String.format("%20s:%d", "field", field_count++));
				log.info(String.format("%20s:0x%04x   //%s", "access_flags", field.getAccessFlags(), getAccDes(field
						.getAccessFlags())));
				log.info(String.format("%20s:%s", "name", field.getName()));
				log.info(String.format("%20s:%s", "type", Type.getType(field.getType()).getClassName()));
				log.info(String.format("%20s:%s", "", field));
				if (value != null)
					log.info(String.format("%20s:%s", "value", value));
				return dcv.visitField(field, value);
			}

			int method_count = 0;
			int field_count = 0;

			public DexMethodVisitor visitMethod(final Method method) {
				log.info("");
				log.info(String.format("%20s:%d", "method", method_count++));
				log.info(String.format("%20s:0x%04x   //%s", "access_flags", method.getAccessFlags(), getAccDes(method
						.getAccessFlags())));
				log.info(String.format("%20s:%s", "name", method.getName()));
				log.info(String.format("%20s:%s", "describe", method.getType().toString()));
				log.info(String.format("%20s:%s", "return", Type.getType(method.getType().getReturnType())
						.getClassName()));
				log.info(String.format("%20s:%s", "", method));
				log.info(String.format("%20s:%d", "args", method.getType().getParameterTypes().length));
				for (int i = 0; i < method.getType().getParameterTypes().length; i++) {
					log.info(String.format("%20s:[%2d]%s", "", i, Type.getType(method.getType().getParameterTypes()[i])
							.getClassName()));
				}
				DexMethodVisitor dmv = dcv.visitMethod(method);
				if (dmv == null) {
					return null;
				}
				return new DexMethodAdapter(dmv) {
					public DexCodeVisitor visitCode() {
						DexCodeVisitor dcv = mv.visitCode();
						if (dcv == null)
							return null;
						return new DumpDexCodeAdapter(dcv, method);
					}
				};
			}
		};
	}

	public static void main(String... args) throws IOException {
		for (String s : args) {
			new DexFileReader(new File(s)).accept(new Dump(new EmptyVisitor()));
		}
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
