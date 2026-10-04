package com.hearth.app.bo;

import org.javalabs.decl.util.StopWatch;
import org.javalabs.jpa.DAOProxy;
import com.hearth.app.auth.AppUser;
import com.hearth.app.model.Province;
import com.hearth.app.util.QueryParams;
import com.hearth.app.util.SearchCriteria;
import java.sql.Timestamp;
import java.util.List;
import org.javalabs.decl.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hearth.app.dao.ProvinceDAO;

/**
 *
 * @author schan280
 */
public class ProvinceBO extends AbstractBO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceBO.class);
    
    private final ProvinceDAO provinceDAO;

    public ProvinceBO() {
        this.provinceDAO = DAOProxy.get(ProvinceDAO.class);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized ProvinceBO: {}. ProvinceDAO: {}", getClass().getSimpleName(), provinceDAO);
        }
    }

    public Province create(AppUser usr, Province province) throws IllegalAccessException {
        // Only admin has the privilege to register a province.
        ensureAdmin(usr);
        validateScope(usr, "province:create");
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        if (province.getCreatedAt() == null) {
            province.setCreatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
        }
        
        provinceDAO.insert(province);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("OperatingProvince created successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return province;
    }

    public void create(AppUser usr, List<Province> records) throws IllegalAccessException {
        // Only admin has the privilege to register a province.
        ensureAdmin(usr);
        validateScope(usr, "province:create");
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        for (Province province : records) {
            if (province.getCreatedAt() == null) {
                province.setCreatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
            }
        }
        
        provinceDAO.insert(records);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Created {} OperatingProvince record(s) successfully. Elapsed time(ms): {}", records.size(), timer.elapsedTimeMillis());
        }
    }

    public Province modify(AppUser usr, Province province) throws IllegalAccessException {
        // Only admin has the privilege to register a province.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        Province existing = provinceDAO.find(new Province.ProvincePK(province.getProvinceId()));
        if (existing == null) {
            throw new IllegalArgumentException("No province found for identifier: " + province.getProvinceId());
        }
        // Update attributes of existing record
        existing.setCountryId(province.getCountryId());
        existing.setProvinceName(province.getProvinceName());
        existing.setRegion(province.getRegion());
        existing.setLanguage(province.getLanguage());
        existing.setLaunchedAt(province.getLaunchedAt());
        existing.setStatus(province.getStatus());
        existing.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));

        provinceDAO.update(existing);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("OperatingProvince record modified successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return existing;
    }
    
    public Province mmodifyPartial(AppUser usr, Province province) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        Province existing = provinceDAO.find(new Province.ProvincePK(province.getProvinceId()));
        if (existing == null) {
            throw new IllegalArgumentException("No province found for identifier: " + province.getProvinceId());
        }
        // Update attributes of existing record
        if (province.getCountryId() != null) {
            existing.setCountryId(province.getCountryId());
        }
        if (province.getProvinceName() != null) {
            existing.setProvinceName(province.getProvinceName());
        }
        if (province.getRegion() != null) {
            existing.setRegion(province.getRegion());
        }
        if (province.getLanguage() != null) {
            existing.setLanguage(province.getLanguage());
        }
        if (province.getStatus() != null) {
            existing.setStatus(province.getStatus());
        }
        if (province.getLaunchedAt() != null) {
            existing.setLaunchedAt(province.getLaunchedAt());
        }
        existing.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));

        provinceDAO.update(existing);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Partial modification of province is successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return existing;
    }

    public List<Province> viewAll(AppUser usr, QueryParams params) {
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        if (! params.contains("status")) {
            params.entries().put("status", List.of(Province.Status.ACTIVE.name()));
        }
        SearchCriteria search = SearchCriteria.from(params);
        List<Province> rows = provinceDAO.query(search);

        timer.stop();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched {} expanded province record(s). Elapsed time(ms): {}", rows.size(), timer.elapsedTimeMillis());
        }
        return rows;
    }

    public Province view(AppUser usr, Integer id) {
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        Province province = provinceDAO.find(new Province.ProvincePK(id));
        if (province == null) {
            throw new IllegalArgumentException("No OperatingProvince found for id: " + id);
        }
        timer.stop();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched province details. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return province;
    }

    public Province remove(AppUser usr, Integer id) throws IllegalAccessException {
        // Only admin has the privilege to register a province.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        Province province = provinceDAO.find(new Province.ProvincePK(id));

        if (province == null) {
            throw new IllegalArgumentException("No province found for id: " + id);
        }
        provinceDAO.delete(province);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleted OperatingProvince. Id: {}. Elapsed time(ms): {}", id, timer.elapsedTimeMillis());
        }
        return province;
    }
}
