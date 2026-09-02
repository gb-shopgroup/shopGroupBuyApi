package cn.com.shopgroup.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// 时间工具类
public class TimeUtils {

    // 日期格式
    private final static String pattern = "yyyy-MM-dd HH:mm:ss";
    private final static String pattern2 = "yyMMddHHmmss";

    // 获取Date类
    public static Date getDataFromStr(String dateTime){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    // 秒级时间戳转日期
    public static String getFormatTimeStamp(int timestamp){

        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    // 日期转秒级时间戳
    public static int toFormatTimeStamp(String dateTime){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            date = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        return (int)(date.getTime() / 1000);
    }

    // 获取秒级时间戳
    public static int getTimeStamp() {

        return (int)(System.currentTimeMillis() / 1000L);
    }

    // 获取今天日期 "yyyy-MM-dd" 格式
    public static String getToday(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    // 获取今天日期 “yyyyMMdd” 格式
    public static String getTodayStr(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }

    // 获取当前时间 "yyyy-MM-dd HH:mm:ss" 格式
    public static String getNowTime() {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    // 获取当前时间 "yyyy-MM-dd HH:mm:ss" 格式
    public static String getNowTimeStr() {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern2);
        return sdf.format(new Date());
    }

    // 获取当前时间
    public static String getNowTimeStr2(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        // 设置东八时区
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = sdf.format(new Date());
        return time;
    }

    // 只获取当前月份
    public static String getNowMonth(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(new Date());
    }

    // 获取当天凌晨时间戳
    public static int getTodayTimeStamp(){

        Calendar cal = Calendar.getInstance();
        // 时、分、秒、毫秒置0
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // 毫秒转秒
        Long todayZeroSecond = cal.getTimeInMillis() / 1000;
        return todayZeroSecond.intValue();
    }

    /**
     * 相对时间描述
     * @param timestamp 秒级时间戳
     * @return 刚刚、N分钟前、N小时前、N天前，超过30天返回 yyyy-MM-dd HH:mm
     */
    public static String getRelativeTime(int timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        long now = System.currentTimeMillis() / 1000L;
        long diff = now - timestamp;
        if (diff < 0) {
            diff = 0;
        }
        if (diff < 60) {
            return "刚刚";
        }
        if (diff < 3600) {
            return (diff / 60) + "分钟前";
        }
        if (diff < 86400) {
            return (diff / 3600) + "小时前";
        }
        if (diff < 30 * 86400L) {
            return (diff / 86400) + "天前";
        }
        return getFormatTimeStamp(timestamp);
    }


}
