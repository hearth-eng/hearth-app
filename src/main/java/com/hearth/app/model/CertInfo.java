package com.hearth.app.model;

import java.util.Collection;

/**
 *
 * @author schan280
 */
public class CertInfo {
    
    private String entryType;
    private String validFrom;
    private String validUntil;
    private String status;
    private String subject;
    private String issuer;
    private String serialNumber;
    private String algorithm;
    private String signAlgorithm;
    private Integer keySizeBits;
    private Collection<?> sans;
    private String signature;

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSignAlgorithm() {
        return signAlgorithm;
    }

    public void setSignAlgorithm(String signAlgorithm) {
        this.signAlgorithm = signAlgorithm;
    }

    public Integer getKeySizeBits() {
        return keySizeBits;
    }

    public void setKeySizeBits(Integer keySizeBits) {
        this.keySizeBits = keySizeBits;
    }

    public Collection<?> getSans() {
        return sans;
    }

    public void setSans(Collection<?> sans) {
        this.sans = sans;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
