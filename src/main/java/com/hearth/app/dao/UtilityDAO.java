package com.hearth.app.dao;

import java.util.Date;
import java.util.List;
import org.javalabs.jpa.annotation.Dao;
import org.javalabs.jpa.annotation.NotSupported;

/**
 * This is a utility data access object.
 *
 * <p>
 * It has several utility methods to extract useful info from the DB.
 *
 * @author Sudiptasish Chanda
 */
@Dao
public interface UtilityDAO {

    String DML_HINT = "/*+ iad */";
    String DDL_HINT = "/*+ cd */";

    /**
     * Return the current date of the database.
     *
     * @return Date
     */
    @NotSupported
    Date currentDate();

    /**
     * Return the current date of the database.
     *
     * @return Date
     */
    @NotSupported
    Object[] dbInfo();

    /**
     * Execute the sql query and return the response.
     *
     * @param sql Query to be executed.
     * @return List Result set.
     */
    @NotSupported
    List<Object[]> query(String sql);

    /**
     * Execute the sql query and return the response.
     *
     * @param sql    Query to be executed.
     * @param offset
     * @param limit
     * @return List Result set.
     */
    @NotSupported
    List<Object[]> query(String sql, int offset, int limit);

    /**
     * Execute a dml query.
     *
     * @param sql
     */
    int execute(String sql);
}
