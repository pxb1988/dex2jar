/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
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
			System.out.println("dex2jar file1.dex file2.dex ...");
		}
		for (String file : args) {
			try {
				File dex = new File(file);
				final File gen = new File(file + ".dex2jar.jar");

				final ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(gen));

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
									ZipEntry entry = new ZipEntry(name + ".class");
									zos.putNextEntry(entry);
									zos.write(data);
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
