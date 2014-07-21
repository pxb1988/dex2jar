/*
 * Copyright (c) 2009-2012 Panxiaobo
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
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@RunWith(ResTest.R.class)
public class ResTest {

    public static class FileSet {
        String name;
        List<Path> files = new ArrayList<>(3);
    }

    public static class R extends ParentRunner<FileSet> {
        public R(Class<?> testClass) throws InitializationError {
            super(testClass);
            init(testClass);
        }

        List<FileSet> fileSetList;
        File dir;

        void init(Class<?> testClass) {
            URL url = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class");
            Assert.assertNotNull(url);
            String file = url.getFile();
            Assert.assertNotNull(file);
            String dirx = file.substring(0, file.length() - testClass.getName().length() - ".class".length());

            dir = new File(dirx, "res");
            Map<String, FileSet> m = new HashMap<>();
            for (Path f : TestUtils.listPath(dir, ".class")) {
                String name = getBaseName(f.getFileName().toString());

                int i = name.indexOf('$');
                String z = i > 0 ? name.substring(0, i) : name;
                FileSet fs = m.get(z);
                if (fs == null) {
                    fs = new FileSet();
                    fs.name = z;
                    m.put(z, fs);
                }
                fs.files.add(f);
            }
            this.fileSetList = new ArrayList<>(m.values());
        }

        @Override
        protected List<FileSet> getChildren() {
            return fileSetList;
        }

        @Override
        protected Description describeChild(FileSet child) {
            return Description.createTestDescription(getTestClass().getJavaClass(), child.name);
        }

        @Override
        protected void runChild(final FileSet child, RunNotifier notifier) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    File dex = TestUtils.dexP(child.files, new File(dir, child.name + ".dex"));
                    File distFile = new File(dex.getParentFile(), getBaseName(dex.getName()) + "_d2j.jar");
                    DexFileNode fileNode = new DexFileNode();
                    DexFileReader r = new DexFileReader(dex);
                    r.accept(fileNode);
                    for (DexClassNode classNode : fileNode.clzs) {
                        TestUtils.translateAndCheck(fileNode, classNode);
                    }
                }
            }, describeChild(child), notifier);
        }
    }

    public static String getBaseName(String fn) {
        int x = fn.lastIndexOf('.');
        return x >= 0 ? fn.substring(0, x) : fn;
    }
}
