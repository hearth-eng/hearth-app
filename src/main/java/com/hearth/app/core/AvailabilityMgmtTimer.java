package com.hearth.app.core;

import com.hearth.app.event.AvailabilityGenEvent;
import com.hearth.app.util.Constants;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Booking timer of the application.
 *
 * @author schan280
 */
public class AvailabilityMgmtTimer implements Handler<Long> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityMgmtTimer.class);

    public AvailabilityMgmtTimer() {}
    
    @Override
    public void handle(Long id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time {} is invoked", getClass().getSimpleName());
        }
        // Add repetitive task ...
        AvailabilityGenEvent event = AvailabilityGenEvent.from(Map.of("numberOfDays", 1));
        event.setStrict(Boolean.TRUE);
        
        // 1. Get the current active context
        Context context = Vertx.currentContext();
        if (context != null) {
            // 2. Retrieve the Vertx instance that owns this context
            Vertx currentVertx = context.owner();
            currentVertx.eventBus().send(Constants.AVAIL_GEN_ADDRESS, event);
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Sent daily event to generate availability calendar");
            }
        }
        else {
            LOGGER.warn("CurrentContext is NULL");
        }
    }
}
