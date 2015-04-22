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

import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.BaseDexExceptionHandler;
import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;
import com.googlecode.dex2jar.ir.ET;
import org.objectweb.asm.MethodVisitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BaksmaliBaseDexExceptionHandler extends BaseDexExceptionHandler {
    public static final String REPORT_MESSAGE = "Please report this file to one of following link if possible (any one).\n" + //
            "    https://sourceforge.net/p/dex2jar/tickets/\n" + //
            "    https://bitbucket.org/pxb1988/dex2jar/issues\n" + //
            "    https://github.com/pxb1988/dex2jar/issues [no attachment support, not preferred]\n" + //
            "    dex2jar@googlegroups.com";

    private Map<DexMethodNode, Exception> exceptionMap = new HashMap<>();
    private List<Exception> fileExceptions = new ArrayList<>();

    public boolean hasException() {
        return exceptionMap.size() > 0 || fileExceptions.size() > 0;
    }

    @Override
    public void handleFileException(Exception e) {
        super.handleFileException(e);
        fileExceptions.add(e);
    }

    @Override
    public void handleMethodTranslateException(Method method, DexMethodNode methodNode, MethodVisitor mv, Exception e) {
        super.handleMethodTranslateException(method, methodNode, mv, e);
        exceptionMap.put(methodNode, e);
    }

    public static String getVersionString() {
        List<String> vs = new ArrayList<>();
        doAddVersion(vs, "dex-reader", DexFileReader.class);
        doAddVersion(vs, "dex-reader-api", Method.class);
        doAddVersion(vs, "dex-ir", ET.class);
        doAddVersion(vs, "d2j-smali", Smali.class);
        doAddVersion(vs, "d2j-base-cmd", BaseCmd.class);
        doAddVersion(vs, "dex-tools", Dex2jarCmd.class);
        doAddVersion(vs, "dex-translator", Dex2jar.class);
        return vs.toString();
    }

    private static void doAddVersion(List<String> vs, String pkg, Class<?> clz) {
        try {
            vs.add(pkg + "-" + clz.getPackage().getImplementationVersion());
        } catch (Exception ignore) {
            // ignored
        }
    }

    public void dump(Path exFile, String[] originalArgs) {
        String fileName = exFile.getFileName().toString().toLowerCase();
        try {
            if (fileName.endsWith(".zip")) {
                dumpZip(exFile, originalArgs);
            } else if (fileName.endsWith(".gz")) {
                dumGZip(exFile, originalArgs);
            } else {
                dumpTxt(exFile, originalArgs);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpTxt(Path exFile, String[] originalArgs) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files
                .newOutputStream(exFile), StandardCharsets.UTF_8))) {
            dumpTxt0(writer, originalArgs);
        }
    }

    private void dumGZip(Path exFile, String[] originalArgs) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files
                .newOutputStream(exFile)), StandardCharsets.UTF_8))) {
            dumpTxt0(writer, originalArgs);
        }
    }

    private void dumpTxt0(BufferedWriter writer, String[] originalArgs) throws IOException {
        dumpSummary(originalArgs, writer);
        int i = 0;
        for (Map.Entry<DexMethodNode, Exception> e : exceptionMap.entrySet()) {
            DexMethodNode dexMethodNode = e.getKey();
            Exception ex = e.getValue();
            writer.newLine();
            writer.write("================= " + i++ + " ===================");
            writer.newLine();
            dumpMethod(writer, dexMethodNode, ex);
        }
    }

    public void dumpZip(Path exFile, String[] originalArgs) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(exFile))) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));

            zos.putNextEntry(new ZipEntry("summary.txt"));
            dumpTxt0(writer, originalArgs);
            zos.closeEntry();
            zos.flush();
        }
    }

    // dump each method
    private void dumpMethod(BufferedWriter writer, DexMethodNode dexMethodNode, Exception ex) throws IOException {
        writer.append(dexMethodNode.method.toString());
        writer.newLine();
        writer.flush();
        ex.printStackTrace(new PrintWriter(writer, true));
        writer.newLine();
        new BaksmaliDumper().baksmaliMethod(dexMethodNode, writer);
        writer.flush();
    }

    // dump summary: timestamp, version, cmdline
    private void dumpSummary(String[] originalArgs, BufferedWriter writer) throws IOException {
        writer.write("#This file is generated by dex2jar");
        writer.newLine();
        writer.write(REPORT_MESSAGE);
        writer.newLine();
        writer.newLine();
        if (fileExceptions.size() > 0) {
            writer.append(String.format("There are %d fails.", fileExceptions.size()));
            writer.newLine();
        }
        if (exceptionMap.size() > 0) {
            writer.append(String.format("There are %d methods fail to translate.", exceptionMap.size()));
            writer.newLine();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        writer.append(sdf.format(new Date()));
        writer.newLine();
        writer.append("version: ");
        writer.append(getVersionString());
        writer.newLine();
        writer.append("cmdline: ");
        writer.append(Arrays.asList(originalArgs).toString());
        writer.newLine();

        writer.append("env:");
        writer.newLine();
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("java.") && !key.toLowerCase().contains("pass")) {
                writer.append(key).append(": ").append(properties.getProperty(key));
                writer.newLine();
            }
        }
        PrintWriter p = new PrintWriter(writer, true);
        for (Exception ex : fileExceptions) {
            ex.printStackTrace(p);
        }
        writer.flush();
    }
}
