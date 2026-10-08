package com.hearth.app.core;

import com.hearth.app.model.KeyCertInfo;
import com.hearth.app.util.CryptoUtil;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
import org.javalabs.decl.util.MapperUtil;
import org.javalabs.decl.util.PEMUtil;
import org.javalabs.decl.util.StreamUtil;
import org.javalabs.decl.vertx.config.internal.ConfigStorage;
import org.javalabs.decl.vertx.jaxb.KeystoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class MtlsStorage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MtlsStorage.class);

    private static final MtlsStorage INSTANCE = new MtlsStorage();
    
    private Boolean initialized = Boolean.FALSE;
    private List<KeyCertInfo> infos;
    
    private MtlsStorage() {}
    
    public static MtlsStorage getInstance() {
        return INSTANCE;
    }
    
    public void init() throws GeneralSecurityException {
        try {
            if (initialized) {
                return;
            }
            String privKey = "";
            String certKey = "";
            
            KeystoreConfig ksConfig = ConfigStorage.get().serverConfig().getKeystoreConfig();
            if (ksConfig == null || ksConfig.getPemKeyConfig() == null) {
                return;
            }
            if (ksConfig.getPemKeyConfig().getBundlePath() != null) {
                // One file combining both the private key and the certificate. PemKeyCertOptions has no "bundle path" 
                // concept of its own (setKeyPath/setCertPath each want the file holding just that one PEM block)
                // So read the file once here and split out the two blocks ourselves, then hand Vert.x the raw PEM content
                // directly via setKeyValue/setCertValue instead of a path.
                byte[] b = StreamUtil.read(ksConfig.getPemKeyConfig().getBundlePath());
                String content = new String(b);

                privKey = PEMUtil.extractPemBlock(content, PEMUtil.KEY_BLOCK);
                certKey = PEMUtil.extractPemBlock(content, PEMUtil.CERT_BLOCK);
            }
            else {
                if (ksConfig.getPemKeyConfig().getKeyPath() != null) {
                    byte[] b = StreamUtil.read(ksConfig.getPemKeyConfig().getKeyPath());
                    String content = new String(b);
                            
                    privKey = PEMUtil.extractPemBlock(content, PEMUtil.KEY_BLOCK);
                }
                if (ksConfig.getPemKeyConfig().getCertPath() != null) {
                    byte[] b = StreamUtil.read(ksConfig.getPemKeyConfig().getCertPath());
                    String content = new String(b);
                            
                    certKey = PEMUtil.extractPemBlock(content, PEMUtil.CERT_BLOCK);
                }
            }
            PrivateKey privateKey = PEMUtil.generateKey(privKey);
            Certificate certificate = PEMUtil.generateCert(certKey);
            
            KeyCertInfo info = new KeyCertInfo();
            info.setPriv(CryptoUtil.serialize(privateKey));
            info.setPub(CryptoUtil.serialize(certificate.getPublicKey()));
            info.setCert(CryptoUtil.serialize(certificate));
            
            infos = List.of(info);
            this.initialized = Boolean.TRUE;
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Initialized hearth-app mtls store. Read file: {}", ksConfig.getPemKeyConfig().getBundlePath());
            }
            this.log();
            
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void log() throws GeneralSecurityException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("MTLS Keystore: {}", new String(MapperUtil.prettyWrite(infos, Boolean.FALSE)));
        }
    }
    
    public List<KeyCertInfo> storageInfo() {
        return infos;
    }
}
