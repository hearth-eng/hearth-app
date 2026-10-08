package com.hearth.app.core;

import com.hearth.app.bo.BookingMgmtBO;
import com.hearth.app.model.Booking;
import io.vertx.core.Handler;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Booking timer of the application.
 *
 * @author schan280
 */
public class BookingTimer implements Handler<Long> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingTimer.class);

    private final BookingMgmtBO bookingMgmtBO;

    public BookingTimer() {
        this.bookingMgmtBO = new BookingMgmtBO();
    }
    
    @Override
    public void handle(Long event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Time {} is invoked", getClass().getSimpleName());
        }
        // Add repetitive task ...
        List<Booking> bookings = bookingMgmtBO.pendingBookings();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fetched {} pending booking(s)", bookings.size());
        }
        for (Booking booking : bookings) {
            try {
                booking.setUpdatedBy("System - BookingTimer");
                bookingMgmtBO.assignProfessional(booking);
            }
            catch (RuntimeException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
    
}
