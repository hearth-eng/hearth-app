package com.hearth.app.model;

/**
 *
 * @author schan280
 */
public class KeyInfo {
    
    private String algorithm;
    private String format;
    private String encodedBase64;
    private String fingerprintAlgo;
    private String fingerprint;
    
    public KeyInfo() {}

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getEncodedBase64() {
        return encodedBase64;
    }

    public void setEncodedBase64(String encodedBase64) {
        this.encodedBase64 = encodedBase64;
    }

    public String getFingerprintAlgo() {
        return fingerprintAlgo;
    }

    public void setFingerprintAlgo(String fingerprintAlgo) {
        this.fingerprintAlgo = fingerprintAlgo;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
