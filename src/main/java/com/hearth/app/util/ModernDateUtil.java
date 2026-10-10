package com.hearth.app.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 *
 * @author schan280
 */
public class ModernDateUtil {

    public static long calculateDiffInMillis(int hour, int minute) {
        // 1. Get the current system wall-clock date and time (no timezone offset)
        LocalDateTime now = LocalDateTime.now();

        // 2. Set the target time on today's date
        LocalDateTime target = now.with(LocalTime.of(hour, minute, 0, 0));

        // 3. If 12:10 AM has already passed today, shift the target to tomorrow
        if (now.isAfter(target)) {
            target = target.plusDays(1);
        }

        // 4. Return the exact millisecond difference
        return Duration.between(now, target).toMillis();
    }
}
