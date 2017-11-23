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

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:daniel@webull.com">杨浩</a>
 */
public class UTCSimpleDateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = 1L;
	private static final int utcPatternLen = "2016-07-27T09:32:40.000+0000".length();
	private static final int utcPattern2Len = "2016-07-27T09:32:40+0000".length();
	private static final ThreadLocal<TimeZone> timeZoneTL = new ThreadLocal<>();
	
	public static TimeZone setTimeZoneTL(TimeZone timeZone) {
		TimeZone old = timeZoneTL.get();
		timeZoneTL.set(timeZone);
		return old;
	}
	
	
	public UTCSimpleDateFormat() {
		super(UtilDate.defaultPatternTZ);
	}
	

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		TimeZone cur = timeZoneTL.get();
		if (cur == null) return super.format(date, toAppendTo, pos);
		
		TimeZone old = getTimeZone();
		setTimeZone(cur);
		try {
			return super.format(date, toAppendTo, pos);
		} finally {
			setTimeZone(old);
		}
	}

	@Override
	public Date parse(String text, ParsePosition pos) {
		if (text == null || text.isEmpty()) return null;
		if (text.length() == utcPatternLen) return super.parse(text, pos);
		
		Date result;
		if (text.length() == utcPattern2Len && text.charAt(10) == 'T') {
			result = UtilDate.parse(text, "yyyy-MM-dd'T'HH:mm:ssZ");
		} else {
			String[] dateTimeTZ = getDateTimeZone(text);
			
			String pattern = "";
			if (dateTimeTZ[0] != null) pattern += "yyyy-MM-dd".substring(0, dateTimeTZ[0].length());
			if (dateTimeTZ[1] != null) pattern += "'T'" + "HH:mm:ss.SSS".substring(0, dateTimeTZ[1].length());
			if (dateTimeTZ[2] != null) pattern += 'Z';
			
			result = UtilDate.parse(text, pattern);
		}
		pos.setIndex(text.length());
		return result;
	}
	
	
	private static String[] getDateTimeZone(String text) {
		int tzPos = text.lastIndexOf('+');
		if (tzPos == -1) {
			tzPos = text.lastIndexOf('-');
		}
		
		String tz = null;
		if (tzPos != -1) {
			tz = text.substring(tzPos);
			text = text.substring(0, tzPos);
		}
		
		String date = null, time = null;
		int tPos = text.indexOf('T');
		if (tPos == -1) {
			date = text;
		} else {
			date = text.substring(0, tPos);
			time = text.substring(tPos + 1);
		}
		return new String[] {date, time, tz};
	}
}
