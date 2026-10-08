package com.hearth.app.bo;

import com.hearth.app.auth.AppUser;
import com.hearth.app.dao.UtilityDAO;
import com.hearth.app.model.User;
import com.hearth.app.util.QueryParams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.javalabs.decl.util.StopWatch;
import org.javalabs.jpa.DAOProxy;
import org.javalabs.jpa.annotation.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class DBQueryBO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBQueryBO.class);

    @Dao
    protected final UtilityDAO utilityDAO;


    public DBQueryBO() {
        this.utilityDAO = DAOProxy.get(UtilityDAO.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized Handler: {}. UtilityDAO: {}"
                    , getClass().getSimpleName()
                    , utilityDAO);
        }

    }

    public List<Object[]> executeSql(AppUser usr, String sql, QueryParams params) throws IllegalAccessException {
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        if (! User.isAdmin(usr.principal().priv())) {
            throw new IllegalAccessException();
        }

        List<Object[]> result = new ArrayList<>(2);
        String sqlQuery = sql.trim();

        if (sqlQuery.length() == 0) {
            throw new IllegalArgumentException("Empty query string provided");
        }

        // Check if it is a dml query. A dml query will wlways prefixed with hint.
        if (sqlQuery.startsWith(UtilityDAO.DML_HINT)) {
            sqlQuery = sqlQuery.substring(UtilityDAO.DML_HINT.length() + 1);
            int res = utilityDAO.execute(sqlQuery);

            result.add(new Object[] {"result"});
            result.add(new Object[] {res + " row(s) affected"});
        }
        else if (sqlQuery.startsWith(UtilityDAO.DDL_HINT)) {
            sqlQuery = sqlQuery.substring(UtilityDAO.DDL_HINT.length() + 1);
            int res = utilityDAO.execute(sqlQuery);

            result.add(new Object[] {"result"});
            result.add(new Object[] {res + " row(s) affected"});
        }
        else {
            int idx = sqlQuery.indexOf(" ");
            if (idx == -1) {
                throw new IllegalArgumentException("Empty query string provided");
            }
            String prefix = sqlQuery.substring(0, idx).toUpperCase();
            if (prefix.startsWith("INSERT") || prefix.startsWith("UPDATE") || prefix.startsWith("DELETE")
                    || prefix.startsWith("CREATE") || prefix.startsWith("ALTER") || prefix.startsWith("GRANT")
                    || prefix.startsWith("ANALYZE")) {

                throw new IllegalArgumentException("DML or DDL Queries are not supported");
            }
            if (sqlQuery.contains("offset") || sqlQuery.contains("limit")
                    || sqlQuery.contains("OFFSET") || sqlQuery.contains("LIMIT")) {
                result = utilityDAO.query(sqlQuery, -1, -1);
            }
            else {
                result = utilityDAO.query(sqlQuery, params.offset(), params.limit());
            }
        }
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched {} rows for query {}. Elapsed time(ms): {}"
                , result.size() - 1
                , sql
                , timer.elapsedTimeMillis());
        }
        if (result.size() > 1) {
            Object obj = result.get(1);
            if (! obj.getClass().isArray() && ! Collection.class.isAssignableFrom(obj.getClass())) {
                for (int i = 1; i < result.size(); i ++) {
                    result.set(i, new Object[] {result.get(i)});
                }
            }
        }
        return result;
    }
}
