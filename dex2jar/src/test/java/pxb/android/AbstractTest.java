/**
 * 
 */
package pxb.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import pxb.android.dex2jar.ClassVisitorFactory;
import pxb.android.dex2jar.DexFile;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public abstract class AbstractTest {
	protected abstract String getName();

	protected abstract String getBase();

	@Test
	public void test() throws IOException {
		final String name = getName();
		InputStream is = AbstractTest.class.getResourceAsStream(name + ".dex");
		Assert.assertNotNull(is);
		new DexFile(IOUtils.toByteArray(is)).accept(new ClassVisitorFactory() {

			public ClassVisitor create() {

				return new ClassWriter(0) {
					String className;

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.objectweb.asm.ClassWriter#visit(int, int,
					 * java.lang.String, java.lang.String, java.lang.String,
					 * java.lang.String[])
					 */
					@Override
					public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
						className = name;
						super.visit(version, access, name, signature, superName, interfaces);
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.objectweb.asm.ClassWriter#visitEnd()
					 */
					@Override
					public void visitEnd() {
						super.visitEnd();
						try {
							FileUtils.writeByteArrayToFile(new File(getBase() + className + ".class"), this.toByteArray());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				};
			}
		});
	}
}
