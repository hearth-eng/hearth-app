package com.hearth.app.bo;

import org.javalabs.decl.util.StopWatch;
import org.javalabs.jpa.DAOProxy;
import com.hearth.app.auth.AppUser;
import com.hearth.app.model.City;
import com.hearth.app.util.QueryParams;
import com.hearth.app.util.SearchCriteria;
import java.sql.Timestamp;
import java.util.List;
import org.javalabs.decl.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hearth.app.dao.CityDAO;

/**
 *
 * @author schan280
 */
public class CityBO extends AbstractBO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CityBO.class);
    
    private final CityDAO cityDAO;

    public CityBO() {
        this.cityDAO = DAOProxy.get(CityDAO.class);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized CityBO: {}. CityDAO: {}", getClass().getSimpleName(), cityDAO);
        }
    }

    public City create(AppUser usr, City city) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        validateScope(usr, "city:create");
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        city.setStatus(City.Status.PLANNED);
        if (city.getCreatedAt() == null) {
            city.setCreatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
        }
        
        cityDAO.insert(city);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("OperatingCity created successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return city;
    }

    public void create(AppUser usr, List<City> records) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        validateScope(usr, "city:create");
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();
        
        for (City city : records) {
            city.setStatus(City.Status.PLANNED);
            if (city.getCreatedAt() == null) {
                city.setCreatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));
            }
        }
        
        cityDAO.insert(records);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Created {} OperatingCity record(s) successfully. Elapsed time(ms): {}", records.size(), timer.elapsedTimeMillis());
        }
    }

    public City modify(AppUser usr, City city) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        City existing = cityDAO.find(new City.CityPK(city.getCityId()));
        if (existing == null) {
            throw new IllegalArgumentException("No city found for identifier: " + city.getCityId());
        }
        // Update attributes of existing record
        existing.setCityName(city.getCityName());
        existing.setImageKey(city.getImageKey());
        existing.setLaunchedAt(city.getLaunchedAt());
        existing.setStatus(city.getStatus());
        existing.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));

        cityDAO.update(existing);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("OperatingCity record modified successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return existing;
    }
    
    public City mmodifyPartial(AppUser usr, City city) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        City existing = cityDAO.find(new City.CityPK(city.getCityId()));
        if (existing == null) {
            throw new IllegalArgumentException("No city found for identifier: " + city.getCityId());
        }
        // Update attributes of existing record
        if (city.getCityName() != null) {
            existing.setCityName(city.getCityName());
        }
        if (city.getImageKey() != null) {
            existing.setImageKey(city.getImageKey());
        }
        if (city.getLaunchedAt() != null) {
            existing.setLaunchedAt(city.getLaunchedAt());
        }
        if (city.getStatus() != null) {
            existing.setStatus(city.getStatus());
        }
        existing.setUpdatedAt(new Timestamp(DateUtil.currentUTCDate().getTime()));

        cityDAO.update(existing);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Partial modification of city is successfully. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return existing;
    }

    public List<City> viewAll(AppUser usr, QueryParams params) {
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        if (! params.contains("status")) {
            params.entries().put("status", List.of(City.Status.ACTIVE.name()));
        }
        SearchCriteria search = SearchCriteria.from(params);
        List<City> rows = cityDAO.query(search);

        timer.stop();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched {} expanded city record(s). Elapsed time(ms): {}", rows.size(), timer.elapsedTimeMillis());
        }
        return rows;
    }

    public City view(AppUser usr, Integer id) {
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        City city = cityDAO.find(new City.CityPK(id));
        if (city == null) {
            throw new IllegalArgumentException("No OperatingCity found for id: " + id);
        }
        timer.stop();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fetched city details. Elapsed time(ms): {}", timer.elapsedTimeMillis());
        }
        return city;
    }

    public City remove(AppUser usr, Integer id) throws IllegalAccessException {
        // Only admin has the privilege to register a city.
        ensureAdmin(usr);
        
        StopWatch timer = StopWatch.newTimer();
        timer.start();

        // First fetch the entry, to see if this already exists.
        City city = cityDAO.find(new City.CityPK(id));

        if (city == null) {
            throw new IllegalArgumentException("No city found for id: " + id);
        }
        cityDAO.delete(city);
        timer.stop();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleted OperatingCity. Id: {}. Elapsed time(ms): {}", id, timer.elapsedTimeMillis());
        }
        return city;
    }
}
