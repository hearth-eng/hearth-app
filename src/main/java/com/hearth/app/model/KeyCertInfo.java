package com.hearth.app.model;

/**
 *
 * @author schan280
 */
public class KeyCertInfo {
    
    private String alias;
    private KeyInfo priv;
    private KeyInfo pub;
    private CertInfo cert;
    
    public KeyCertInfo() {}

    public KeyCertInfo(String alias, KeyInfo priv, KeyInfo pub, CertInfo cert) {
        this.alias = alias;
        this.priv = priv;
        this.pub = pub;
        this.cert = cert;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public KeyInfo getPriv() {
        return priv;
    }

    public void setPriv(KeyInfo priv) {
        this.priv = priv;
    }

    public KeyInfo getPub() {
        return pub;
    }

    public void setPub(KeyInfo pub) {
        this.pub = pub;
    }

    public CertInfo getCert() {
        return cert;
    }

    public void setCert(CertInfo cert) {
        this.cert = cert;
    }
}
