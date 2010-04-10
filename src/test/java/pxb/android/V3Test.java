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
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.v3.Main;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3Test {
	static final Logger log = LoggerFactory.getLogger(V3Test.class);

	@Test
	public void test() throws IOException {
		File file = new File("target/test-classes/dexes");
		Iterator it = FileUtils.iterateFiles(file, new String[] { "dex" }, false);
		while (it.hasNext()) {
			File f = (File) it.next();
			log.info("dex2jar file {}", f);
			Main.doFile(f);
		}
	}
}
