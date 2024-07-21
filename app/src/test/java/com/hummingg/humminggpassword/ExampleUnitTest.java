package com.hummingg.humminggpassword;

import org.junit.Test;

import static org.junit.Assert.*;


import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ExampleUnitTest {
    @Test
    public void maximumSum() {
        // 日期时间字符串
        String dateString = "2021-07-21 03:02:15";

        // 创建 SimpleDateFormat 对象，并指定输入的日期时间格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 解析字符串为 Date 对象
        Date date = null;
        try {
            date = inputFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            // 获取系统默认时区
            TimeZone systemTimeZone = TimeZone.getDefault();
            System.out.println(systemTimeZone);

            // 创建 SimpleDateFormat 对象，并指定输出的日期时间格式
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 设置时区为系统默认时区
            outputFormat.setTimeZone(systemTimeZone);

            // 将 Date 对象格式化为系统时区的时间字符串
            String systemTimeZoneDateString = outputFormat.format(date);

            // 打印结果
            System.out.println("系统时区的时间字符串: " + systemTimeZoneDateString);
        }
    }

    @Test
    public void testGmt(){

        String eventTimeStr = "2024-07-21 03:02:15";
        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdfLocal.setTimeZone(TimeZone.getDefault());
        Date eventTime = null;
        try {
            eventTime = sdfUTC.parse(eventTimeStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String localTimeStr = sdfLocal.format(eventTime);
        System.out.println(localTimeStr);
    }
}
