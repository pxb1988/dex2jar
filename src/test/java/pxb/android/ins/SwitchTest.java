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
package pxb.android.ins;

import java.io.File;

import org.junit.Test;

import pxb.android.TestUtils;
import pxb.android.dex2jar.dump.Dump;
import pxb.android.dex2jar.v3.Main;

/**
 * @author Panxiaobo
 * 
 */
public class SwitchTest {
	@Test
	public void test() throws Exception {
		File f = TestUtils.dex("target/test-classes/res/SwitchRes.class");
		Main.doFile(f, new File("target/switch.dex2jar.jar"));
		Dump.doFile(f, new File("target/switch.dump.jar"));
	}
}
