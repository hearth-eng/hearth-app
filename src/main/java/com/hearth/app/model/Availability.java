package com.hearth.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;
import org.javalabs.jpa.annotation.ResultColumn;


/**
 * This class is auto generated with jpa-lite framework.
 *
 * @author Sudiptasish Chanda
 */

@Entity
@Table(name = "fks_availabilities")
@IdClass(Availability.AvailabilityPK.class)
@NamedNativeQueries({
    @NamedNativeQuery(name = "Availability.selectAll", query = "SELECT * FROM fks_availabilities"),
    @NamedNativeQuery(name = "Availability.selectMinMaxDate", query = "SELECT MIN(date), MAX(date) FROM fks_availabilities"),
    
    @NamedNativeQuery(name = "Availability.findByDateAndService"
            , query = """
                    SELECT c.*, a.professional_id, b.full_name, b.phone1
                      FROM fks_professionals a
                     INNER JOIN fks_users b ON (a.user_id = b.user_id)
                     INNER JOIN fks_availabilities c ON (a.professional_id = c.professional_id AND c.date = ? AND is_booked = ?)
                     INNER JOIN fks_professional_services d ON (a.professional_id = d.professional_id AND d.service_id = ?)
                     WHERE EXISTS (
                           SELECT 1
                             FROM fks_professional_neighbourhoods e
                            INNER JOIN fks_neighbourhoods f ON e.neighbourhood_id = f.neighbourhood_id
                            WHERE e.professional_id = a.professional_id
                              AND f.city_id = ?)
                    """),
    @NamedNativeQuery(name = "Availability.findByDateServiceAndSlot"
            , query = """
                    SELECT c.*, a.professional_id, b.full_name, b.phone1
                      FROM fks_professionals a
                     INNER JOIN fks_users b ON (a.user_id = b.user_id)
                     INNER JOIN fks_availabilities c ON (a.professional_id = c.professional_id AND c.date = ? AND c.start_time = ? AND is_booked = ?)
                     INNER JOIN fks_professional_services d ON (a.professional_id = d.professional_id AND d.service_id = ?)
                     WHERE EXISTS (
                           SELECT 1
                             FROM fks_professional_neighbourhoods e
                            INNER JOIN fks_neighbourhoods f ON e.neighbourhood_id = f.neighbourhood_id
                            WHERE e.professional_id = a.professional_id
                              AND f.city_id = ?)
                    """),
    @NamedNativeQuery(name = "Availability.findByDateServiceAndNeighbourhood"
            , query = """
                    SELECT c.*, a.professional_id, b.full_name, b.phone1
                      FROM fks_professionals a
                     INNER JOIN fks_users b ON (a.user_id = b.user_id)
                     INNER JOIN fks_availabilities c ON (a.professional_id = c.professional_id AND c.date = ? AND is_booked = ?)
                     INNER JOIN fks_professional_services d ON (a.professional_id = d.professional_id AND d.service_id = ?)
                     INNER JOIN fks_professional_neighbourhoods e ON (a.professional_id = e.professional_id AND e.neighbourhood_id = ?)
                     INNER JOIN fks_neighbourhoods f ON (e.neighbourhood_id = f.neighbourhood_id)
                    """),
    @NamedNativeQuery(name = "Availability.findByDateServiceNeighbourhoodAndSlot"
            , query = """
                    SELECT c.*, a.professional_id, b.full_name, b.phone1
                      FROM fks_professionals a
                     INNER JOIN fks_users b ON (a.user_id = b.user_id)
                     INNER JOIN fks_availabilities c ON (a.professional_id = c.professional_id AND c.date = ? AND c.start_time = ? AND is_booked = ?)
                     INNER JOIN fks_professional_services d ON (a.professional_id = d.professional_id AND d.service_id = ?)
                     INNER JOIN fks_professional_neighbourhoods e ON (a.professional_id = e.professional_id AND e.neighbourhood_id = ?)
                     INNER JOIN fks_neighbourhoods f ON (e.neighbourhood_id = f.neighbourhood_id)
                    """)
})
public class Availability implements Serializable, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id", nullable = false, updatable = false, precision = 32)
    private Integer availabilityId;

    @Column(name = "professional_id", nullable = false, updatable = true, precision = 32)
    private Integer professionalId;

    @Column(name = "date", nullable = false, updatable = true)
    private Date date;

    @Column(name = "start_time", nullable = true, updatable = true)
    private Time startTime;

    @Column(name = "end_time", nullable = true, updatable = true)
    private Time endTime;

    @Column(name = "is_booked", nullable = false, updatable = true, precision = 16)
    private Short isBooked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = true, updatable = true)
    private Timestamp updatedAt;

    @Transient
    @ResultColumn(name = "full_name")
    private String fullName;

    @Transient
    @ResultColumn(name = "phone1")
    private String phone1;

    public Availability() {}

    public void setAvailabilityId(Integer availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Integer getAvailabilityId() {
        return this.availabilityId;
    }

    public void setProfessionalId(Integer professionalId) {
        this.professionalId = professionalId;
    }

    public Integer getProfessionalId() {
        return this.professionalId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getStartTime() {
        return this.startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Time getEndTime() {
        return this.endTime;
    }

    public void setIsBooked(Short isBooked) {
        this.isBooked = isBooked;
    }

    public Short getIsBooked() {
        return this.isBooked;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public static class AvailabilityPK {

        private Integer availabilityId;

        public AvailabilityPK() {}

        public AvailabilityPK(Integer availabilityId) {
            this.availabilityId = availabilityId;
        }

        public void setAvailabilityId(Integer availabilityId) {
            this.availabilityId = availabilityId;
        }

        public Integer getAvailabilityId() {
            return this.availabilityId;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.availabilityId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AvailabilityPK other = (AvailabilityPK)obj;
            if (! Objects.equals(this.availabilityId, other.availabilityId)) {
                return false;
            }
            return true;
        }

    }
}