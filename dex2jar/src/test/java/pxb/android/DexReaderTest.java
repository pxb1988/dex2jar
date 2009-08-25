/**
 * 
 */
package pxb.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import pxb.android.dex2jar.ClassVisitorFactory;
import pxb.android.dex2jar.Version;
import pxb.android.dex2jar.reader.DexFileReader;
import pxb.android.dex2jar.visitors.impl.ToAsm;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexReaderTest {
	@Test
	public void test() throws IOException {
		final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(new File("target/gen_test.jar")));
		zos.setComment("Create by dex2jar version:" + Version.version);
		new DexFileReader(new File("target/classes.dex")).accept(new ToAsm(new ClassVisitorFactory() {
			public ClassVisitor create(final String name) {
//				if (!name.equals("org/mortbay/jetty/HttpHeaders"))
//					return null;
				return new ClassWriter(ClassWriter.COMPUTE_MAXS) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.objectweb.asm.ClassWriter#visitEnd()
					 */
					@Override
					public void visitEnd() {
						super.visitEnd();
						try {
							zos.putNextEntry(new ZipEntry(name + ".class"));
							IOUtils.write(this.toByteArray(), zos);
							zos.closeEntry();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
			}
		}));
		zos.finish();
		zos.close();
	}
}
