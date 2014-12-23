/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.googlecode.d2j.signapk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public abstract class AbstractJarSign {
    /** Write to another stream and also feed it to the Signature object. */
    private static class SignatureOutputStream extends FilterOutputStream {
        private int mCount;
        private Signature mSignature;

        public SignatureOutputStream(OutputStream out, Signature sig) {
            super(out);
            mSignature = sig;
            mCount = 0;
        }

        public int size() {
            return mCount;
        }

        @Override
        public void write(byte[] b) throws IOException {
            try {
                mSignature.update(b, 0, b.length);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            out.write(b);
            mCount += b.length;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                mSignature.update(b, off, len);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            out.write(b, off, len);
            mCount += len;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                mSignature.update((byte) b);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            out.write(b);
            mCount++;
        }
    }

    // Files matching this pattern are not copied to the output.
    private static Pattern stripPattern = Pattern.compile("^META-INF/(.*)[.](SF|RSA|DSA)$");
    private static final byte[] EOL = "\r\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COL = ": ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NAMES = "Name: ".getBytes(StandardCharsets.UTF_8);

    /**
     * Copy all the files in a manifest from input to output. We set the
     * modification times in the output to a fixed time, so as to reduce
     * variation in the output file and make incremental OTAs more efficient.
     */
    private static void copyFiles(Manifest manifest, JarFile in, JarOutputStream out, long timestamp)
            throws IOException {
        byte[] buffer = new byte[4096];
        int num;

        Map<String, Attributes> entries = manifest.getEntries();
        List<String> names = new ArrayList<>(entries.keySet());
        Collections.sort(names);
        for (String name : names) {
            JarEntry inEntry = in.getJarEntry(name);
            JarEntry outEntry = null;
            if (inEntry.getMethod() == JarEntry.STORED) {
                // Preserve the STORED method of the input entry.
                outEntry = new JarEntry(inEntry);
            } else {
                // Create a new entry so that the compressed len is recomputed.
                outEntry = new JarEntry(name);
            }
            outEntry.setTime(timestamp);
            out.putNextEntry(outEntry);

            InputStream data = in.getInputStream(inEntry);
            while ((num = data.read(buffer)) > 0) {
                out.write(buffer, 0, num);
            }
            out.flush();
        }
    }

    final protected String digestAlg;

    final protected PrivateKey privateKey;

    public AbstractJarSign(PrivateKey privateKey) {
        this(privateKey, "SHA1", "SHA1withRSA");
    }

    public AbstractJarSign(PrivateKey privateKey, String digestAlg, String signAlg) {
        super();

        this.privateKey = privateKey;
        this.digestAlg = digestAlg;
        this.signAlg = signAlg;
    }

    final protected String signAlg;

    /** Add the SHA1 of every file to the manifest, creating it if necessary. */
    private Manifest addDigestsToManifest(JarFile jar) throws IOException, GeneralSecurityException {
        Manifest input = jar.getManifest();
        Manifest output = new Manifest();
        Attributes main = output.getMainAttributes();
        if (input != null) {
            main.putAll(input.getMainAttributes());
        }
        main.putValue("Manifest-Version", "1.0");
        main.putValue("Created-By", "1.6.0_21 (d2j-" + AbstractJarSign.class.getPackage().getImplementationVersion() + ")");

        MessageDigest md = MessageDigest.getInstance(digestAlg);
        byte[] buffer = new byte[4096];
        int num;

        // We sort the input entries by name, and add them to the
        // output manifest in sorted order. We expect that the output
        // map will be deterministic.

        TreeMap<String, JarEntry> byName = new TreeMap<String, JarEntry>();

        for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
            JarEntry entry = e.nextElement();
            byName.put(entry.getName(), entry);
        }

        String digName = digestAlg + "-Digest";
        for (JarEntry entry : byName.values()) {
            String name = entry.getName();
            if (!entry.isDirectory() && !name.equals(JarFile.MANIFEST_NAME) && !stripPattern.matcher(name).matches()) {
                InputStream data = jar.getInputStream(entry);
                while ((num = data.read(buffer)) > 0) {
                    md.update(buffer, 0, num);
                }

                Attributes attr = null;
                if (input != null) {
                    attr = input.getAttributes(name);
                }
                attr = attr != null ? new Attributes(attr) : new Attributes();
                attr.putValue(digName, encodeBase64(md.digest()));
                output.getEntries().put(name, attr);
            }
        }

        return output;
    }

    protected String encodeBase64(byte[] data) {
       return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public void sign(File in, File out) throws IOException, GeneralSecurityException {

        JarFile inputJar = null;
        JarOutputStream outputJar = null;
        FileOutputStream outputFile = null;

        try {

            // Assume the certificate is valid for at least an hour.
            long timestamp = System.currentTimeMillis();

            inputJar = new JarFile(in, false); // Don't verify.

            OutputStream outputStream = outputFile = new FileOutputStream(out);
            outputJar = new JarOutputStream(outputStream);
            outputJar.setLevel(9);

            JarEntry je;

            // MANIFEST.MF
            Manifest manifest = addDigestsToManifest(inputJar);
            je = new JarEntry(JarFile.MANIFEST_NAME);
            je.setTime(timestamp);
            outputJar.putNextEntry(je);
            manifest.write(outputJar);

            // CERT.SF
            Signature signature = Signature.getInstance(signAlg);
            signature.initSign(privateKey);
            je = new JarEntry("META-INF/CERT.SF");
            je.setTime(timestamp);
            outputJar.putNextEntry(je);
            writeSignatureFile(manifest, new SignatureOutputStream(outputJar, signature));

            int i = digestAlg.toLowerCase().indexOf("with");
            String ext;
            if (i > 0) {
                ext = digestAlg.substring(i + 4);
            } else {
                ext = "RSA";
            }
            // CERT.RSA
            je = new JarEntry("META-INF/CERT." + ext);
            je.setTime(timestamp);
            outputJar.putNextEntry(je);

            writeSignatureBlock(signature.sign(), outputJar);

            // Everything else
            copyFiles(manifest, inputJar, outputJar, timestamp);

            outputJar.close();
            outputJar = null;
            outputStream.flush();

        } finally {
            try {
                if (inputJar != null) {
                    inputJar.close();
                }
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Write a .RSA file with a digital signature. */
    protected abstract void writeSignatureBlock(byte[] signature, OutputStream out) throws IOException;

    /** Write a .SF file with a digest of the specified manifest. */
    private void writeSignatureFile(Manifest manifest, SignatureOutputStream out) throws IOException,
            GeneralSecurityException {
        Manifest sf = new Manifest();
        Attributes main = sf.getMainAttributes();
        main.putValue("Signature-Version", "1.0");
        main.putValue("Created-By", "1.6.0_21 (d2j-" + AbstractJarSign.class.getPackage().getImplementationVersion() + ")");

        MessageDigest md = MessageDigest.getInstance(digestAlg);
        DigestOutputStream print = new DigestOutputStream(new OutputStream() {

            @Override
            public void write(byte[] b) throws IOException {

            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {

            }

            @Override
            public void write(int b) throws IOException {

            }
        }, md);

        // Digest of the entire manifest
        manifest.write(print);
        print.flush();
        main.putValue(digestAlg + "-Digest-Manifest", encodeBase64(md.digest()));

        // digest main attribute
        Manifest m2 = new Manifest();
        m2.getMainAttributes().putAll(manifest.getMainAttributes());
        m2.write(print);
        main.putValue(digestAlg + "-Digest-Manifest-Main-Attributes", encodeBase64(md.digest()));

        String digName = digestAlg + "-Digest";
        Map<String, Attributes> entries = manifest.getEntries();

        for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
            // Digest of the manifest stanza for this entry.
            print.write(NAMES);
            print.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
            print.write(EOL);
            for (Map.Entry<Object, Object> att : entry.getValue().entrySet()) {
                print.write(att.getKey().toString().getBytes(StandardCharsets.UTF_8));
                print.write(COL);
                print.write(att.getKey().toString().getBytes(StandardCharsets.UTF_8));
                print.write(EOL);
            }
            print.write(EOL);
            print.flush();

            Attributes sfAttr = new Attributes();
            sfAttr.putValue(digName, encodeBase64(md.digest()));
            sf.getEntries().put(entry.getKey(), sfAttr);
        }

        sf.write(out);

        // A bug in the java.util.jar implementation of Android platforms
        // up to version 1.6 will cause a spurious IOException to be thrown
        // if the length of the signature file is a multiple of 1024 bytes.
        // As a workaround, add an extra CRLF in this case.
        if ((out.size() % 1024) == 0) {
            out.write(EOL);
        }
    }
}
