package com.hearth.app.dao;

import com.hearth.app.model.AvailTimeSlot;
import org.javalabs.jpa.query.Criteria;
import com.hearth.app.model.Availability;
import com.hearth.app.util.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.javalabs.jpa.util.QueryHints;

/**
 * Concrete DAO class to handle database operations related.
 *
 * @author Sudiptasish Chanda
 */
public class AvailabilityDAOImpl extends AbstractDAO implements AvailabilityDAO {
    
    private final String TABLE = "fks_availabilities";
    
    @PersistenceContext(name = PU_NAME)
    private EntityManager em;
    
    private final AvailabilityQueryGen queryGen = new AvailabilityQueryGen();
    
    @Override
    public void insert(Availability record) {
        insert(Arrays.asList(record));
    }

    @Override
    public void insert(List<Availability> records) {
        for (Availability record : records) {
            em.persist(record);
        }
    }

    @Override
    public void update(Availability record) {
        update(Arrays.asList(record));
    }

    @Override
    public void update(List<Availability> records) {
        for (Availability record : records) {
            em.merge(record);
        }
    }

    @Override
    public void delete(Availability record) {
        em.remove(record);
    }

    @Override
    public Availability find(Availability.AvailabilityPK pk) {
        return em.find(Availability.class, pk);
    }

    @Override
    public Object[] findMinMaxDate() {
        Criteria query = new Criteria()
                .select("COALESCE(MIN(date), CURRENT_DATE - 1)", "COALESCE(MAX(date), CURRENT_DATE - 1)")
                .from(TABLE);
        
        return (Object[])em.createNativeQuery(query.toQuery()).getSingleResult();
    }

    @Override
    public List<Availability> query(SearchCriteria search) {
        Criteria query = new Criteria()
                .select(Arrays.asList("*"))
                .from(TABLE);

        int idx = 0;
        for (Map.Entry<String, List<Object>> me : search.params().entrySet()) {
            String col = me.getKey();
            List<Object> vals = me.getValue();
            if (vals.isEmpty()) {
                continue;
            }
            if (idx == 0) {
                if (vals.size() > 1) {
                    query.where(col).in(vals);
                }
                else {
                    if (col.equals("start_time")) {
                        query.where(col).gte(vals.get(0));
                    }
                    else if (col.equals("end_time")) {
                        query.where(col).lte(vals.get(0));
                    }
                    else {
                        query.where(col).eq(vals.get(0));
                    }
                }
                idx ++;
            }
            else {
                if (vals.size() > 1) {
                    query.and(col).in(vals);
                }
                else {
                    if (col.equals("start_time")) {
                        query.and(col).gte(vals.get(0));
                    }
                    else if (col.equals("end_time")) {
                        query.and(col).lte(vals.get(0));
                    }
                    else {
                        query.and(col).eq(vals.get(0));
                    }
                }
            }
        }
        if (search.orderBy() != null) {
            query.orderBy(search.orderBy());
        }
        if (! search.asc()) {
            query.desc();
        }

        TypedQuery<Availability> q = em.createNativeQuery(query.toQuery(), Availability.class);
        List<Object> binds = query.params();
        
        idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        q.setFirstResult(search.offset());
        q.setMaxResults(search.limit());
        
        List<Availability> result = q.getResultList();
        return result;
    }

    @Override
    public List<AvailTimeSlot> findAvailability(Integer serviceId, Short durationMin, Date date) {
        String query = queryGen.availabilityQuery(serviceId, durationMin, date);
        
        Query q = em.createNativeQuery(query);
        List<Object> binds = List.of(1, date, 0, serviceId);
        
        Integer idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        List<Object[]> result = q.getResultList();
        
        List<AvailTimeSlot> list = new ArrayList<>(result.size());
        for (Object[] row : result) {
            list.add(new AvailTimeSlot((Time)row[0], (Time)row[1], (Integer)row[2], null));
        }
        
        return list;
    }

    @Override
    public List<Availability> findProfessional(Integer serviceId
            , Integer neighbourhoodId
            , String date
            , String startTime
            , String endTime
            , Boolean takeLock) {
        
        List<Availability> availabilities = internalFind(
                serviceId
                , neighbourhoodId
                , date
                , startTime
                , endTime
                , takeLock
                , Boolean.TRUE);
        
        if (availabilities.isEmpty()) {
            availabilities = internalFind(serviceId
                    , neighbourhoodId
                    , date
                    , startTime
                    , endTime
                    , takeLock
                    , Boolean.FALSE);
        }
        return availabilities;
    }
    
    private List<Availability> internalFind(Integer serviceId
            , Integer neighbourhoodId
            , String date
            , String startTime
            , String endTime
            , Boolean takeLock
            , Boolean fair) {
        
        String query = queryGen.matchingProfessionalQuery(takeLock, fair);
        
        TypedQuery<Availability> q = em.createNativeQuery(query, Availability.class);
        List<Object> binds = new ArrayList<>(List.of(date, startTime, endTime, 1, neighbourhoodId, serviceId, 0));
        
        if (fair) {
            binds.add(1);
        }
        
        Integer idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        return q.getResultList();
    }
    
    @Override
    public List<Availability> findProfAvailability(String query, List<Object> binds) {
        TypedQuery<Availability> q = em.createNamedQuery(query, Availability.class)
                .setHint(QueryHints.POPULATE_RESULT_COLUMN, Boolean.TRUE);
        
        Integer idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        return q.getResultList();
    }
    
}
