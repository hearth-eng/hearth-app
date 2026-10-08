package com.hearth.app.util;

import java.util.regex.Pattern;

public class Constants {

    public static final Pattern MOBILENUM_PATTERN = Pattern.compile("\\d{10}");

    public static final long OTP_EXPIRY_TIME_MSEC = 300000; //5 mins

    // Viewing records from db
    public static final int DEFAULT_SEARCH_LIMIT = 1000;

    // Address
    public static final String DEFAULT_LABEL = "HOME";
    public static final short IS_DEFAULT_ADDR = 1;

    // Document
    public static final String  DEFAULT_DOC_TYPE = "AADHAAR";

    // PROFESSIONAL
    public static final short PROF_NOT_VERIFIED = 0;

    // PROFESSIONAL SERVICE
    public static final short PROF_SERVICE_ACTIVE = 1;

    public static final String BOOKING_ADDRESS = "booking.event.address";
    public static final String AVAIL_GEN_ADDRESS = "availability.gen.address";
    public static final String PROF_REG_ADDRESS = "professional.reg.address";
    public static final String BROADCAST_ADDRESS = "broadcast.event.address";
    
    public static final String CACHE_CHANNEL = "cache:ops";
}

