package cn.carbs.android.gregorianlunarcalendar.library.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar;

public class Util {

    /**
     * 数字对应的汉字
     */
    public static final String[] lunarNumbers = {"零","一","二","三","四","五","六","七","八","九"};

    /**
     * 农历的月份
     */
    public static final String[] lunarMonths = {"一","二","三","四","五","六","七","八","九","十","十一","十二"};

    public static final String[] lunarDays = {	"初一","初二","初三","初四","初五","初六","初七","初八","初九","初十",
                                                "十一","十二","十三","十四","十五","十六","十七","十八","十九","廿十",
                                                "廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"};

    public static HashMap<Integer, String[]> twelveMonthWithLeapCache = new HashMap<Integer, String[]>();


    public static int getMonthLeapByYear(int year){
        return ChineseCalendar.getMonthLeapByYear(year);
    }

    /**
     * 通过月份的索引获取当月对应的天数
     * @param year
     * 			年份
     * @param monthSway
     * 			索引从1开始
     * @param isGregorian
     * 			是否是公历
     * @return
     * 			月份包含的天数
     */
    public static int getSumOfDayInMonth(int year, int monthSway, boolean isGregorian){
        if(isGregorian){
            return getSumOfDayInMonthForGregorianByMonth(year, monthSway);
        }else{
            return getSumOfDayInMonthForLunarByMonthSway(year, monthSway);
        }
    }

    /**
     * 获取公历year年month月的天数
     * @param year
     * 			年
     * @param month
     * 			月，从1开始计数
     * @return
     * 			月份包含的天数
     */
    public static int getSumOfDayInMonthForGregorianByMonth(int year, int month){
        return new GregorianCalendar(year, month, 0).get(Calendar.DATE);
    }

    /**
     * 获取农历year年monthSway月的天数
     * @param year
     * 			年
     * @param monthSway
     * 			月，包含闰月，如闰五月，monthSway为1代表1月，5代表五月，6代表闰五月
     * @return
     */
    public static int getSumOfDayInMonthForLunarByMonthSway(int year, int monthSway){
        int monthLeap = ChineseCalendar.getMonthLeapByYear(year);
        int monthLunar = convertMonthSwayToMonthLunar(monthSway, monthLeap);
        return ChineseCalendar.daysInChineseMonth(year, monthLunar);
    }

    public static int getSumOfDayInMonthForLunarByMonthLunar(int year, int monthLunar){
        return ChineseCalendar.daysInChineseMonth(year, monthLunar);
    }

    /**
     * 根据已知的闰月月份获取monthSway指向的月份天数
     * @param year
     * 			年
     * @param monthSway
     * 			月，包含闰月，如闰五月，monthSway为1代表1月，5代表五月，6代表闰五月
     * @param monthLeap
     * 			闰月，如闰五月则为5
     * @return
     * 			monthSway指向的月份天数
     */
    public static int getSumOfDayInMonthForLunarLeapYear(int year, int monthSway, int monthLeap){
        int month = convertMonthSwayToMonthLunar(monthSway, monthLeap);
        return ChineseCalendar.daysInChineseMonth(year, month);
    }

    /**
     * 根据year的阿拉伯数字生成汉字
     * @param year
     * 			year in number format, e.g. 1970
     * @return
     * 			year in Hanzi format, e.g. 一九七零年
     */
    public static String getLunarNameOfYear(int year){

        StringBuilder sb = new StringBuilder();
        int divider = 10;
        int digital = 0;

        while(year > 0){
            digital = year % divider;
            sb.insert(0, lunarNumbers[digital]);
            year = year / 10;
        }
    //		sb.append("年");
        return sb.toString();
    }

    /**
     * 获取月份的农历中文
     * @param month
     * 			month in number format, e.g. 1
     * 			month should be in range of [1, 12]
     * @return
     * 			month in Hanzi format, e.g. 一月
     */
    public static String getLunarNameOfMonth(int month){
        if(month > 0 && month < 13){
            return lunarMonths[month - 1];
        }else{
            throw new IllegalArgumentException("month should be in range of [1, 12] month is " + month);
        }
    }

    /**
     * 获取农历的日的中文
     * @param day
     * 			day in number format, e.g. 1
     * 			day should be in range of [1, 30]
     * @return
     * 			month in Hanzi format, e.g. 初一
     */
    public static String getLunarNameOfDay(int day){
        if(day > 0 && day < 31){
            return lunarDays[day - 1];
        }else{
            throw new IllegalArgumentException("day should be in range of [1, 30] day is " + day);
        }
    }

    /**
     * 获取农历的月份的中文，可包含闰月
     * @param monthLeap
     * 			the leap month
     * 			month should be in range of [0, 12], 0 if not leap
     * @return
     */
    public static String[] getLunarMonthsNamesWithLeap(int monthLeap){

        if(monthLeap == 0){
            return lunarMonths;
        }

        if(monthLeap < -12 || monthLeap > 0){
            throw new IllegalArgumentException("month should be in range of [-12, 0]");
        }

        int monthLeapAbs = Math.abs(monthLeap);

        String[] monthsOut = twelveMonthWithLeapCache.get(monthLeapAbs);
        if(monthsOut != null && monthsOut.length == 13){
            return monthsOut;
        }

        monthsOut = new String[13];

        System.arraycopy(lunarMonths, 0, monthsOut, 0, monthLeapAbs);
        monthsOut[monthLeapAbs] = "闰" + getLunarNameOfMonth(monthLeapAbs);
        System.arraycopy(lunarMonths, monthLeapAbs, monthsOut, monthLeapAbs + 1, lunarMonths.length - monthLeapAbs);

        twelveMonthWithLeapCache.put(monthLeapAbs, monthsOut);
        return monthsOut;
    }

    /**
     * 农历中，根据闰月、月份，获取月份view应该选择显示的游标值
     * @param monthLunar
     * 			小于0为闰月。取值范围是[-12,-1] + [1,12]
     * @param monthLeap
     * 			已知的闰月。取值范围是[-12,-1] + 0
     * 			0代表无闰月
     * @return
     */
    public static int convertMonthLunarToMonthSway(int monthLunar, int monthLeap){

        if(monthLeap > 0){
            throw new IllegalArgumentException("convertChineseMonthToMonthSway monthLeap should be in range of [-12, 0]");
        }

        if(monthLeap == 0){
            return monthLunar;
        }

        if(monthLunar == monthLeap){//闰月
            return -monthLunar + 1;
        }else if(monthLunar < -monthLeap + 1){
            return monthLunar;
        }else{
            return monthLunar + 1;
        }
    }

    /**
     * 农历根据月份的游标值和闰月值，获取月份的值，负值为闰月
     * @param monthSway
     * 				在NumberPicker中的value，取值范围[1,12] + 13
     * @param monthLeap
     * 				已知的闰月。取值范围是[-12,-1] + 0
     * 				0代表无闰月
     * @return
     * 				返回ChineseCalendar中需要的month，如果是闰月，传入负值
     * 				返回值的范围是[-12,-1] + [1,12]
     */
    public static int convertMonthSwayToMonthLunar(int monthSway, int monthLeap){

        if(monthLeap > 0){
            throw new IllegalArgumentException("convertChineseMonthToMonthSway monthLeap should be in range of [-12, 0]");
        }

        if(monthLeap == 0){
            return monthSway;
        }
        //有闰月
        if(monthSway == -monthLeap + 1){//闰月
            return monthLeap;
        }else if(monthSway < -monthLeap + 1){
            return monthSway;
        }else{
            return monthSway - 1;
        }
    }

    /**
     * 农历根据年份和月份游标值，获取月份的值。负值为闰月
     * @param monthSway
     * 				农历月份view的游标值
     * @param year
     * 				农历年份
     * @return
     */
    public static int convertMonthSwayToMonthLunarByYear(int monthSway, int year){
        int monthLeap = getMonthLeapByYear(year);
        return convertMonthSwayToMonthLunar(monthSway, monthLeap);
    }
}