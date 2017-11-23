/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redis.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具：日期处理<br>
 * pattern 必须存在且正确 (TIP: 为什么？调用者应该自己在做什么.)<br>
 * 时区：ll /usr/share/zoneinfo/
 * 
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 */
public class UtilDate {
	private static final Map<String, TimeZone> timeZoneCache = new ConcurrentHashMap<>();
	private static final Map<Object, ThreadLocal<DateFormat>> formatCache = new ConcurrentHashMap<>();
	public static final TimeZone tzUTC = getTimeZone("UTC"), tzShangHai = getTimeZone("Asia/Shanghai"), tzHongKong = getTimeZone("Asia/Hong_Kong"),
			tzAmericaNewYork = getTimeZone("America/New_York");
	/** "yyyy-MM-dd HH:mm:ss.SSS" */
	public static final String defaultPattern = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String defaultPatternTZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	/** 格式:yyyy-MM-dd */
	public static final String patternDate = "yyyy-MM-dd";
	public static final String patternTime = "HH:mm:ss";
	public static final int[] fields = {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
			Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

	public static TimeZone getTimeZone(String id) {
		if (id == null) return TimeZone.getDefault();
		return timeZoneCache.computeIfAbsent(id, key -> {
			return TimeZone.getTimeZone(key);
		});
	}

	public static TimeZone getTimeZone(Integer timezoneOffset) {
		return getTimeZone(timezoneOffset == null ? null : "GMT" + (timezoneOffset < 0 ? "" : "+") + timezoneOffset + ":00");
	}

	/// 日期运算
	/** 仅仅设置时间，日期不变 */
	public static void setTime(Calendar calendar, Integer hour, Integer minute, Integer second, Integer millsecond) {
		if (hour != null) {
			calendar.set(Calendar.HOUR_OF_DAY, hour);
		}
		if (minute != null) {
			calendar.set(Calendar.MINUTE, minute);
		}
		if (second != null) {
			calendar.set(Calendar.SECOND, second);
		}
		if (millsecond != null) {
			calendar.set(Calendar.MILLISECOND, millsecond);
		}
	}

	/**
	 * 仅仅设置日期，时间不变
	 * 
	 * @param year
	 *            {@linkplain Calendar#YEAR}
	 * @param month
	 *            {@linkplain Calendar#MONTH} 一月=0
	 * @param dayOfMonth
	 *            {@linkplain Calendar#DAY_OF_MONTH} 第一天=1
	 */
	public static void setDate(Calendar calendar, Integer year, Integer month, Integer dayOfMonth) {
		if (year != null) {
			calendar.set(Calendar.YEAR, year);
		}
		if (month != null) {
			calendar.set(Calendar.MONTH, month);
		}
		if (dayOfMonth != null) {
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		}
	}

	/**
	 * 某日开始加减按field来的offset
	 *
	 * @param date
	 * @param field Calendar的类型，比如：Calendar.DAY_OF_MONTH
	 * @param offset 偏移量
	 * @return
	 */
	public static Date getBeginOfDay(Date date, int field, int offset) {
		UtilLog.getLogger("com.webull.eye.center.abc").info("sleep 10s");
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		if (offset != 0) c.set(field, c.get(field) + offset);
		return c.getTime();
	}

	/**
	 * 当前时间的多少周以前
	 *
	 * @param date
	 * @param offset 偏移量
	 * @return
	 */
	public static Date getBeginOfWeek(Date date, int offset) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		if (offset != 0) c.set(Calendar.WEEK_OF_YEAR, c.get(Calendar.WEEK_OF_YEAR) + offset);
		return c.getTime();
	}

	public static Date getBeginOfDay(Date date) {
		return getBeginOfDay(date, null, 0);
	}
	/**
	 * 某日的开始
	 * 
	 * @param date
	 * @param offsetOfDay
	 *            0当前, -n倒推n天, n未来n天
	 * @return
	 */
	public static Date getBeginOfDay(Date date, int offsetOfDay) {
		return getBeginOfDay(date, null, offsetOfDay);
	}
	/** 获取指定时间，在timezone的日期 */
	public static Date getBeginOfDay(Date date, TimeZone timeZone) {
		return getBeginOfDay(date, timeZone, 0);
	}
	/** 获取指定时间，在timezone的日期 */
	public static Date getBeginOfDay(Date date, TimeZone timeZone, int offsetOfDay) {
		Calendar c = Calendar.getInstance();
		if (timeZone != null) c.setTimeZone(timeZone);
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		if (offsetOfDay != 0) c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + offsetOfDay);
		return c.getTime();
	}
	/// end 日期运算

	/** 转化为可读的持续时间格式 */
	public static String prettyTime(Date start, Date end) {
		return prettyTime(start == null ? null : start.getTime(), end == null ? null : end.getTime());
	}
	/** 转化为可读的持续时间格式 */
	public static String prettyTime(Number start, Number end) {
		if (start == null && end == null) {
			return null;
		}
		if (start == null) {
			return "null --> " + format(new Date(end.longValue()), defaultPattern);
		}
		if (end == null) {
			return format(new Date(start.longValue()), defaultPattern) + " --> null";
		}

		long time = end.longValue() - start.longValue();
		return time < 0 ? "-" + prettyTime(-time) : prettyTime(time);
	}

	/** 转化为可读的持续时间格式 */
	public static String prettyTime(Number detTime) {
		if (detTime == null) {
			return null;
		}

		long time = detTime.longValue();
		StringBuilder buf = new StringBuilder(128);
		int tmp = (int) (time / (24 * 3600 * 1000));
		if (tmp > 0) {
			buf.append(tmp).append('天');
		}
		time %= (24 * 3600 * 1000);
		if (time == 0) {
			return buf.length() == 0 ? "0秒" : buf.toString();
		}

		tmp = (int) (time / (3600 * 1000));
		if (tmp > 0) {
			buf.append(tmp).append('时');
		}
		time %= (3600 * 1000);
		if (time == 0) {
			return buf.length() == 0 ? "0秒" : buf.toString();
		}

		tmp = (int) (time / (60 * 1000));
		if (tmp > 0) {
			buf.append(tmp).append('分');
		}
		time %= (60 * 1000);
		if (time == 0) {
			return buf.length() == 0 ? "0秒" : buf.toString();
		}

		buf.append(time / 1000f).append('秒');
		return buf.toString();
	}

	/**
	 * @param locale
	 *            地域(null使用当前)
	 * @param timeZone
	 *            时区(null使用当前)
	 * @param pattern
	 *            必填，调用者应该清楚自己在做什么
	 */
	public static DateFormat getDateFormat(Locale locale, TimeZone timeZone, String pattern) {
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		if (locale == null) {
			locale = Locale.getDefault(Locale.Category.FORMAT);
		}

		KeyComplex key = new KeyComplex(timeZone, locale, pattern);
		ThreadLocal<DateFormat> formatThreadLocal = formatCache.get(key);
		if (formatThreadLocal == null) {
			formatThreadLocal = new ThreadLocal<>();
			formatCache.put(key, formatThreadLocal);
		}

		DateFormat result = formatThreadLocal.get();
		if (result != null) {
			return result;
		}

		result = new SimpleDateFormat(pattern, locale);
		result.setTimeZone(timeZone);
		formatThreadLocal.set(result);
		return result;
	}

	public static Date parse(String date, Integer timeZoneOffset, String pattern) {
		return parse(date, null, getTimeZone(timeZoneOffset), pattern);
	}

	public static Date parse(String date, Locale locale, Integer timeZoneOffset, String pattern) {
		return parse(date, null, getTimeZone(timeZoneOffset), pattern);
	}

	public static Date parse(String date, TimeZone timeZone, String pattern) {
		return parse(date, null, timeZone, pattern);
	}

	/** 使用{@link #patternDate} */
	public static Date parseDate(String date, TimeZone timeZone) {
		return parse(date, null, timeZone, patternDate);
	}

	public static Date parse(String date, Locale locale, TimeZone timeZone, String pattern) {
		if (date == null || date.isEmpty()) {
			return null;
		}

		if (pattern.length() > date.length()) {
			pattern = pattern.substring(0, date.length());
		}
		DateFormat format = getDateFormat(locale, timeZone, pattern);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("pattern=" + pattern + ", date=" + date);
		}
	}

	public static String format(Date date, Locale locale, TimeZone timeZone, String pattern) {
		if (date == null) {
			return null;
		}
		return getDateFormat(locale, timeZone, pattern).format(date);
	}

	/** 使用当前locale和timezone转换 */
	public static Date parse(String date, String pattern) {
		return parse(date, null, (TimeZone) null, pattern);
	}

	/** 使用当前locale和timezone转换, {@linkplain #defaultPattern} */
	public static Date parse(String date) {
		return parse(date, null, (TimeZone) null, defaultPattern);
	}
	/** 使用当前locale和timezone转换, {@linkplain #defaultPatternTZ} */
	public static Date parseTZ(String date, TimeZone tz) {
		return parse(date, null, tz, defaultPatternTZ);
	}

	/** 使用当前locale和timezone格式化 */
	public static String format(Date date, String pattern) {
		return format(date, null, null, pattern);
	}

	/** 使用当前locale和timezone格式化, {@linkplain #defaultPattern} */
	public static String format(Date date) {
		return format(date, null, null, defaultPattern);
	}

	/** 使用当前locale和timezone格式化, {@linkplain #defaultPatternTZ} */
	public static String formatTZ(Date date) {
		return format(date, null, null, defaultPatternTZ);
	}

	/** 使用当前locale和timezone格式化, {@linkplain #defaultPatternTZ} */
	public static String formatTZ(Date date, TimeZone timeZone) {
		return format(date, null, timeZone, defaultPatternTZ);
	}
	public static String formatOfNowTZ() {
		return formatTZ(new Date());
	}

	/** 基于当前日期，生成指定hour和minutes的时间(秒、毫秒=0, zone==null使用缺省) */
	public static Calendar getCalendarTime(TimeZone zone, Integer hour, Integer minutes) {
		if (zone == null) {
			zone = TimeZone.getDefault();
		}
		Calendar calendar = Calendar.getInstance(zone);
		setTime(calendar, hour, minutes, 0, 0);
		return calendar;
	}

	/** 基于当前日期，生成指定hour和minutes的时间(秒、毫秒=0) */
	public static Date getTime(TimeZone zone, Integer hour, Integer minutes) {
		return getCalendarTime(zone, hour, minutes).getTime();
	}

	public static Date getAfter(Date date1, Date date2) {
		return date1 == null ? date2 : date2 == null ? date1 : date2.after(date1) ? date2 : date1;
	}

	/** date.length == {@link #patternDate} */
	public static boolean isDate(String date) {
		return UtilString.isLength(date, patternDate.length());
	}

	/**
	 * @param field
	 *            {@linkplain Calendar#YEAR}请使用常量Calendar
	 */
    public static Date changeDate(Date date, int field, int step){
    	return changeDate(date, field, step, null);
    }

	/**
	 * @param field
	 *            {@linkplain Calendar#YEAR}请使用常量Calendar
	 */
    public static Date changeDate(Date date, int field, int step, TimeZone zone){
    	Calendar cale = Calendar.getInstance(zone == null ? TimeZone.getDefault() : zone);
    	cale.setTime(date);
    	changeDate(cale, field, step);
    	return cale.getTime();    	
    }
    public static void changeDate(Calendar calendar, int field, int step){
    	calendar.add(field, step);
    }

    /** @return time1.after(time2) */
	public static boolean after(Date time1, Date time2) {
		if (time1 == time2) return false;
		if (time2 == null) return true;
		if (time1 == null) return false;
		return time1.after(time2);
	}
	
	private static ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal<>();
	/** 获取本地线程变量日历(TZ：UTC) */
	public static Calendar getCalendar() {
		return getCalendar(tzUTC);
	}
	public static Calendar getCalendar(TimeZone tz) {
		if (tz == null) tz = TimeZone.getDefault();
		
		Calendar calendar = calendarThreadLocal.get();
		if (calendar == null) {
			calendar = Calendar.getInstance(tz, Locale.US);
			calendarThreadLocal.set(calendar);
		} else {
			calendar.setTimeZone(tz);
		}
		calendar.setTimeInMillis(0);
		return calendar;
	}
	/** time是UTC时间的，格式:"HH:mm:ss" */
	public static Date toDate(String time, TimeZone tz) {
		Calendar cal = getCalendar(tz);
		cal.setTimeInMillis(System.currentTimeMillis());
		
		int[] times = new int[] {0, 0, 0, 0};
		Iterator<String> iter = UtilString.split(time, ':').iterator();
		int count = 0;
		times[count++] = Integer.parseInt(iter.next());
		if (iter.hasNext()) {
			times[count++] = Integer.parseInt(iter.next());
			if (iter.hasNext()) {
				times[count++] = Integer.parseInt(iter.next());
			}
		}

		cal.set(Calendar.HOUR_OF_DAY, times[0]);
		cal.set(Calendar.MINUTE, times[1]);
		cal.set(Calendar.SECOND, times[2]);
		cal.set(Calendar.MILLISECOND, times[3]);
		return cal.getTime();
	}
	/** time是UTC时间的，格式:"HH:mm:ss" */
	public static Date toDateByUTC(String time) {
		return toDate(time, tzUTC);
	}

	public static Date getDay(Date date, int hour, int minute, TimeZone tz) {
		Calendar c = getCalendar(tz);
		if (date == null) {
			c.setTimeInMillis(System.currentTimeMillis());
		} else {
			c.setTime(date);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	/**
	 * 指定时间time，指定时间字段field，指定时间间隔det<br>
	 * 向前：如时间10:05，间隔5分钟结果10:05，间隔10分钟结果10:00<br>
	 * 向后：如时间10:05，间隔5分钟结果10:05，间隔10分钟结果10:10<br>
	 */
	public static Date getTimeByDet(Date date, int field, int det, boolean before, TimeZone tz, boolean include) {
		if (det < 1) throw new IllegalArgumentException("det=" + det);
		if (tz == null) tz = tzUTC;
		Calendar c = Calendar.getInstance(tz, Locale.US);
		if (date != null) c.setTime(date);
		
		for (int i = 0; i < fields.length; i++) {
			int tmp = fields[i];
			if (tmp > field) c.set(fields[i], 0);
		}
		
		int value = c.get(field), mod = value % det;
		if (mod == 0) {
			if (include) return c.getTime();
			
			c.set(field, before ? value - det : value + det);
			return c.getTime();
		}
		
		c.set(field, before ? value - mod : value - mod + det);
		return c.getTime();
	}
}
