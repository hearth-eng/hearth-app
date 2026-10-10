package com.hearth.app.dao;

import com.hearth.app.util.SearchCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.javalabs.jpa.query.Criteria;

/**
 * Base Data Access Object (DAO) providing common functionality for
 * constructing database queries from {@link SearchCriteria}.
 *
 * <p>The generated query supports:
 * 
 * <ul>
 *     <li>Selecting all or a specified set of columns.</li>
 *     <li>Filtering columns using equality ({@code =}) for single values.</li>
 *     <li>Filtering columns using {@code IN} for multiple values.</li>
 *     <li>Ignoring search parameters containing no values.</li>
 *     <li>Ordering results by a specified column.</li>
 *     <li>Ascending or descending sort order.</li>
 * </ul>
 *
 * <p>If no ordering column is specified in the search criteria,
 * {@code created_at} is used as the default.
 *
 * @author schan280
 */
public abstract class AbstractDAO {
    
    /**
     * Default list of columns used when all columns should be selected.
     */
    private static final List<String> ALL = Arrays.asList("*");
    
    private static final String NOT_NULL = "not_null";
    
    protected static final String PU_NAME = "hearth-app-pu";
        
    /**
     * Builds a query for the specified table using all available columns.
     *
     * <p>This is a convenience method equivalent to calling: {@link #getQuery(java.lang.String, java.util.List, com.hearth.app.util.SearchCriteria) }
     *
     * @param table  The database table to query
     * @param search The search criteria containing filter and ordering information
     * @return  The constructed {@link Criteria} query
     */
    protected Criteria getQuery(String table, SearchCriteria search) {
        return getQuery(table, search.fields(), search);
    }
    
    /**
     * Builds a query for the specified table using the supplied columns and search criteria.
     *
     * <p>Each entry in {@link SearchCriteria#params()} represents a column and the values that should be used to
     * filter that column.
     *
     * <p>Filtering is performed as follows:</p>
     * <ul>
     *     <li>An empty value list is ignored.</li>
     *     <li>A single value produces an equality condition.</li>
     *     <li>Multiple values produce an {@code IN} condition.</li>
     *     <li>Multiple column conditions are combined using {@code AND}.</li>
     * </ul>
     *
     * <p>For example, search parameters equivalent to:
     *
     * <pre>
     * status  = ["ACTIVE"]
     * country = ["US", "CA"]
     * </pre>
     *
     * <p>would conceptually produce:</p>
     *
     * <pre>
     * SELECT *
     * FROM table
     * WHERE status = 'ACTIVE'
     *   AND country IN ('US', 'CA')
     * ORDER BY created_at;
     * </pre>
     *
     * <p>If {@code columns} is {@code null}, all columns ({@code *}) are
     * selected. If {@link SearchCriteria#orderBy()} is {@code null},
     * results are ordered by {@code created_at}. Descending order is applied
     * when {@link SearchCriteria#asc()} returns {@code false}.</p>
     *
     * @param table   The database table to query
     * @param columns The columns to select, or {@code null} to select all columns
     * @param search  The search criteria containing filters and ordering information
     * @return  The constructed {@link Criteria} query
     */
    protected Criteria getQuery(String table, List<String> columns, SearchCriteria search) {
        Criteria query = new Criteria()
                .select(columns != null && ! columns.isEmpty() ? columns : ALL)
                .from(table);

        String operator = search.operator();
        int idx = 0;
        for (Map.Entry<String, List<Object>> me : search.params().entrySet()) {
            String col = me.getKey();
            List<Object> vals = me.getValue();
            
            if (vals == null || vals.isEmpty()) {
                continue;
            }
            if (idx == 0) {
                if (vals.size() > 1) {
                    query.where(col).in(vals);
                }
                else {
                    // A special value to indicate if a column is not null.
                    if (NOT_NULL.equalsIgnoreCase(String.valueOf(vals.get(0)))) {
                        query.where(col).isNotNull();
                    }
                    else {
                        query.where(col).eq(vals.get(0));
                    }
                }
                idx ++;
            }
            else {
                if (vals.size() > 1) {
                    if ("or".equals(operator)) {
                        query.or(col).in(vals);
                    }
                    else {
                        query.and(col).in(vals);
                    }
                }
                else {
                    // A special value to indicate if a column is not null.
                    if (NOT_NULL.equalsIgnoreCase(String.valueOf(vals.get(0)))) {
                        if ("or".equals(operator)) {
                            query.or(col).isNotNull();
                        }
                        else {
                            query.and(col).isNotNull();
                        }
                    }
                    else {
                        if ("or".equals(operator)) {
                            query.or(col).eq(vals.get(0));
                        }
                        else {
                            query.and(col).eq(vals.get(0));
                        }
                    }
                }
            }
        }
        query.orderBy(search.orderBy() != null ? search.orderBy() : "created_at");
        if (! search.asc()) {
            query.desc();
        }
        return query;
    }
}
