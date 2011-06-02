/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public abstract class TestUtils {
    public static File dex(File file, File distFile) throws Exception {
        return dex(new File[] { file }, distFile);
    }

    public static File dex(File[] files, File distFile) throws Exception {
        return dex(Arrays.asList(files), distFile);
    }

    public static File dex(List<File> files, File distFile) throws Exception {
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
        List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + distFile.getCanonicalPath()));
        for (File f : files) {
            args.add(f.getCanonicalPath());
        }
        m.invoke(null, new Object[] { args.toArray(new String[0]) });
        return distFile;
    }

    public static File dex(File[] files) throws Exception {
        return dex(files, null);
    }
}