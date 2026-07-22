package com.googlecode.d2j.signapk;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class SunJarSignImpl extends AbstractJarSign {

    protected final X509Certificate cert;

    public SunJarSignImpl(X509Certificate cert, PrivateKey privateKey) {
        super(privateKey);
        this.cert = cert;
    }

    /**
     * Write a .RSA file with a digital signature.
     */
    protected void writeSignatureBlock(byte[] signature, OutputStream out) throws IOException {
        /* Code below is equivalent to the following:
        try {
            SignerInfo signerInfo = new SignerInfo(new X500Name(cert.getIssuerX500Principal().getName()),
                    cert.getSerialNumber(), AlgorithmId.get(digestAlg), AlgorithmId.get("RSA"), signature);

            PKCS7 pkcs7 = new PKCS7(new AlgorithmId[]{AlgorithmId.get(digestAlg)}, new ContentInfo(
                    ContentInfo.DATA_OID, null), new X509Certificate[]{cert}, new SignerInfo[]{signerInfo});

            pkcs7.encodeSignedData(out);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        */

        try {
            // Load classes dynamically
            Class<?> x500NameClass = Class.forName("sun.security.x509.X500Name");
            Class<?> algorithmIdClass = Class.forName("sun.security.x509.AlgorithmId");
            Class<?> contentInfoClass = Class.forName("sun.security.pkcs.ContentInfo");
            Class<?> signerInfoClass = Class.forName("sun.security.pkcs.SignerInfo");
            Class<?> pkcs7Class = Class.forName("sun.security.pkcs.PKCS7");
            Class<?> objectIdentifierClass = Class.forName("sun.security.util.ObjectIdentifier");
            Class<?> derValueClass = Class.forName("sun.security.util.DerValue");

            // Get constructors
            Constructor<?> x500NameConstructor = x500NameClass.getConstructor(String.class);
            Method algorithmIdConstructor = algorithmIdClass.getMethod("get", String.class);
            Constructor<?> contentInfoConstructor = contentInfoClass.getConstructor(objectIdentifierClass,
                    derValueClass);
            Constructor<?> signerInfoConstructor = signerInfoClass.getConstructor(x500NameClass,
                    BigInteger.class, algorithmIdClass, algorithmIdClass, byte[].class);
            Constructor<?> pkcs7Constructor = null;
            for (Constructor<?> c : pkcs7Class.getConstructors()) {
                if (c.getParameterCount() != 4) continue;
                Class<?>[] types = c.getParameterTypes();
                if (!types[0].isArray() || types[1] != contentInfoClass ||
                    types[2] != X509Certificate[].class || !types[3].isArray()) continue;
                pkcs7Constructor = c;
                break;
            }
            // Throw weird exception in order to allow having it in the catch block...
            if (pkcs7Constructor == null) throw new NoSuchAlgorithmException("PKCS7 constructor not found");

            // Create instances
            Object x500Name = x500NameConstructor.newInstance(cert.getIssuerX500Principal().getName());
            Object digestAlgorithmId = algorithmIdConstructor.invoke(null, digestAlg);
            Object signatureAlgorithmId = algorithmIdConstructor.invoke(null, "RSA");
            Object signerInfo = signerInfoConstructor.newInstance(x500Name, cert.getSerialNumber(),
                    digestAlgorithmId, signatureAlgorithmId, signature);

            // Prepare PKCS7 instance
            Object algorithms = Array.newInstance(algorithmIdClass, 1);
            Array.set(algorithms, 0, digestAlgorithmId);
            Field dataOIDField = contentInfoClass.getField("DATA_OID");
            Object dataOID = dataOIDField.get(null);

            // Throw weird exception in order to allow having it in the throws declaration...
            if (dataOID == null) throw new IOException("DATA_OID is null");

            Object contentInfo = contentInfoConstructor.newInstance(dataOID, null);
            X509Certificate[] certificates = {cert};
            Object signerInfos = Array.newInstance(signerInfoClass, 1);
            Array.set(signerInfos, 0, signerInfo);

            // Create PKCS7 instance
            Object pkcs7 = pkcs7Constructor.newInstance(algorithms, contentInfo, certificates, signerInfos);

            // Invoke encodeSignedData method
            Method encodeSignedDataMethod = pkcs7Class.getMethod("encodeSignedData", OutputStream.class);
            encodeSignedDataMethod.invoke(pkcs7, out);
        } catch (NoSuchAlgorithmException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException | NoSuchFieldException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
