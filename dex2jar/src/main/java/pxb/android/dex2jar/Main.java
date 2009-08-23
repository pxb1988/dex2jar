/**
 * 
 */
package pxb.android.dex2jar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("dex2jar file1.dex file2.dex ...");
		}
		for (String file : args) {
			try {
				File dex = new File(file);
				File jar = new File(file + "." + (System.currentTimeMillis() / 1000) + ".jar");
				byte[] data = FileUtils.readFileToByteArray(dex);
				final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(jar));
				zos.setComment("Create by dex2jar version:" + Version.version);
				new DexFile(data, true).accept(new ClassVisitorFactory() {
					public ClassVisitor create(final String name) {
//						 if (!name.equals("javax/servlet/ServletInputStream")) {
//						 return null;
//						 }
						return new ClassWriter(0) {
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
				});
				zos.finish();
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
