/**
 * 
 */
package pxb.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import pxb.android.dex2jar.reader.DexFileReader;
import pxb.android.dex2jar.v1.ClassVisitorFactory;
import pxb.android.dex2jar.v2.ToAsm;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V2Test {
	@Test
	public void test() throws IOException {
		// final ZipOutputStream zos = new
		// ZipOutputStream(FileUtils.openOutputStream(new
		// File("target/gen_test.jar")));
		// zos.setComment("Create by dex2jar version:" + Version.version);
		final File base = new File("target/g/");
		new DexFileReader(new File("target/classes.dex")).accept(new ToAsm(new ClassVisitorFactory() {
			public ClassVisitor create(final String name) {
				 if (!name.equals("org/mortbay/jetty/handler/DefaultHandler"))
				 return null;
				return new ClassWriter(ClassWriter.COMPUTE_MAXS) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.objectweb.asm.ClassWriter#visitEnd()
					 */
					@Override
					public void visitEnd() {
						super.visitEnd();
						byte[] data = this.toByteArray();
						try {
							// zos.putNextEntry(new ZipEntry(name + ".class"));
							// IOUtils.write(data, zos);
							// zos.closeEntry();
							FileUtils.writeByteArrayToFile(new File(base, name + ".class"), data);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							Class<?> c = new Load().def(data, name);
							System.out.println(c);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				};
			}
		}));
//		zos.finish();
//		zos.close();
	}

	static class Load extends ClassLoader {
		public Class<?> def(byte[] data, String name) {
			return defineClass(name.replace('/', '.'), data, 0, data.length);
		}
	}
}
