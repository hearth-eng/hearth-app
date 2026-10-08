package com.hearth.app.core;

import com.hearth.app.cache.ReferenceDataLoader;
import com.hearth.app.cache.impl.UserRoleCache;
import org.javalabs.decl.vertx.container.VertxContainer;
import com.hearth.app.config.ApplicationConfiguration;
import com.hearth.app.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javalabs.jpa.JdbcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class AppContainer extends VertxContainer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AppContainer.class);
    
    private static final String DEFAULT_CONFIG = "app.json";
    private static final String PERSISTENCE_UNIT = "hearth-app-pu";
    
    private final ReferenceDataLoader loader = new ReferenceDataLoader();
    
    public AppContainer() {
        super();
    }

    @Override
    protected void preDeploy() {
        try {
            // Read the application configuration file.
            // Application may have environment specific file. At the time of boostrap, the environment name 
            // is picked up from the variable EPAAS_ENV.
            String cfgFile = System.getProperty("app.config", DEFAULT_CONFIG);

            ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
            appConfig.init(cfgFile);
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Read application configuration file {}", cfgFile);
            }
            // Initialize the database.
            EntityManagerFactory emf = initDb();
            
            // Load cache
            loadCache(emf);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void postDeploy() {
        try {
            // Initialize keystore and mTLS store
            KeyStorage.getInstance().init();
            MtlsStorage.getInstance().init();
            CAStorage.getInstance().init();
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private EntityManagerFactory initDb() {
        // System.setProperty("orm.config.file", "persistence.xml");
        
        Map<String, Object> props = ApplicationConfiguration.getInstance().get("db.config");
        Map<String, String> dbConfig = new HashMap<>();
        
        if (props == null) {
            // Using default h2 db as specified in persistence.xml.
        }
        else {
            // Override default database configuration.
            Object val = null;
            
            if ((val = props.get("url")) != null && ((String)val).trim().length() > 0) {
                dbConfig.put("javax.persistence.jdbc.url", (String)val);
            }
            if ((val = props.get("user")) != null && ((String)val).trim().length() > 0) {
                dbConfig.put("javax.persistence.jdbc.user", (String)val);
            }
            if ((val = props.get("password")) != null && ((String)val).trim().length() > 0) {
                dbConfig.put("javax.persistence.jdbc.password", (String)val);
            }
            if ((val = props.get("host")) != null && ((String)val).trim().length() > 0) {
                dbConfig.put("javax.persistence.jdbc.host", (String)val);
            }
            if ((val = props.get("port")) != null) {
                dbConfig.put("javax.persistence.jdbc.port", String.valueOf(val));
            }
            if (props.get("db.pool.size.max") != null) {
                dbConfig.put("db.pool.size.max", String.valueOf(props.get("db.pool.size.max")));
            }
            if (props.get("db.pool.conn.timeout") != null) {
                dbConfig.put("db.pool.conn.timeout", String.valueOf(props.get("db.pool.conn.timeout")));
            }
            if (props.get("db.pool.min.idle") != null) {
                dbConfig.put("db.pool.min.idle",  String.valueOf(props.get("db.pool.min.idle")));
            }
            if (props.get("db.pool.idle.timeout") != null) {
                dbConfig.put("db.pool.idle.timeout", String.valueOf(props.get("db.pool.idle.timeout")));
            }
            if (props.get("db.pool.keep.alive") != null) {
                dbConfig.put("db.pool.keep.alive", String.valueOf(props.get("db.pool.keep.alive")));
            }

        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, dbConfig);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initialized jpa-lite entity manager factory.");
        }
        return emf;
    }

    private void loadCache(EntityManagerFactory emf) {
        EntityManager em = null;
        
        try {
            em = emf.createEntityManager();
            
            // 1. Load admin users
            List<User> adminUsers = em.createNamedQuery("User.selectByRole", User.class)
                    .setParameter(1, User.Role.ADMIN)
                    .getResultList();
            
            for (User user : adminUsers) {
                UserRoleCache.getCache().add(user.getExternalId(), user);
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded {} admin user(s)", adminUsers.size());
            }
            
            // Cache the reference/static data.
            loader.loadCache(emf);
        }
        catch (JdbcException e) {
            LOGGER.error("Error loading startup cache", e);
        }
        finally {
            if (em != null) {
                em.close();
            }
        }
    }
}

