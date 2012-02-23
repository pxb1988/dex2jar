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
import java.lang.reflect.Method;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import p.rn.util.FileOut;
import p.rn.util.FileWalker;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker.OutAdapter;

public class ApkSign extends BaseCmd {
    public static void main(String[] args) {
        new ApkSign().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .apk file, default is $current_dir/[apk-name]-signed.apk", argName = "out-apk-file")
    private File output;
    @Opt(opt = "w", longOpt = "sign-whole", hasArg = false, description = "Sign whole apk file")
    private boolean signWhole = false;

    public ApkSign() {
        super("d2j-apk-sign [options] <apk>", "Sign an android apk file use a test certificate.");
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        File apkIn = new File(remainingArgs[0]);
        if (!apkIn.exists()) {
            System.err.println(apkIn + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            if (apkIn.isDirectory()) {
                output = new File(apkIn.getName() + "-signed.apk");
            } else {
                output = new File(FilenameUtils.getBaseName(apkIn.getName()) + "-signed.apk");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        File realJar;
        if (apkIn.isDirectory()) {
            realJar = File.createTempFile("d2j", ".jar");
            realJar.deleteOnExit();
            System.out.println("zipping " + apkIn + " -> " + realJar);
            OutHandler out = FileOut.create(realJar, true);
            try {
                new FileWalker().withStreamHandler(new OutAdapter(out)).walk(apkIn);
            } finally {
                IOUtils.closeQuietly(out);
            }
        } else {
            realJar = apkIn;
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(ApkSign.class
                .getResourceAsStream("ApkSign.cer"));
        KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(IOUtils.toByteArray(ApkSign.class
                .getResourceAsStream("ApkSign.private"))));

        Class<?> clz;
        try {
            clz = Class.forName("com.android.signapk.SignApk");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("please run d2j-apk-sign in a sun compatible JRE (contains sun.security.*)");
            return;
        }
        Method m = clz
                .getMethod("sign", X509Certificate.class, PrivateKey.class, boolean.class, File.class, File.class);
        m.setAccessible(true);

        System.out.println("sign " + realJar + " -> " + output);
        m.invoke(null, cert, privateKey, this.signWhole, realJar, output);
    }
}
