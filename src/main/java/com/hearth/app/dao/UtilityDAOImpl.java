package com.hearth.app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import org.javalabs.jpa.dialect.SQLDialect;

/**
 *
 * @author schan280
 */
public class UtilityDAOImpl extends AbstractDAO implements UtilityDAO {

    private static final String COL_MD_QUERY
            = "\nSELECT column_name, ordinal_position, is_nullable, data_type"
            + "\n  FROM information_schema.columns"
            + "\n WHERE table_schema = ?"
            + "\n   AND table_name = ?";

    @PersistenceContext(name = PU_NAME)
    private EntityManager em;

    @Override
    public Date currentDate() {
        SQLDialect dialect = (SQLDialect) em.getProperties().get("dialect");

        Object time = em
                .createNativeQuery(dialect.timestamp())
                .getSingleResult();

        if (time instanceof Timestamp) {
            return new Date(((Timestamp) time).getTime());
        } else if (time instanceof OffsetDateTime) {
            return new Date(((OffsetDateTime) time).toEpochSecond());
        } else {
            throw new IllegalArgumentException("Unknown timestamp data type: " + time.getClass());
        }
    }

    @Override
    public Object[] dbInfo() {
        SQLDialect dialect = (SQLDialect) em.getProperties().get("dialect");

        return (Object[]) em
                .createNativeQuery(dialect.version())
                .getSingleResult();
    }

    @Override
    public List<Object[]> query(String sql) {
        return query(sql, 0, 0);
    }

    @Override
    public List<Object[]> query(String sql, int offset, int limit) {
        Query query = em.createNativeQuery(sql)
                .setHint("fetch.table.metadata", "true");

        if (offset != 0 && limit != 0) {
            query.setFirstResult(offset)
                    .setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int execute(String sql) {
        return em.createNativeQuery(sql)
                .executeUpdate();
    }
}
