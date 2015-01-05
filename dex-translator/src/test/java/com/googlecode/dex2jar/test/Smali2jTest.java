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
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.smali.Smali;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@RunWith(Smali2jTest.S.class)
public class Smali2jTest {

    public static class S extends ParentRunner<Runner> {

        public S(Class<?> klass) throws InitializationError {
            super(klass);
            init(klass);
        }

        List<Runner> runners;

        public void init(final Class<?> testClass) throws InitializationError {
            URL url = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class");
            System.out.println("url is " + url);
            Assert.assertNotNull(url);

            final String file = url.getFile();
            Assert.assertNotNull(file);
            String dirx = file.substring(0, file.length() - testClass.getName().length() - ".class".length());

            System.out.println("dirx is " + dirx);
            final Path basePath = new File(dirx, "smalis").toPath();
            final Set<Path> files = new TreeSet<>();
            try {
                Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().endsWith(".smali")) {
                            files.add(file);
                        }
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<Runner> runners = new ArrayList<>();
            for (final Path p : files) {

                Smali smali = new Smali();
                final DexFileNode fileNode = new DexFileNode();
                try {
                    smali.smaliFile(p, fileNode);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                runners.add(new ParentRunner<DexClassNode>(testClass) {
                    @Override
                    protected List<DexClassNode> getChildren() {
                        return fileNode.clzs;
                    }

                    @Override
                    protected String getName() {
                        return "s2j [" + basePath.relativize(p) + "]";
                    }

                    @Override
                    protected Description describeChild(DexClassNode child) {
                        return Description.createTestDescription(testClass, "s [" + child.className + "]");
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
