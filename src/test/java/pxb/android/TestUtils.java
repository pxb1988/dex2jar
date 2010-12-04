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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Panxiaobo
 * 
 */
public abstract class TestUtils {

        public static File dex(File file, File distFile) throws Exception {
                String dxJar = "src/test/resources/dx.jar";
                File dxFile = new File(dxJar);
                if (!dxFile.exists()) {
                        throw new RuntimeException("dx.jar文件不存在");
                }
                URLClassLoader cl = new URLClassLoader(new URL[] { dxFile.toURI().toURL() });
                Class<?> c = cl.loadClass("com.android.dx.command.Main");
                Method m = c.getMethod("main", String[].class);

                if (distFile == null)
                        distFile = File.createTempFile("dex", ".dex");

                String[] args = new String[] { "--dex", "--no-strict", "--output=" + distFile.getCanonicalPath(), file.getCanonicalPath() };
                m.invoke(null, new Object[] { args });
                return distFile;
        }

        public static File dex(File file) throws Exception {
                return dex(file, null);
        }
}