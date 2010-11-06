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
package pxb.android.dex2jar.v3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import pxb.android.dex2jar.ClassVisitorFactory;
import pxb.android.dex2jar.reader.DexFileReader;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String... args) {
		if (args.length == 0) {
			System.out.println("dex2jar file1.dexORapk file2.dexORapk ...");
		}
		for (String file : args) {
			File dex = new File(file);
			final File gen = new File(file + ".dex2jar.jar");
			try {
				doFile(dex, gen);
			} catch (IOException e) {
				throw new RuntimeException("处理文件时发生异常:" + dex, e);
			}
		}
	}

	public static void doData(byte[] data, File destJar) throws IOException {
		final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(destJar));

		DexFileReader reader = new DexFileReader(data);
		V3AccessFlagsAdapter afa = new V3AccessFlagsAdapter();
		reader.accept(afa);
		reader.accept(new V3(afa.getAccessFlagsMap(), new ClassVisitorFactory() {
			public ClassVisitor create(final String name) {
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
							byte[] data = this.toByteArray();
							ZipEntry entry = new ZipEntry(name + ".class");
							zos.putNextEntry(entry);
							zos.write(data);
							zos.closeEntry();
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

	public static void doFile(File srcDex) throws IOException {
		doFile(srcDex, new File(srcDex.getParentFile(), srcDex.getName() + ".dex2jar.jar"));
	}

	public static void doFile(File srcDex, File destJar) throws IOException {
		byte[] data = FileUtils.readFileToByteArray(srcDex);
		// checkMagic
		if ("dex".equals(new String(data, 0, 3))) {// dex
			doData(data, destJar);
		} else if ("PK".equals(new String(data, 0, 2))) {// ZIP
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
			for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
				if (entry.getName().equals("classes.dex")) {
					data = IOUtils.toByteArray(zis);
					doData(data, destJar);
				}
			}
		} else {
			throw new RuntimeException("the src file not a .dex file or a zip file");
		}

	}

}
