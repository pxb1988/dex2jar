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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.dump.Dump;
import pxb.android.dex2jar.v3.Main;

/**
 * @author Panxiaobo
 * 
 */
public class ResTest {
	static final Logger log = LoggerFactory.getLogger(ResTest.class);

	@Test
	public void test() throws Exception {
		File dir = new File("target/test-classes/res");
		for (File f : FileUtils.listFiles(dir, new String[] { "class" }, false)) {
			log.info("Testing res file {}", f);
			String name = f.getName();
			name = name.substring(0, name.length() - ".class".length());
			File dex = TestUtils.dex(f, new File(dir, name + ".dex"));
			Main.doFile(dex);
			Dump.doFile(dex);
		}
		log.info("Done.");
	}
}