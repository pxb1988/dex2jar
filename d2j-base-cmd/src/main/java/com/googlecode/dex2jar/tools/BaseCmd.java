/*
 * dex2jar - Tools to work with android .dex and java .class files
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
package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public abstract class BaseCmd {
    public static String getBaseName(String fn) {
        int x = fn.lastIndexOf('.');
        return x >= 0 ? fn.substring(0, x) : fn;
    }

    public static String getBaseName(Path fn) {
        return getBaseName(fn.getFileName().toString());
    }

    public interface FileVisitorX {
        // change the relative from Path to String
        // java.nio.file.ProviderMismatchException on jdk8
        void visitFile(Path file, String relative) throws IOException;
    }

    public static void walkFileTreeX(final Path base, final FileVisitorX fv) throws IOException {
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fv.visitFile(file, base.relativize(file).toString());
                return super.visitFile(file, attrs);
            }
        });
    }

    public static void walkJarOrDir(final Path in, final FileVisitorX fv) throws IOException {
        if (Files.isDirectory(in)) {
            walkFileTreeX(in, fv);
        } else {
            try (FileSystem inputFileSystem = openZip(in)) {
                walkFileTreeX(inputFileSystem.getPath("/"), fv);
            }
        }
    }

    public static void createParentDirectories(Path p) throws IOException {
        // merge patch from t3stwhat, fix crash on save to windows path like 'C:\\abc.jar'
        Path parent = p.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    public static FileSystem createZip(Path output) throws IOException {
        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");
        Files.deleteIfExists(output);

        createParentDirectories(output);

        for (FileSystemProvider p : FileSystemProvider.installedProviders()) {
            String s = p.getScheme();
            if ("jar".equals(s) || "zip".equalsIgnoreCase(s)) {
                return p.newFileSystem(output, env);
            }
        }
        throw new IOException("cant find zipfs support");
    }

    public static FileSystem openZip(Path in) throws IOException {
        for (FileSystemProvider p : FileSystemProvider.installedProviders()) {
            String s = p.getScheme();
            if ("jar".equals(s) || "zip".equalsIgnoreCase(s)) {
                return p.newFileSystem(in, new HashMap<String, Object>());
            }
        }
        throw new IOException("cant find zipfs support");
    }

    @SuppressWarnings("serial")
    protected static class HelpException extends RuntimeException {

        public HelpException() {
            super();
        }

        public HelpException(String message) {
            super(message);
        }

    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.FIELD })
    static public @interface Opt {
        String argName() default "";

        String description() default "";

        boolean hasArg() default true;

        String longOpt() default "";

        String opt() default "";

        boolean required() default false;
    }

    static protected class Option implements Comparable<Option> {
        public String argName = "arg";
        public String description;
        public Field field;
        public boolean hasArg = true;
        public String longOpt;
        public String opt;
        public boolean required = false;

        @Override
        public int compareTo(Option o) {
            int result = s(this.opt, o.opt);
            if (result == 0) {
                result = s(this.longOpt, o.longOpt);
                if (result == 0) {
                    result = s(this.argName, o.argName);
                    if (result == 0) {
                        result = s(this.description, o.description);
                    }
                }
            }
            return result;
        }

        private static int s(String a, String b) {
            if (a != null && b != null) {
                return a.compareTo(b);
            } else if (a != null) {
                return 1;
            } else if (b != null) {
                return -1;
            } else {
                return 0;
            }
        }

        public String getOptAndLongOpt() {
            StringBuilder sb = new StringBuilder();
            boolean havePrev = false;
            if (opt != null && opt.length() > 0) {
            sb.append("-").append(opt);
                havePrev = true;
            }
            if (longOpt != null && longOpt.length() > 0) {
                if (havePrev) {
                sb.append(",");
            }
                sb.append("--").append(longOpt);
            }
            return sb.toString();
        }

    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE })
    static public @interface Syntax {

        String cmd();

        String desc() default "";

        String onlineHelp() default "";

        String syntax() default "";
    }

    private String cmdLineSyntax;

    private String cmdName;
    private String desc;
    private String onlineHelp;

    protected Map<String, Option> optMap = new HashMap<String, Option>();

    @Opt(opt = "h", longOpt = "help", hasArg = false, description = "Print this help message")
    private boolean printHelp = false;

    protected String remainingArgs[];
    protected String orginalArgs[];

    public BaseCmd() {
    }

    public BaseCmd(String cmdLineSyntax, String header) {
        super();
        int i = cmdLineSyntax.indexOf(' ');
        if (i > 0) {
            this.cmdName = cmdLineSyntax.substring(0, i);
            this.cmdLineSyntax = cmdLineSyntax.substring(i + 1);
        }
        this.desc = header;
    }

    public BaseCmd(String cmdName, String cmdSyntax, String header) {
        super();
        this.cmdName = cmdName;
        this.cmdLineSyntax = cmdSyntax;
        this.desc = header;
    }

    private Set<Option> collectRequriedOptions(Map<String, Option> optMap) {
        Set<Option> options = new HashSet<Option>();
        for (Map.Entry<String, Option> e : optMap.entrySet()) {
            Option option = e.getValue();
            if (option.required) {
                options.add(option);
            }
        }
        return options;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object convert(String value, Class type) {
        if (type.equals(String.class)) {
            return value;
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.parseFloat(value);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        }
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        if (type.equals(File.class)) {
            return new File(value);
        }
        if (type.equals(Path.class)) {
            return new File(value).toPath();
        }
        try {
            type.asSubclass(Enum.class);
            return Enum.valueOf(type, value);
        } catch (Exception e) {
        }

        throw new RuntimeException("can't convert [" + value + "] to type " + type);
    }

    ;

    protected abstract void doCommandLine() throws Exception;

    public void doMain(String... args) {
        try {
            initOptions();
            parseSetArgs(args);
            doCommandLine();
        } catch (HelpException e) {
            String msg = e.getMessage();
            if (msg != null && msg.length() > 0) {
                System.err.println("ERROR: " + msg);
            }
            usage();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected String getVersionString() {
        return getClass().getPackage().getImplementationVersion();
    }

    protected void initOptionFromClass(Class<?> clz) {
        if (clz == null) {
            return;
        } else {
            initOptionFromClass(clz.getSuperclass());
        }

        Syntax syntax = clz.getAnnotation(Syntax.class);
        if (syntax != null) {
            this.cmdLineSyntax = syntax.syntax();
            this.cmdName = syntax.cmd();
            this.desc = syntax.desc();
            this.onlineHelp = syntax.onlineHelp();
        }

        Field[] fs = clz.getDeclaredFields();
        for (Field f : fs) {
            Opt opt = f.getAnnotation(Opt.class);
            if (opt != null) {
                f.setAccessible(true);
                if (!opt.hasArg()) {
                    Class<?> type = f.getType();
                    if (!type.equals(boolean.class)) {
                        throw new RuntimeException("the type of " + f
                                + " must be boolean, as it is declared as no args");
                    }
                    boolean b;
                    try {
                        b = (Boolean) f.get(this);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (b) {
                        throw new RuntimeException("the value of " + f + " must be false, as it is declared as no args");
                    }
                }
                Option option = new Option();
                option.field = f;
                option.description = opt.description();
                option.hasArg = opt.hasArg();
                option.required = opt.required();
                boolean haveLongOpt = false;
                if (!"".equals(opt.longOpt())) {
                    option.longOpt = opt.longOpt();
                    checkConflict(option, "--" + option.longOpt);
                    haveLongOpt = true;
                }
                if (!"".equals(opt.argName())) {
                    option.argName = opt.argName();
                }
                if (!"".equals(opt.opt())) {
                    option.opt = opt.opt();
                    checkConflict(option, "-" + option.opt);
                } else {
                    if (!haveLongOpt) {
                        throw new RuntimeException("opt or longOpt is not set in @Opt(...) " + f);
            }
        }
    }
        }
    }

    private void checkConflict(Option option, String key) {
        if (optMap.containsKey(key)) {
            Option preOption = optMap.get(key);
            throw new RuntimeException(String.format("[@Opt(...) %s] conflict with [@Opt(...) %s]",
                    preOption.field.toString(), option.field
            ));
        }
        optMap.put(key, option);
    }

    protected void initOptions() {
        initOptionFromClass(this.getClass());
    }

    public static void main(String... args) throws Exception {
        if (args.length < 1) {
            System.err.println("d2j-run <class> [args]");
            return;
        }
        Class<?> clz = Class.forName(args[0]);
        String newArgs[] = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        if (BaseCmd.class.isAssignableFrom(clz)) {
            BaseCmd baseCmd = (BaseCmd) clz.newInstance();
            baseCmd.doMain(newArgs);
        } else {
            Method m = clz.getMethod("main",String[].class);
            m.setAccessible(true);
            m.invoke(null, (Object)newArgs);
        }
    }
    
    protected void parseSetArgs(String... args) throws IllegalArgumentException, IllegalAccessException {
        this.orginalArgs = args;
        List<String> remainsOptions = new ArrayList<String>();
        Set<Option> requiredOpts = collectRequriedOptions(optMap);
        Option needArgOpt = null;
        for (String s : args) {
            if (needArgOpt != null) {
                needArgOpt.field.set(this, convert(s, needArgOpt.field.getType()));
                needArgOpt = null;
            } else if (s.startsWith("-")) {// its a short or long option
                Option opt = optMap.get(s);
                requiredOpts.remove(opt);
                if (opt == null) {
                    System.err.println("ERROR: Unrecognized option: " + s);
                    throw new HelpException();
                } else {
                    if (opt.hasArg) {
                        needArgOpt = opt;
                    } else {
                        opt.field.set(this, true);
                    }
                }
            } else {
                remainsOptions.add(s);
            }
        }

        if (needArgOpt != null) {
            System.err.println("ERROR: Option " + needArgOpt.getOptAndLongOpt() + " need an argument value");
            throw new HelpException();
        }
        this.remainingArgs = remainsOptions.toArray(new String[remainsOptions.size()]);
        if (this.printHelp) {
            throw new HelpException();
        }
        if (!requiredOpts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("ERROR: Options: ");
            boolean first = true;
            for (Option option : requiredOpts) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" and ");
                }
                sb.append(option.getOptAndLongOpt());
            }
            sb.append(" is required");
            System.err.println(sb.toString());
            throw new HelpException();
        }

    }

    protected void usage() {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8), true);

        final int maxLength = 80;
        final int maxPaLength = 40;
        out.println(this.cmdName + " -- " + desc);
        out.println("usage: " + this.cmdName + " " + cmdLineSyntax);
        if (this.optMap.size() > 0) {
            out.println("options:");
        }
        // [PART.A.........][Part.B
        // .-a,--aa.<arg>...desc1
        // .................desc2
        // .-b,--bb
        TreeSet<Option> options = new TreeSet<Option>(this.optMap.values());
        int palength = -1;
        for (Option option : options) {
            int pa = 4 + option.getOptAndLongOpt().length();
            if (option.hasArg) {
                pa += 3 + option.argName.length();
            }
            if (pa < maxPaLength) {
                if (pa > palength) {
                    palength = pa;
                }
            }
        }
        int pblength = maxLength - palength;

        StringBuilder sb = new StringBuilder();
        for (Option option : options) {
            sb.setLength(0);
            sb.append(" ").append(option.getOptAndLongOpt());
            if (option.hasArg) {
                sb.append(" <").append(option.argName).append(">");
            }
            String desc = option.description;
            if (desc == null || desc.length() == 0) {// no description
                out.println(sb);
            } else {
                for (int i = palength - sb.length(); i > 0; i--) {
                    sb.append(' ');
                }
                if (sb.length() > maxPaLength) {// to huge part A
                    out.println(sb);
                    sb.setLength(0);
                    for (int i = 0; i < palength; i++) {
                        sb.append(' ');
                    }
                }
                int nextStart = 0;
                while (nextStart < desc.length()) {
                    if (desc.length() - nextStart < pblength) {// can put in one line
                        sb.append(desc.substring(nextStart));
                        out.println(sb);
                        nextStart = desc.length();
                        sb.setLength(0);
                    } else {
                        sb.append(desc.substring(nextStart, nextStart + pblength));
                        out.println(sb);
                        nextStart += pblength;
                        sb.setLength(0);
                        if (nextStart < desc.length()) {
                            for (int i = 0; i < palength; i++) {
                                sb.append(' ');
                            }
                        }
                    }
                }
                if (sb.length() > 0) {
                    out.println(sb);
                    sb.setLength(0);
                }
            }
        }
        String ver = getVersionString();
        if (ver != null && !"".equals(ver)) {
            out.println("version: " + ver);
        }
        if (onlineHelp != null && !"".equals(onlineHelp)) {
            if (onlineHelp.length() + "online help: ".length() > maxLength) {
                out.println("online help: ");
                out.println(onlineHelp);
            } else {
                out.println("online help: " + onlineHelp);
            }
        }
        out.flush();
    }
}
