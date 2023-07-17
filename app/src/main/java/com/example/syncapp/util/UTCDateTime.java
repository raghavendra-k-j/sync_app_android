package com.example.syncapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class UTCDateTime {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    static {
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        DATE_FORMATTER.setTimeZone(utcTimeZone);
        TIME_FORMATTER.setTimeZone(utcTimeZone);
        DATETIME_FORMATTER.setTimeZone(utcTimeZone);
    }

    @SuppressWarnings("unused")
    public static String getCurrentDate() {
        Date currentDate = new Date();
        return DATE_FORMATTER.format(currentDate);
    }

    @SuppressWarnings("unused")
    public static String getCurrentTime() {
        Date currentTime = new Date();
        return TIME_FORMATTER.format(currentTime);
    }

    public static String getCurrentDateTime() {
        Date currentDateTime = new Date();
        return DATETIME_FORMATTER.format(currentDateTime);
    }

    public static String toDeviceTime(String format, String utcTime) {
        try {
            SimpleDateFormat utcFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat deviceFormatter = new SimpleDateFormat(format, Locale.getDefault());
            deviceFormatter.setTimeZone(TimeZone.getDefault());

            Date utcDate = utcFormatter.parse(utcTime);
            return deviceFormatter.format(Objects.requireNonNull(utcDate));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
