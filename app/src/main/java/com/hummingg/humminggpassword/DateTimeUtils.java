package com.hummingg.humminggpassword;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {
    public static String getLocalTime(String gmtStr){
        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfLocal.setTimeZone(TimeZone.getDefault());
        Date eventTime = null;
        try {
            eventTime = sdfUTC.parse(gmtStr);
        } catch (ParseException e) {
            return e.getMessage();
        }
        String localTimeStr = sdfLocal.format(eventTime);
        return localTimeStr;
    }
}
