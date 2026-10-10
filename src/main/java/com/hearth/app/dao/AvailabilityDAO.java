package com.hearth.app.dao;

import com.hearth.app.model.AvailTimeSlot;
import org.javalabs.jpa.annotation.Dao;
import org.javalabs.jpa.annotation.NotSupported;
import com.hearth.app.model.Availability;
import com.hearth.app.util.SearchCriteria;
import java.sql.Date;
import java.util.List;

/**
 * Data Access Object for Availability table.
 *
 * <p>
 * The DAO layer uses <code>jpa-lite</code> framework to manage persistence and
 * transaction. Invocation of a method in a DAO class will implicitly
 * start a transaction. If a method is annotated with {@link NotSupported}, then
 * no transaction will be started. But, if a client is still trying to make a database
 * write operation, it will fail. The transaction management is taken over by the
 * underlying jpa framework. If a database operation fails, then the current
 * {@link UserTransaction} will be marked us rollback-only.
 * 
 * @author Sudiptasish Chanda
 */
@Dao
public interface AvailabilityDAO {
    
    /**
     * Insert a new record in the designated table.
     * @param record    Availability entry to be created
     */
    void insert(Availability record);
    
    /**
     * A bulk insert method to create multiple records at once.
     * @param records 
     */
    void insert(List<Availability> records);
    
    /**
     * Update the entry in the DB.
     * 
     * Update operation will change the state of the record present in the table
     * with the one provided as an argument.
     * 
     * @param record    Availability entry to be updated
     */
    void update(Availability record);
    
    /**
     * Bulk update multiple records.
     * @param records 
     */
    void update(List<Availability> records);
    
    /**
     * Delete the specific entry from the database.
     * @param record    Availability entry to be deleted.
     */
    void delete(Availability record);
    
    /**
     * Retrieve the entry from the database, as identified by this primary key.
     * If no matching record is found in the DB, then this api will return null.
     * 
     * @param   pk    Primary key of the record to be fetched
     * @return  Availability
     */
    @NotSupported
    Availability find(Availability.AvailabilityPK pk);
    
    /**
     * 
     * @return 
     */
    Object[] findMinMaxDate();
    
    /**
     * Query all the entries from the underlying db.
     * This is an extension of the {@link #query(SearchCriteria)} method, just that this method
     * will return all the attributes associated with the record.
     * 
     * @param search  Query criteria.
     * @return List
     */
    @NotSupported
    List<Availability> query(SearchCriteria search);
    
    /**
     * Find the availability of slots for a given service and on a specific day.
     * 
     * @param serviceId
     * @param durationMin
     * @param date
     * @return 
     */
    @NotSupported
    List<AvailTimeSlot> findAvailability(Integer serviceId, Short durationMin, Date date);
    
    /**
     * Find the availability of slots for a given service and on a specific day.
     * 
     * @param serviceId
     * @param neighbourhoodId
     * @param startTime
     * @param endTime
     * @param date
     * @param takeLock
     * 
     * @return List
     */
    @NotSupported
    List<Availability> findProfessional(Integer serviceId
            , Integer neighbourhoodId
            , String date
            , String startTime
            , String endTime
            , Boolean takeLock);
    
    @NotSupported
    List<Availability> findProfAvailability(String query, List<Object> binds);
}

