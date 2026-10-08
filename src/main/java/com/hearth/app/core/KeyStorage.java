package com.hearth.app.core;

import com.hearth.app.model.CertInfo;
import com.hearth.app.model.KeyCertInfo;
import com.hearth.app.model.KeyInfo;
import com.hearth.app.util.CryptoUtil;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javalabs.decl.util.MapperUtil;
import org.javalabs.decl.util.StreamUtil;
import org.javalabs.decl.vertx.config.internal.ConfigStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class KeyStorage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStorage.class);

    private static final KeyStorage INSTANCE = new KeyStorage();
    private KeyStore ks;
    private Boolean initialized = Boolean.FALSE;
    
    private List<KeyCertInfo> infos;

    private KeyStorage() {}
    
    public static KeyStorage getInstance() {
        return INSTANCE;
    }
    
    public void init() throws GeneralSecurityException {
        try {
            if (initialized) {
                return;
            }
            if (ConfigStorage.get().keystoreFile() == null) {
                return;
            }
            this.ks = KeyStore.getInstance("PKCS12");
            
            try (InputStream in = StreamUtil.stream(ConfigStorage.get().keystoreFile())) {
                ks.load(in, ConfigStorage.get().keystorePassword().toCharArray());
                
                this.initialized = Boolean.TRUE;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Initialized hearth-app keystore. Read keystore file: {}"
                            , ConfigStorage.get()
                                    .serverConfig()
                                    .getKeystoreConfig()
                                    .getStoreName());
                }
                this.log();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void log() throws GeneralSecurityException {
        infos = new ArrayList<>(aliases().size());
        
        for (String alias : aliases()) {
            KeyInfo priv = getPrivateKey(alias);
            KeyInfo pub = getPublicKey(alias);
            CertInfo cert = getCertificate(alias);
            
            infos.add(new KeyCertInfo(alias, priv, pub, cert));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JWT Keystore: {}", new String(MapperUtil.prettyWrite(infos, Boolean.FALSE)));
        }
    }
    
    public List<String> aliases() throws GeneralSecurityException {
        return Collections.list(ks.aliases());
    }
    
    public List<KeyCertInfo> storageInfo() {
        return infos;
    }
    
    public KeyCertInfo storageInfo(String alias) {
        for (KeyCertInfo info : infos) {
            if (info.getAlias().equals(alias)) {
                return info;
            }
        }
        return null;
    }
    
    public KeyInfo getPrivateKey(String alias) throws GeneralSecurityException {
        if (! ks.containsAlias(alias)) {
            throw new IllegalArgumentException("No such alias " + alias + " is found in keystore");
        }
        PrivateKey privateKey = (PrivateKey)ks.getKey(alias, ConfigStorage.get().keystorePassword().toCharArray());
        if (privateKey == null) {
            throw new IllegalArgumentException("No corresponding key found for alias " + alias + " in the key store");
        }
        return CryptoUtil.serialize(privateKey);
    }
    
    public KeyInfo getPublicKey(String alias) throws GeneralSecurityException {
        Certificate cert = ks.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();
        
        return CryptoUtil.serialize(publicKey);
    }
    
    public CertInfo getCertificate(String alias) throws GeneralSecurityException {
        if (! ks.containsAlias(alias)) {
            throw new IllegalArgumentException("No such alias " + alias + " is found in keystore");
        }

        Certificate certificate = ks.getCertificate(alias);
        if (certificate == null) {
            throw new IllegalArgumentException("No corresponding certificate found for alias " + alias + " in the key store");
        }
        
        return CryptoUtil.serialize(certificate);
    }
    
    public String entryType(String alias) throws GeneralSecurityException {
        if (ks.isKeyEntry(alias)) {
            return "Private/Public Key Pair";
        }
        else if (ks.isCertificateEntry(alias)) {
            return "Trusted Certificate";
        }
        else {
            return "Unknown";
        }
    }
}
