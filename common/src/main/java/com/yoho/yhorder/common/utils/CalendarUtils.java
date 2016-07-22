package com.yoho.yhorder.common.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 常用日期格式
 */
public class CalendarUtils {

	/** 完整日期时间无间隔格式 */
	public static final String LONG_FORMAT = "yyyyMMddHHmmss";
	/** 日期无间隔格式 */
	public static final String SHORT_FORMAT = "yyyyMMdd";
	/** 无间隔时间格式 */
	public static final String SHORT_FORMAT_TIME = "HHmmss";
	/** 完整日期时间横线与冒号分隔格式 */
	public static final String LONG_FORMAT_LINE = "yyyy-MM-dd HH:mm:ss";
	/** 横线分隔日期格式 */
	public static final String SHORT_FORMAT_LINE = "yyyy-MM-dd";
	/** 冒号分隔时间格式 */
	public static final String SHORT_FORMAT_TIME_COLON = "HH:mm:ss";
	/**
	 * 一天的秒数
	 */
	public static long ONE_DAY_OF_SECONDS = 86400;

	public static Calendar parseCalendar(String str, String... parsePatterns)
			throws ParseException {
		Calendar cal = Calendar.getInstance();
		if (ArrayUtils.isEmpty(parsePatterns)) {
			cal.setTime(DateUtils.parseDate(str,
					new String[]{LONG_FORMAT_LINE}));
		} else {
			cal.setTime(DateUtils.parseDate(str, parsePatterns));
		}
		return cal;
	}

	public static String parsefomatCalendar(Calendar cal, String parsePattern) {
		String str = "";
		if (StringUtils.isEmpty(parsePattern)) {
			str = DateFormatUtils.format(cal, LONG_FORMAT_LINE);
		} else {
			str = DateFormatUtils.format(cal, parsePattern);
		}
		return str;
	}

	/**
	 * 格式化日期
	 * 
	 * @param format
	 * @return
	 */
	public static String toCalendarString(String format) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdft = new SimpleDateFormat(format);
		return sdft.format(c.getTime());
	}

	/**
	 * 字符串转换为指定格式日期
	 * 
	 * @param date
	 *            字符串时间
	 * @param format
	 *            格式
	 * @return
	 * @throws ParseException
	 */
	public static Calendar toCalendar(String date, String format)
			throws ParseException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdft = new SimpleDateFormat(format);
		c.setTime(sdft.parse(date));
		return c;
	}

	/**
	 * 格式化日期
	 * 
	 * @param calendar
	 * @param format
	 * @return
	 */
	public static String toCalendarString(Calendar calendar, String format) {
		SimpleDateFormat sdft = new SimpleDateFormat(format);
		return sdft.format(calendar.getTime());
	}

	/*
	 * 转换日期
	 */
	public static Calendar toCalendar(java.util.Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	/*
	 * 按格式转换日期
	 */
	public static Calendar toCalendar(Calendar calendar, String format)
			throws ParseException {
		String s = toCalendarString(calendar, format);
		return toCalendar(s, format);
	}

	public static String getCurrentCalendar() {
		return toCalendarString(LONG_FORMAT_LINE);
	}

	public static String increaseDay(String day) throws ParseException {
		Calendar cal = CalendarUtils
				.toCalendar(day, CalendarUtils.SHORT_FORMAT);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return CalendarUtils.toCalendarString(cal, CalendarUtils.SHORT_FORMAT);
	}

	/**
	 * 格式化秒数日期
	 * @param time			秒数
	 * @param format		格式
	 * @return
	 */
	public static String parsefomatSeconds(Integer time, String format){
		SimpleDateFormat sdft = new SimpleDateFormat(format);
		Date secondsTime = new Date();
		secondsTime.setTime(time * 1000L);
		return sdft.format(secondsTime);
	}

	/**
	 * 获取时间差（绝对值）
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static Long getTimeDiff(long time1, long time2){
		return Math.abs(time1 - time2);
	}

	/**
	 * 获取天数的秒数时间
	 * @param day         天数
	 * @return
	 */
	public static long getTimes(long day){
		return day * ONE_DAY_OF_SECONDS ;
	}

	/**
	 * 获取系统当前秒数时间
	 * @return
	 */
	public static Long getSystemSeconds(){
		return System.currentTimeMillis() / 1000;
	}
	
	/**
	 * 获取系统当前秒数时间
	 * @return
	 */
	public static int getSystemIntSeconds(){
		return (int)(System.currentTimeMillis() / 1000);
	}

	public static void main(String[] args) {
		String s = CalendarUtils.toCalendarString("yyyy-MM-dd HH:mm:ss");
		System.out.println(s);
	}

}
