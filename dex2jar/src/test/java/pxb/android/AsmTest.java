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
package pxb.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;

public class AsmTest {
	@Test
	public void trycatch() throws IOException {
		InputStream in = AsmTest.class.getResourceAsStream("/test/TryCatch.class");
		Assert.assertNotNull(in);
		byte[] data = IOUtils.toByteArray(in);
		new ClassReader(data).accept(new ASMifierClassVisitor(new PrintWriter(System.out)), ClassReader.EXPAND_FRAMES
				| ClassReader.SKIP_DEBUG);
	}
}
