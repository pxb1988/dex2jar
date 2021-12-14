package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.jasmin.JasminDumper;
import com.googlecode.d2j.jasmin.Jasmins;
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
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.objectweb.asm.tree.ClassNode;

@RunWith(Jasmin2jTest.TestRunner.class)
public class Jasmin2jTest {

    public static class TestRunner extends ParentRunner<Path> {

        public TestRunner(Class<?> klass) throws InitializationError {
            super(klass);
            init(klass);
        }

        Path basePath;
        List<Path> runners = new ArrayList<>();

        public void init(final Class<?> testClass) {
            URL url = testClass.getResource("/jasmins/type.j");
            Assert.assertNotNull(url);

            final String file = url.getFile();
            Assert.assertNotNull(file);

            basePath = new File(file).toPath().getParent();

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
                    ClassNode cn = Jasmins.parse(basePath.resolve(child));
                    JasminDumper dumper = new JasminDumper(new PrintWriter(System.out, true));
                    dumper.dump(cn);
                }
            }, describeChild(child), notifier);
        }

    }

}
