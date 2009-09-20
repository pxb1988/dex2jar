package pxb.android.dex2jar.v3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import pxb.android.dex2jar.ClassVisitorFactory;
import pxb.android.dex2jar.reader.DexFileReader;

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
				// final File gen = new File(file + "." +
				// (System.currentTimeMillis() / 1000) + ".g");
				final File gen = new File(file + ".g");
				byte[] data = FileUtils.readFileToByteArray(dex);
				DexFileReader reader = new DexFileReader(data);
				V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
				reader.accept(afa);
				reader.accept(new V3(afa.getAccessFlagsMap(), new ClassVisitorFactory() {
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
									byte[] data = this.toByteArray();
									// ClassNode node = new ClassNode();
									// new ClassReader(data).accept(node,
									// ClassReader.EXPAND_FRAMES);
									// ClassWriter cw = new
									// ClassWriter(ClassWriter.COMPUTE_MAXS);
									// node.accept(cw);
									// data = cw.toByteArray();
									FileUtils.writeByteArrayToFile(new File(gen, name + ".class"), data);
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
