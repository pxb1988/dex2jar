/**
 * 
 */
package pxb.android.dex2jar.dump;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexClassAdapter;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;
import pxb.android.dex2jar.visitors.DexMethodAdapter;
import pxb.android.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Dump implements DexFileVisitor {
	private static final Logger log = LoggerFactory.getLogger(Dump.class);
	int class_count = 0;
	DexFileVisitor dfv;

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
		log.info(String.format("%-20s:0x%08x", "access", access_flags));
		log.info(String.format("%-20s:%s", "class", Type.getType(className).getClassName()));
		log.info(String.format("%-20s:%s", "super", Type.getType(superClass).getClassName()));
		if (interfaceNames == null) {
			interfaceNames = new String[0];
		}
		log.info(String.format("%-20s:%-2d", "interfaces", interfaceNames.length));
		for (int i = 0; i < interfaceNames.length; i++)
			log.info(String.format("%-20s:[%2d]%s", "", i, Type.getType(interfaceNames[i]).getClassName()));

		DexClassVisitor dcv = dfv.visit(access_flags, className, superClass, interfaceNames);
		if (dfv == null)
			return null;
		return new DexClassAdapter(dcv) {

			public DexFieldVisitor visitField(Field field, Object value) {
				log.info("");
				log.info(String.format("%20s:%d", "field", field_count++));
				log.info(String.format("%20s:0x%08x", "access_flags", field.getAccessFlags()));
				log.info(String.format("%20s:%s", "name", field.getName()));
				log.info(String.format("%20s:%s", "type", Type.getType(field.getType()).getClassName()));
				if (value != null)
					log.info(String.format("%20s:%s", "value", value));
				return dcv.visitField(field, value);
			}

			int method_count = 0;
			int field_count = 0;

			public DexMethodVisitor visitMethod(Method method) {
				log.info("");
				log.info(String.format("%20s:%d", "method", method_count++));
				log.info(String.format("%20s:0x%08x", "access_flags", method.getAccessFlags()));
				log.info(String.format("%20s:%s", "name", method.getName()));
				log.info(String.format("%20s:%s", "describe", method.getType().toString()));
				log.info(String.format("%20s:%s", "return", Type.getType(method.getType().getReturnType()).getClassName()));
				log.info(String.format("%20s:%d", "args", method.getType().getParameterTypes().length));
				for (int i = 0; i < method.getType().getParameterTypes().length; i++) {
					log.info(String.format("%20s:[%2d]%s", "", i, Type.getType(method.getType().getParameterTypes()[i]).getClassName()));
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
						return new DumpDexCodeAdapter(dcv);
					}
				};
			}
		};
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
