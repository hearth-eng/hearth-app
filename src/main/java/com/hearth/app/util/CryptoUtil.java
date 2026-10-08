package com.hearth.app.util;

import com.hearth.app.model.CertInfo;
import com.hearth.app.model.KeyInfo;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 *
 * @author schan280
 */
public class CryptoUtil {
    
    public static KeyInfo serialize(Key key) throws GeneralSecurityException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(key.getEncoded());
        String fingerprint = byteToHex(digest);
        
        KeyInfo info = new KeyInfo();
        info.setAlgorithm(key.getAlgorithm());
        info.setFormat(key.getFormat());
        info.setEncodedBase64(Base64.getEncoder().encodeToString(key.getEncoded()));
        info.setFingerprintAlgo("SHA-256");
        info.setFingerprint(fingerprint);
        
        return info;
    }
    
    public static CertInfo serialize(Certificate certificate) throws GeneralSecurityException {
        X509Certificate x509 = (X509Certificate) certificate;
        CertInfo cert = new CertInfo();
        
        try {
            x509.checkValidity();
            cert.setStatus("VALID");
        }
        catch (CertificateExpiredException | CertificateNotYetValidException e) {
            cert.setStatus("EXPIRED");
        }

        cert.setSubject(x509.getSubjectX500Principal().getName());
        cert.setIssuer(x509.getIssuerX500Principal().getName());
        cert.setSerialNumber(x509.getSerialNumber().toString(16).toUpperCase());
        cert.setAlgorithm(x509.getPublicKey().getAlgorithm());
        cert.setSignature(Base64.getEncoder().encodeToString(x509.getSignature()));
        cert.setSignAlgorithm(x509.getSigAlgName());
        cert.setValidFrom(x509.getNotBefore().toString());
        cert.setValidUntil(x509.getNotAfter().toString());
        cert.setSans(x509.getSubjectAlternativeNames());
        
        if (x509.getPublicKey() instanceof RSAPublicKey) {
            cert.setKeySizeBits(((RSAPublicKey) x509.getPublicKey()).getModulus().bitLength());
        }
        return cert;
    }

    public static String byteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X:", b));
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
