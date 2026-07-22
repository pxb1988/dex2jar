package com.googlecode.d2j.jasmin;

import com.googlecode.dex2jar.tools.BaseCmd;
import com.googlecode.dex2jar.tools.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

@BaseCmd.Syntax(cmd = "d2j-jasmin2jar", syntax = "[options] <jar>", desc = "Assemble .j files to .class file",
        onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/Jasmin")
public class Jasmin2JarCmd extends BaseCmd implements Opcodes {

    @Opt(opt = "g", longOpt = "autogenerate-linenumbers", hasArg = false, description = "autogenerate-linenumbers")
    boolean autogenLines = false;

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is "
                                                      + "$current_dir/[jar-name]-jasmin2jar.jar",
            argName = "out-jar-file")
    private Path output;

    @Opt(opt = "e", longOpt = "encoding", description = "encoding for .j files, default is UTF-8", argName = "enc")
    private String encoding = "UTF-8";

    @Opt(opt = "d", longOpt = "dump", description = "dump to stdout", hasArg = false)
    private boolean dump;

    @Opt(longOpt = "no-compute-max", description = "", hasArg = false)
    private boolean noComputeMax;

    @Opt(opt = "cv", longOpt = "class-version", description = "default .class version, [1~" +
                                                              Constants.MAX_JAVA_VERSION + "], default 8 for JAVA8")
    private int classVersion = 8;

    public Jasmin2JarCmd() {
    }

    public static void main(String... args) throws ClassNotFoundException, SecurityException {
        new Jasmin2JarCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }
        if (classVersion < 1 || classVersion > Constants.MAX_JAVA_VERSION) {
            throw new HelpException("-cv,--class-version out of range, 1-" + Constants.MAX_JAVA_VERSION +
                                    " is supported.");
        }

        Path jar = new File(remainingArgs[0]).toPath().toAbsolutePath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " doesn't exist");
            usage();
            return;
        }

        if (output == null) {
            output = new File(getBaseName(jar) + "-jasmin2jar/").toPath();
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("assemble " + jar + " -> " + output);

        if (!output.toString().endsWith(".jar") && !output.toString().endsWith(".zip")) {
            assemble0(jar, output);
        } else {
            try (FileSystem fs = createZip(output)) {
                assemble0(jar, fs.getPath("/"));
            }
        }
    }

    private void assemble0(Path in, Path output) throws IOException {
        if (Files.isDirectory(in)) { // a dir
            travelFileTree(in, output);
        } else if (in.toString().endsWith(".j")) {
            assemble1(in, output);
        } else {
            try (FileSystem fs = openZip(in)) {
                travelFileTree(fs.getPath("/"), output);
            }
        }
    }

    private void travelFileTree(Path in, final Path output) throws IOException {
        Files.walkFileTree(in, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".j")) {
                    assemble1(file, output);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private void assemble1(Path file, Path output) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(file, Charset.forName(encoding))) {
            ANTLRStringStream is = new ANTLRReaderStream(bufferedReader);
            is.name = file.toString();
            JasminLexer lexer = new JasminLexer(is);
            CommonTokenStream ts = new CommonTokenStream(lexer);
            JasminParser parser = new JasminParser(ts);
            parser.rebuildLine = autogenLines;
            ClassWriter cw = new ClassWriter(noComputeMax ? 0 : ClassWriter.COMPUTE_MAXS);
            ClassNode cn = parser.parse();
            if (cn.version == 0) {
                cn.version = Constants.JAVA_VERSIONS[classVersion];
            }
            if (dump) {
                new JasminDumper(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true))
                        .dump(cn);
            }
            cn.accept(cw);
            Path clzFile = output.resolve(cn.name.replace('.', '/') + ".class");
            createParentDirectories(clzFile);
            Files.write(clzFile, cw.toByteArray());
        } catch (RecognitionException e) {
            System.err.println("Failed to assemble " + file);
            e.printStackTrace();
        }
    }

}
