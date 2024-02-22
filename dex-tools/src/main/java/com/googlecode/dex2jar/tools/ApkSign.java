package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.signapk.AbstractJarSign;
import com.googlecode.d2j.signapk.SunJarSignImpl;
import com.googlecode.d2j.signapk.TinySignImpl;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

@BaseCmd.Syntax(cmd = "d2j-apk-sign", syntax = "[options] <apk>", desc = "Sign an android apk file use a test "
        + "certificate.")
public class ApkSign extends BaseCmd {

    public static void main(String... args) {
        new ApkSign().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "o", longOpt = "output", description = "output .apk file, default is $current_dir/[apk-name]-signed"
            + ".apk", argName = "out-apk-file")
    private Path output;

    @Opt(opt = "t", longOpt = "tiny", hasArg = false, description = "use tiny sign")
    private boolean tiny = false;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        Path apkIn = new File(remainingArgs[0]).toPath();
        if (!Files.exists(apkIn)) {
            System.err.println(apkIn + " doesn't exist");
            usage();
            return;
        }

        if (output == null) {
            if (Files.isDirectory(apkIn)) {
                output = new File(apkIn.getFileName() + "-signed.apk").toPath();
            } else {
                output = new File(getBaseName(apkIn.getFileName().toString()) + "-signed.apk").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        Path tmp = null;
        try {
            final Path realJar;
            if (Files.isDirectory(apkIn)) {
                realJar = Files.createTempFile("d2j", ".jar");
                tmp = realJar;
                System.out.println("zipping " + apkIn + " -> " + realJar);
                try (FileSystem fs = createZip(realJar)) {
                    final Path outRoot = fs.getPath("/");
                    walkJarOrDir(apkIn, (file, relative) -> {
                        Path target = outRoot.resolve(relative);
                        createParentDirectories(target);
                        Files.copy(file, target);
                    });
                }
            } else {
                realJar = apkIn;
            }

            AbstractJarSign signer;

            if (tiny) {
                signer = new TinySignImpl();
            } else {
                try {
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(ApkSign.class
                            .getResourceAsStream("ApkSign.cer"));
                    KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
                    PrivateKey privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(ZipUtil
                            .toByteArray(ApkSign.class.getResourceAsStream("ApkSign.private"))));

                    signer = new SunJarSignImpl(cert, privateKey);
                } catch (Exception cnfe) {
                    signer = new TinySignImpl();
                }
            }
            signer.sign(apkIn.toFile(), output.toFile());

            System.out.println("sign " + realJar + " -> " + output);
        } finally {
            if (tmp != null) {
                Files.deleteIfExists(tmp);
            }
        }
    }

}
