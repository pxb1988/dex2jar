package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.dex.ClassVisitorFactory;
import com.googlecode.d2j.dex.ExDex2Asm;
import com.googlecode.d2j.dex.LambadaNameSafeClassAdapter;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.MultiDexFileReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@BaseCmd.Syntax(cmd = "d2j-mt-dex2jar", syntax = "[options] <file0> [file1 ... fileN]", desc = "convert dex to jar")
public class Dex2jarMultiThreadCmd extends BaseCmd {

    public static void main(String... args) {
        new Dex2jarMultiThreadCmd().doMain(args);
    }

    @Opt(opt = "mt", longOpt = "multi-thread", description = "concurrent process, default is 4 thread")
    private int multiThread = 4;

    @Opt(opt = "fl", longOpt = "file-list", description = "a file contains a list of dex to process")
    private Path fileList;

    @Opt(opt = "dsn", longOpt = "dont-sanitize-names", hasArg = false, description = "do not replace '_' by '-'")
    private boolean dontSanitizeNames = false;

    @Override
    protected void doCommandLine() throws Exception {
        List<String> f = new ArrayList<>(Arrays.asList(remainingArgs));
        if (fileList != null) {
            f.addAll(Files.readAllLines(fileList, StandardCharsets.UTF_8));
        }
        if (f.isEmpty()) {
            throw new HelpException();
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(multiThread);

        final Iterator<String> fileIt = f.iterator();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (fileIt.hasNext()) {
                    String fileName = fileIt.next();
                    try {
                        run0(fileName, executorService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executorService.submit(this); // run this job again
                    }
                } else {
                    executorService.shutdown();
                }
            }
        });
        executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    private void run0(String fileName, final ExecutorService executorService) throws IOException {
        // long baseTS = System.currentTimeMillis();
        String baseName = getBaseName(new File(fileName).toPath());
        Path currentDir = new File(".").toPath();
        Path file = currentDir.resolve(baseName + "-dex2jar.jar");
        final Path errorFile = currentDir.resolve(baseName + "-error.zip");
        System.err.println("dex2jar " + fileName + " -> " + file);
        final BaksmaliBaseDexExceptionHandler exceptionHandler = new BaksmaliBaseDexExceptionHandler();
        BaseDexFileReader reader = MultiDexFileReader.open(Files.readAllBytes(new File(fileName).toPath()));
        DexFileNode fileNode = new DexFileNode();
        try {
            reader.accept(fileNode, DexFileReader.SKIP_DEBUG | DexFileReader.IGNORE_READ_EXCEPTION);
        } catch (Exception ex) {
            exceptionHandler.handleFileException(ex);
            throw ex;
        }
        final FileSystem fs = createZip(file);
        final Path dist = fs.getPath("/");
        ClassVisitorFactory cvf = new ClassVisitorFactory() {
            @Override
            public ClassVisitor create(final String name) {
                final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                final LambadaNameSafeClassAdapter rca = new LambadaNameSafeClassAdapter(cw, dontSanitizeNames);
                return new ClassVisitor(Constants.ASM_VERSION, rca) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        String className = rca.getClassName();
                        byte[] data;
                        try {
                            // FIXME handle 'java.lang.RuntimeException: Method code too large!'
                            data = cw.toByteArray();
                        } catch (Exception ex) {
                            System.err.printf("ASM failed to generate .class file: %s%n", className);
                            exceptionHandler.handleFileException(ex);
                            return;
                        }
                        try {
                            Path dist1 = dist.resolve(className + ".class");
                            BaseCmd.createParentDirectories(dist1);
                            Files.write(dist1, data);
                        } catch (IOException e) {
                            exceptionHandler.handleFileException(e);
                        }
                    }
                };
            }
        };

        new ExDex2Asm(exceptionHandler) {

            @Override
            public void convertDex(final DexFileNode fileNode, final ClassVisitorFactory cvf) {
                if (fileNode.clzs != null) {
                    final Map<String, Clz> classes = collectClzInfo(fileNode);
                    final List<Future<?>> results = new ArrayList<>(fileNode.clzs.size());
                    for (final DexClassNode classNode : fileNode.clzs) {
                        results.add(executorService.submit(() -> convertClass(fileNode, classNode, cvf, classes)));
                    }
                    executorService.submit(() -> {
                        for (Future<?> result : results) {
                            try {
                                result.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        BaksmaliBaseDexExceptionHandler exceptionHandler1 =
                                (BaksmaliBaseDexExceptionHandler) exceptionHandler;
                        if (exceptionHandler1.hasException()) {
                            exceptionHandler1.dump(errorFile, new String[0]);
                        }
                        try {
                            fs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }.convertDex(fileNode, cvf);
    }

}
