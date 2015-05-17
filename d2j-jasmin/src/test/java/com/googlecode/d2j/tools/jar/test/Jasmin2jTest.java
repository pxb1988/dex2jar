/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2015 Panxiaobo
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
package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.jasmin.JasminDumper;
import com.googlecode.d2j.jasmin.Jasmins;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@RunWith(Jasmin2jTest.S.class)
public class Jasmin2jTest {

    public static class S extends ParentRunner<Path> {

        public S(Class<?> klass) throws InitializationError {
            super(klass);
            init(klass);
        }

        Path basePath;
        List<Path> runners = new ArrayList<>();

        public void init(final Class<?> testClass) throws InitializationError {
            URL url = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class");
            Assert.assertNotNull(url);

            final String file = url.getFile();
            Assert.assertNotNull(file);
            String dirx = file.substring(0, file.length() - testClass.getName().length() - ".class".length());

            basePath = new File(dirx, "jasmins").toPath();

            System.out.println("jasmins dir is " + basePath);

            try {
                Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().endsWith(".j")) {
                            runners.add(basePath.relativize(file));
                        }
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<Path> getChildren() {
            return runners;
        }

        @Override
        protected Description describeChild(Path child) {
            return Description.createTestDescription(getTestClass().getJavaClass(), child.toString());
        }

        @Override
        protected void runChild(final Path child, RunNotifier notifier) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    ClassNode cn= Jasmins.parse(basePath.resolve(child));
                    JasminDumper dumper=new JasminDumper(new PrintWriter(System.out,true));
                    dumper.dump(cn);
                }
            }, describeChild(child), notifier);
        }
    }
}
