package com.hearth.app.core;

import com.hearth.app.model.KeyCertInfo;
import com.hearth.app.util.CryptoUtil;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;
import org.javalabs.decl.util.MapperUtil;
import org.javalabs.decl.util.PEMUtil;
import org.javalabs.decl.util.StreamUtil;
import org.javalabs.decl.vertx.config.internal.ConfigStorage;
import org.javalabs.decl.vertx.jaxb.TruststoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class CAStorage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CAStorage.class);

    private static final CAStorage INSTANCE = new CAStorage();
    
    private Boolean initialized = Boolean.FALSE;
    private List<KeyCertInfo> infos;
    
    private CAStorage() {}
    
    public static CAStorage getInstance() {
        return INSTANCE;
    }
    
    public void init() throws GeneralSecurityException {
        try {
            if (initialized) {
                return;
            }
            String privKey = "";
            String certKey = "";
            
            TruststoreConfig tsConfig = ConfigStorage.get().serverConfig().getTruststoreConfig();
            if (tsConfig == null || tsConfig.getPemCertConfig() == null) {
                return;
            }
            if (tsConfig.getPemCertConfig().getCertPath() != null) {
                // One file combining both the private key and the certificate. PemKeyCertOptions has no "bundle path" 
                // concept of its own (setKeyPath/setCertPath each want the file holding just that one PEM block)
                // So read the file once here and split out the two blocks ourselves, then hand Vert.x the raw PEM content
                // directly via setKeyValue/setCertValue instead of a path.
                byte[] b = StreamUtil.read(tsConfig.getPemCertConfig().getCertPath());
                String content = new String(b);
                certKey = PEMUtil.extractPemBlock(content, PEMUtil.CERT_BLOCK);
            }
            Certificate certificate = PEMUtil.generateCert(certKey);
            
            KeyCertInfo info = new KeyCertInfo();
            info.setPub(CryptoUtil.serialize(certificate.getPublicKey()));
            info.setCert(CryptoUtil.serialize(certificate));
            
            infos = List.of(info);
            this.initialized = Boolean.TRUE;
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Initialized hearth-app ca store. Read file: {}", tsConfig.getPemCertConfig().getCertPath());
            }
            this.log();
            
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void log() throws GeneralSecurityException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("CA Keystore: {}", new String(MapperUtil.prettyWrite(infos, Boolean.FALSE)));
        }
    }
    
    public List<KeyCertInfo> storageInfo() {
        return infos;
    }
}
