package com.googlecode.d2j.signapk;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public class SunJarSignImpl extends AbstractJarSign {
    final protected X509Certificate cert;

    public SunJarSignImpl(X509Certificate cert, PrivateKey privateKey) {
        super(privateKey);
        this.cert = cert;
    }

    /** Write a .RSA file with a digital signature. */
    @SuppressWarnings("all")
    protected void writeSignatureBlock(byte[] signature, OutputStream out) throws IOException {
        try {
            SignerInfo signerInfo = new SignerInfo(new X500Name(cert.getIssuerX500Principal().getName()),
                    cert.getSerialNumber(), AlgorithmId.get(digestAlg), AlgorithmId.get("RSA"), signature);

            PKCS7 pkcs7 = new PKCS7(new AlgorithmId[] { AlgorithmId.get(digestAlg) }, new ContentInfo(
                    ContentInfo.DATA_OID, null), new X509Certificate[] { cert }, new SignerInfo[] { signerInfo });

            pkcs7.encodeSignedData(out);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
