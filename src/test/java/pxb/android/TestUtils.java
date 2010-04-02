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
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * @author Panxiaobo
 * 
 */
public class TestUtils {

	public static File dex(String file) throws Exception {

		Properties ps = new Properties();
		String dxJar = null;
		FileInputStream fis = FileUtils.openInputStream(new File("src/test/resources/pxb/android/dx.properties"));
		ps.load(fis);

		dxJar = ps.getProperty("dx.lib.jar");
		File dxFile = new File(dxJar);
		if (!dxFile.exists()) {
			throw new RuntimeException("dx.jar文件不存在");
		}
		URLClassLoader cl = new URLClassLoader(new URL[] { dxFile.toURI().toURL() });
		Class<?> c = cl.loadClass("com.android.dx.command.Main");
		Method m = c.getMethod("main", String[].class);

		File tempJar = File.createTempFile("dex", ".dex");

		String[] args = new String[] { "--dex", "--no-strict", "--output=" + tempJar.getCanonicalPath(), new File(file).getCanonicalPath() };
		m.invoke(null, new Object[] { args });
		return tempJar;
	}
}
