package com.hearth.app.dao;

import com.hearth.app.model.Availability;
import org.javalabs.jpa.query.Criteria;
import com.hearth.app.model.Booking;
import com.hearth.app.model.User;
import com.hearth.app.util.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.javalabs.decl.util.DateUtil;
import org.javalabs.jpa.annotation.Dao;
import org.javalabs.jpa.query.CriteriaUpdate;
import org.javalabs.jpa.util.QueryHints;

/**
 * Concrete DAO class to handle database operations related.
 *
 * @author Sudiptasish Chanda
 */
public class BookingDAOImpl extends AbstractDAO implements BookingDAO {
    
    private final String TABLE = "fks_bookings";
    
    @Dao
    private AvailabilityDAO availabilityDAO;
    
    @PersistenceContext(name = PU_NAME)
    private EntityManager em;
    
    @Override
    public void insert(Booking record) {
        insert(Arrays.asList(record));
    }

    @Override
    public void insert(List<Booking> records) {
        for (Booking record : records) {
            em.persist(record);
        }
    }

    @Override
    public void update(Booking record) {
        update(Arrays.asList(record));
    }

    @Override
    public void update(List<Booking> records) {
        for (Booking record : records) {
            em.merge(record);
        }
    }

    @Override
    public void delete(Booking record) {
        em.remove(record);
    }

    @Override
    public Booking find(Booking.BookingPK pk) {
        return em.find(Booking.class, pk);
    }
    
    @Override
    public int updateStatus(Booking booking) {
        CriteriaUpdate criteria = new CriteriaUpdate()
                .update(TABLE)
                .set("status").eq(booking.getStatus())
                .set("status_msg").eq(booking.getStatusMsg())
                .set("updated_at").eq(booking.getUpdatedAt())
                .set("updated_by").eq(booking.getUpdatedBy())
                .where("booking_id").eq(booking.getBookingId());
        
        Query q = em.createQuery(criteria.toQuery());
        List<Object> binds = criteria.params();
        
        int idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        return q.executeUpdate();
    }
    
    @Override
    public List<Booking> findAllByCustomer(Integer customerId) {
        return em.createNamedQuery("Booking.queryByCustomer", Booking.class)
                .setParameter(1, User.Role.PROFESSIONAL)
                .setParameter(2, customerId)
                .setHint(QueryHints.POPULATE_RESULT_COLUMN, Boolean.TRUE)
                .getResultList();
    }
    
    @Override
    public List<Booking> findAllByProfessional(Integer professionalId) {
        return em.createNamedQuery("Booking.queryByProfessional", Booking.class)
                .setParameter(1, User.Role.CUSTOMER)
                .setParameter(2, professionalId)
                .setHint(QueryHints.POPULATE_RESULT_COLUMN, Boolean.TRUE)
                .getResultList();
    }

    @Override
    public List<Booking> query(SearchCriteria search) {
        Criteria query = getQuery(TABLE, search);

        TypedQuery q = em.createNativeQuery(query.toQuery(), Booking.class);
        List<Object> binds = query.params();
        
        int idx = 1;
        for (Object bind : binds) {
            q.setParameter(idx ++, bind);
        }
        q.setFirstResult(search.offset());
        q.setMaxResults(search.limit());
        
        List<Booking> result = q.getResultList();
        return result;
    }
    
    @Override
    public Boolean assignProfessional(Booking booking) {
        Timestamp date = booking.getScheduledAt();
        String start = booking.getTimeSlot().split(" - ")[0];
        String end = booking.getTimeSlot().split(" - ")[1];
        
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTime(date);
        
        List<Availability> availabilities = availabilityDAO.findProfessional(
                booking.getServiceId()
                , booking.getNeighbourhoodId()
                , String.valueOf(cal.get(Calendar.YEAR))
                        + "-" + String.format("%02d", (cal.get(Calendar.MONTH) + 1))
                        + "-" + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
                , start
                , end
                , Boolean.TRUE);
        
        if (! availabilities.isEmpty()) {
            booking.setProfessionalId(availabilities.get(0).getProfessionalId());
            booking.setStatus(Booking.Status.CONFIRMED);
            booking.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
            booking.setUpdatedBy(booking.getUpdatedBy());
            
            em.merge(booking);

            for (Availability availability : availabilities) {
                availability.setIsBooked((short)1);
                availability.setUpdatedAt(booking.getUpdatedAt());
            }
            availabilityDAO.update(availabilities);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    @Override
    public Boolean freeProfessional(Booking booking, SearchCriteria search) {
        List<Availability> availabilities = availabilityDAO.query(search);
        
        if (! availabilities.isEmpty()) {
            for (Availability availability : availabilities) {
                availability.setIsBooked((short)0);
                booking.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
            }
            availabilityDAO.update(availabilities);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<Booking> pendingBooking() {
        return em.createNamedQuery("Booking.pendingBookings", Booking.class)
            .setParameter(1, Booking.Status.PENDING)
            .setHint(QueryHints.ALLOW_NATIVE_QUERY, Boolean.TRUE)
            .getResultList();
    }
    
}
