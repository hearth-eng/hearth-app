package com.hearth.app.core;

import com.hearth.app.config.ApplicationConfiguration;
import com.hearth.app.event.AvailabilityEventConsumer;
import com.hearth.app.event.AvailabilityGenCodec;
import com.hearth.app.event.AvailabilityGenEvent;
import com.hearth.app.event.BookingEventConsumer;
import com.hearth.app.event.BookingCodec;
import com.hearth.app.event.BroadcastCodec;
import com.hearth.app.event.BroadcastEventConsumer;
import com.hearth.app.event.BroadcastEventWrapper;
import com.hearth.app.event.ProfessionalEventConsumer;
import com.hearth.app.event.ProfessionalRegCodec;
import com.hearth.app.event.ProfessionalRegEvent;
import com.hearth.app.listener.DistributedCache;
import com.hearth.app.model.Booking;
import com.hearth.app.util.Constants;
import com.hearth.app.util.ModernDateUtil;
import io.vertx.core.AbstractVerticle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class AppProcessor extends AbstractVerticle {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProcessor.class);

    // Name of this verticle.
    private static final String NAME = "AppProcessor";
    
    protected final List<Long> timerIds = new ArrayList<>(2);
    
    @Override
    public void start() throws Exception {
        initEventBus();
        initTimer();
        initSubscriber();
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Started Verticle: {}", NAME);
        }
    }
    
    /**
     * Initialize the event bus consumer.
     */
    private void initEventBus() {
        getVertx().eventBus().consumer(Constants.BOOKING_ADDRESS, new BookingEventConsumer());
        getVertx().eventBus().registerDefaultCodec(Booking.class, new BookingCodec());
        
        getVertx().eventBus().consumer(Constants.AVAIL_GEN_ADDRESS, new AvailabilityEventConsumer());
        getVertx().eventBus().registerDefaultCodec(AvailabilityGenEvent.class, new AvailabilityGenCodec());
        
        getVertx().eventBus().consumer(Constants.PROF_REG_ADDRESS, new ProfessionalEventConsumer());
        getVertx().eventBus().registerDefaultCodec(ProfessionalRegEvent.class, new ProfessionalRegCodec());
        
        getVertx().eventBus().consumer(Constants.BROADCAST_ADDRESS, new BroadcastEventConsumer());
        getVertx().eventBus().registerDefaultCodec(BroadcastEventWrapper.class, new BroadcastCodec());
        
    }
    
    private void initTimer() {
        Map<String, Object> config = ApplicationConfiguration.getInstance().get("timer.config");
        if (config == null) {
            config = new HashMap<>();
        }
        
        // Start booking timer.
        // This timer will periodically check if any pending booking exists, and if so, try to
        // assign a professional to it.
        long delay = 0L;
        long interval = (Integer)config.getOrDefault("booking.timer.interval.s", 60);
        
        Long timerId = getVertx().setPeriodic(delay, interval * 1000L, new BookingTimer());
        timerIds.add(timerId);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduled booking timer. Initial Delay: {}. Pause Time (s): {}", delay, interval);
        }
        
        // Start the availability calendar timer.
        // This timer will run every midnight at 12:10 and create a calendar entry for all the verified
        // professional in the availabilities table.
        delay = ModernDateUtil.calculateDiffInMillis(12, 10);
        interval = (Integer)config.getOrDefault("availability.timer.interval.s", 3600);
        
        timerId = getVertx().setPeriodic(delay, interval * 1000L, new AvailabilityMgmtTimer());
        timerIds.add(timerId);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduled availability calendar daily timer. Initial Delay: {}. Pause Time (s): {}", delay, interval);
        }
    }
    
    private void initSubscriber() {
        DistributedCache.get().init();
    }

    @Override
    public void stop() throws Exception {
        for (Long timerId : timerIds) {
            vertx.cancelTimer(timerId);
        }
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Stopped worker verticle {}", NAME);
        }
    }
}
