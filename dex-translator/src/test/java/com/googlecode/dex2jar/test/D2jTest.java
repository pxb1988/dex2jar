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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@RunWith(D2jTest.S.class)
public class D2jTest {

    public static class S extends ParentRunner<Runner> {

        public S(Class<?> klass) throws InitializationError {
            super(klass);
            init(klass);
        }

        List<Runner> runners;

        public void init(final Class<?> testClass) throws InitializationError {
            Collection<Path> files = TestUtils.listTestDexFiles();

            List<Runner> runners = new ArrayList<>(files.size());

            for (final Path f : files) {
                final DexFileNode fileNode = readDex(f);
                runners.add(new ParentRunner<DexClassNode>(testClass) {
                    @Override
                    protected List<DexClassNode> getChildren() {
                        return fileNode.clzs;
                    }

                    @Override
                    protected String getName() {
                        return "d2j [" + f.toString() + "]";
                    }

                    @Override
                    protected Description describeChild(DexClassNode child) {
                        return Description.createTestDescription(testClass, "c [" + child.className + "]");
                    }

                    @Override
                    protected void runChild(final DexClassNode child, RunNotifier notifier) {
                        runLeaf(new Statement() {
                            @Override
                            public void evaluate() throws Throwable {
                                TestUtils.translateAndCheck(fileNode, child);
                            }
                        }, describeChild(child), notifier);
                    }
                });
            }
            this.runners = runners;
        }

        private DexFileNode readDex(Path f) {
            DexFileNode fileNode = new DexFileNode();
            DexFileReader reader = null;
            try {
                reader = new DexFileReader(ZipUtil.readDex(f));
            } catch (IOException e) {
                throw new RuntimeException("Fail to read dex:" + f);
            }
            reader.accept(fileNode);
            return fileNode;
        }

        @Override
        protected List<Runner> getChildren() {
            return runners;
        }

        @Override
        protected Description describeChild(Runner child) {
            return child.getDescription();
        }

        @Override
        protected void runChild(Runner child, RunNotifier notifier) {
            child.run(notifier);
        }
    }
}
