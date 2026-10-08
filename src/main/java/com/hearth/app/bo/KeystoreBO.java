package com.hearth.app.bo;

import com.hearth.app.auth.AppUser;
import com.hearth.app.core.CAStorage;
import com.hearth.app.core.KeyStorage;
import com.hearth.app.core.MtlsStorage;
import com.hearth.app.model.KeyCertInfo;
import com.hearth.app.model.User;
import com.hearth.app.util.QueryParams;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.javalabs.decl.util.StopWatch;
import org.javalabs.decl.vertx.config.internal.ConfigStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class KeystoreBO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeystoreBO.class);

    public KeystoreBO() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized KeystoreBO: {}", getClass().getSimpleName());
        }
    }

    public Map<String, Object> viewAliases(AppUser usr) throws IllegalAccessException {
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        if (! User.isAdmin(usr.principal().priv())) {
            throw new IllegalAccessException();
        }
        try {
            List<String> aliases = KeyStorage.getInstance().aliases();
            
            timer.stop();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Fetched {} alias(s). Elapsed time(ms): {}", aliases, timer.elapsedTimeMillis());
            }
            return Map.of(
                    "keystore", ConfigStorage.get()
                            .serverConfig()
                            .getKeystoreConfig()
                            .getStoreName()
                    , "aliases", aliases
            );
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public List<KeyCertInfo> viewStorageInfo(AppUser usr, QueryParams params) throws IllegalAccessException {
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        if (! User.isAdmin(usr.principal().priv())) {
            throw new IllegalAccessException();
        }
        String type = params.param("type");
        if (type == null) {
            type = "JWT";
        }
        List<KeyCertInfo> result = null;
        if ("MTLS".equalsIgnoreCase(type)) {
            result = mtlsStorageInfo();
        }
        else if ("JWT".equalsIgnoreCase(type)) {
            result = jwtStorageInfo();
        }
        else if ("CA".equalsIgnoreCase(type)) {
            result = caStorageInfo();
        }
        else {
            throw new IllegalArgumentException("Invalid type. Valid values are: [JWT, MTLS, CA]");
        }

        timer.stop();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched storage info for {}. Elapsed time(ms): {}", type, timer.elapsedTimeMillis());
        }
        return result;
    }
    
    private List<KeyCertInfo> jwtStorageInfo() {
        return KeyStorage.getInstance().storageInfo();
    }
    
    private List<KeyCertInfo> mtlsStorageInfo() {
        return MtlsStorage.getInstance().storageInfo();
    }
    
    private List<KeyCertInfo> caStorageInfo() {
        return CAStorage.getInstance().storageInfo();
    }
}
