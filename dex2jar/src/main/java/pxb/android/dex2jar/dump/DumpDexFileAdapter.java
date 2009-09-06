/**
 * 
 */
package pxb.android.dex2jar.dump;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexAnnotationAble;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;
import pxb.android.dex2jar.visitors.EmptyDexCodeAdapter;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpDexFileAdapter implements DexFileVisitor {
	private static final Logger log = LoggerFactory.getLogger(DumpDexFileAdapter.class);
	int class_count = 0;

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
		return new DexClassVisitor() {

			public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
				// TODO Auto-generated method stub
				return null;
			}

			public void visitEnd() {
				// TODO Auto-generated method stub

			}

			public DexFieldVisitor visitField(Field field, Object value) {
				// TODO Auto-generated method stub
				return null;
			}

			int method_count = 0;

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
				return new DexMethodVisitor() {

					public DexAnnotationVisitor visitAnnotation(String name, int visiable) {
						// TODO Auto-generated method stub
						return null;
					}

					public DexCodeVisitor visitCode() {
						return new DumpDexCodeAdapter(new EmptyDexCodeAdapter());
					}

					public void visitEnd() {
						// TODO Auto-generated method stub

					}

					public DexAnnotationAble visitParamesterAnnotation(int index) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			public void visitSource(String file) {
				// TODO Auto-generated method stub

			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
		log.info("=========");
	}

}
