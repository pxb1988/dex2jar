/**
 * 
 */
package pxb.android.dex2jar.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import pxb.android.dex2jar.reader.DexFileReader;
import pxb.android.dex2jar.v1.ClassVisitorFactory;

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
				final File gen = new File(file + "." + (System.currentTimeMillis() / 1000) + ".g");
				byte[] data = FileUtils.readFileToByteArray(dex);
				new DexFileReader(data).accept(new ToAsm(new ClassVisitorFactory() {
					public ClassVisitor create(final String name) {
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
									FileUtils.writeByteArrayToFile(new File(gen, name + ".class"), this.toByteArray());
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						};
					}
				}));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
