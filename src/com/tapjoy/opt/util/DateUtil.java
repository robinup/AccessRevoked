package com.tapjoy.opt.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil
{
	
	/**
	 * TBD: Make this a little generic so that it can return difference on different dates
	 * Make sure the new method handles dates on both sides of daylightsavings 
	 * Computes "Tomorrow" - a 
	 * @param a should be today + X hrs + Y mins 
	 * @return "(24-X)hrs and (60-Y)mins"
	 */
	public static String getDiffWRTTomorrow(Date a)
	{
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.add(java.util.GregorianCalendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0) ;
		cal.set(Calendar.MINUTE, 0) ;
		cal.set(Calendar.SECOND, 0) ;
		
		//calculated difference
		long diffInMilliSecs = cal.getTimeInMillis() - a.getTime() ;
		cal.setTimeZone(TimeZone.getTimeZone("GMT")) ;
		cal.setTimeInMillis(diffInMilliSecs) ;
		return cal.get(Calendar.HOUR_OF_DAY) + " hours and " + cal.get(Calendar.MINUTE) + " minutes";	

	}

	/**
	 * return timeStamp of different days for the same time
	 * @param old timeStamp, days of the difference
	 * @return new timeStamp
	 */
	public static Timestamp getTimestampDiffDays(Timestamp oldTime, int days) {
		final long ONE_DAY_MILLISCONDS = 24 * 60 * 60 * 1000;
				
		long milliseconds = oldTime.getTime();		
		Timestamp newTime = new Timestamp(milliseconds + days*ONE_DAY_MILLISCONDS);
		
		return newTime;
	}
	
	public static Date addDays(Date a, int days)
	{
		long newTimeInMillis = a.getTime() + (days*24L*60*60*1000) ;
		return new Date(newTimeInMillis) ;
	}

	/**
	 * return standard date format
	 * 
	 * @return
	 */
	public static SimpleDateFormat getStandardDateFormat() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		return format;
	}

	/**
	 * get today's date in the format of example 2012-11-29
	 * 
	 * @return
	 */
	public static String getTodayDateString(Date now) {
		SimpleDateFormat format = getStandardDateFormat();
		return format.format(now);
	}

	public static String getTodayDateString() {
		Date now = new Date();
		SimpleDateFormat format = getStandardDateFormat();
		return format.format(now);
	}

	public static String getCurrentTimeStampString() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		return format.format(now);
	}

	public static String getYYYY(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		return format.format(current);
	}

	public static String getMM(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("MM");
		return format.format(current);
	}

	public static String getDD(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("dd");
		return format.format(current);
	}

	public static String getHH(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("HH");
		return format.format(current);
	}

	public static String get_mm(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("mm");
		return format.format(current);
	}

	public static String get_ss(Date current) {
		SimpleDateFormat format = new SimpleDateFormat("ss");
		return format.format(current);
	}

	public static String getYYYY(int diff) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		return format.format(getDiffDate(diff));
	}

	public static String getMM(int diff) {
		SimpleDateFormat format = new SimpleDateFormat("MM");
		return format.format(getDiffDate(diff));
	}

	public static String getDD(int diff) {
		SimpleDateFormat format = new SimpleDateFormat("dd");
		return format.format(getDiffDate(diff));
	}

	public static String getYYYY(int diff, Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		return format.format(getDiffDate(date, diff));
	}

	public static String getMM(int diff, Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MM");
		return format.format(getDiffDate(date, diff));
	}

	public static String getDD(int diff, Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd");
		return format.format(getDiffDate(date, diff));
	}

	public static Date getDiffDate(int diff) {
		Date date = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, diff);

		return cal.getTime();
	}
	
	public static Date getDiffDateBySeconds(int diff) {
		Date date = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.SECOND, diff);

		return cal.getTime();
	}

	public static Date getDiffDate(Date date, int diff) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, diff);

		return cal.getTime();
	}

	public static long getDateDiffInSeconds(Date date1, Date date2) {
		long ms1 = date1.getTime();
		long ms2 = date2.getTime();

		return (ms2 - ms1)/1000;
	}

	public static String getDiffDateString(int diff) {
		Date date = new Date();
		return getYYYY(diff, date) + "-" + getMM(diff, date) + "-" + getDD(diff, date);
	}
}
