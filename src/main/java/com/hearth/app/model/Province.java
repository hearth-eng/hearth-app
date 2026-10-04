package com.hearth.app.model;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;


/**
 * This class is auto generated with jpa-lite framework.
 *
 * @author Sudiptasish Chanda
 */

@Entity
@Table(name = "fks_provinces")
@IdClass(Province.ProvincePK.class)
@NamedNativeQueries({
    @NamedNativeQuery(name = "Province.selectAll", query = "SELECT * FROM fks_provinces")
})
public class Province implements Serializable, Cloneable {

    public static enum Status {
        PLANNED,
        ACTIVE,
        PAUSED,
        INACTIVE;
    };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "province_id", nullable = false, updatable = false, precision = 32)
    private Integer provinceId;

    @Column(name = "country_id", nullable = false, updatable = true, precision = 32)
    private Integer countryId;

    @Column(name = "province_name", nullable = true, updatable = true, length = 128)
    private String provinceName;

    @Column(name = "region", nullable = true, updatable = true, length = 32)
    private String region;

    @Column(name = "language", nullable = false, updatable = true, length = 30)
    private String language;

    @Column(name = "status", nullable = false, updatable = true, check = @CheckConstraint(constraint = "status IN ('PLANNED', 'ACTIVE', 'PAUSED', 'INACTIVE')"))
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "launched_at", nullable = true, updatable = true)
    private Date launchedAt;

    @Column(name = "created_at", nullable = false, updatable = true)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = true, updatable = true)
    private Timestamp updatedAt;

    public Province() {}

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getProvinceId() {
        return this.provinceId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getCountryId() {
        return this.countryId;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceName() {
        return this.provinceName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return this.region;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getLaunchedAt() {
        return launchedAt;
    }

    public void setLaunchedAt(Date launchedAt) {
        this.launchedAt = launchedAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getUpdatedAt() {
        return this.updatedAt;
    }

    public static class ProvincePK {

        private Integer provinceId;

        public ProvincePK() {}

        public ProvincePK(Integer provinceId) {
            this.provinceId = provinceId;
        }

        public void setProvinceId(Integer provinceId) {
            this.provinceId = provinceId;
        }

        public Integer getProvinceId() {
            return this.provinceId;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.provinceId);
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
            final ProvincePK other = (ProvincePK)obj;
            if (! Objects.equals(this.provinceId, other.provinceId)) {
                return false;
            }
            return true;
        }

    }
}