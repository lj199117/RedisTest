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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 * @since 0.1.0
 */
public class UtilString {
	public static final int md5Len = md5("empty").length(), uuidLen = uuid().length();
	private static final Random random = new Random();
	private static final Character[] randomChars;
	private static final Character[] randomNumbers;
	public static final Set<String> CHINA_PHONES = new TreeSet<>();

	static {
		{
			List<Character> chars = new ArrayList<>();
			addChars(chars, 'a', 'z');
			addChars(chars, 'A', 'Z');
			addChars(chars, '0', '9');
			Collections.shuffle(chars, random);
			randomChars = chars.toArray(new Character[0]);
		}
		{
			List<Character> chars = new ArrayList<>();
			addChars(chars, '0', '9');
			Collections.shuffle(chars, random);
			randomNumbers = chars.toArray(new Character[0]);
		}

		CHINA_PHONES.addAll(UtilString.split("130,131,132,145,155,156,185,186", ',')); // 联通
		CHINA_PHONES.addAll(UtilString.split("134,135,136,137,138,139,150,151,152,157,158,159,147,182,183,184,187,188", ',')); // 移动
		CHINA_PHONES.addAll(UtilString.split("133,153,180,181,189", ',')); // 电信
		CHINA_PHONES.addAll(UtilString.split("140,141,142,143,144,146,148,149,154", ',')); // 未知号段
		CHINA_PHONES.addAll(UtilString.split("176,177,178", ',')); // 4G号段
		CHINA_PHONES.add("170");
	}

	private static void addChars(List<Character> list, int start, int end) {
		for (int i = start; i <= end; i++) {
			list.add((char) i);
		}
	}

	/** 生成数字随机码 */
	private static String randChars(Character[] chars, int len) {
		StringBuilder buf = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			buf.append(chars[random.nextInt(chars.length)]);
		}
		return buf.toString();
	}

	/** 生成字母数字随机码 */
	public static String randCode(int len) {
		return randChars(randomChars, len);
	}

	/** 生成数字随机码 */
	public static String randNumCode(int len) {
		return randChars(randomNumbers, len);
	}

	public static boolean isEmpty(Object value) {
		return isEmpty(value, false);
	}
	public static boolean notEmpty(Object value) {
		return !isEmpty(value, false);
	}

	public static boolean isEmpty(Object value, boolean trim) {
		if (value == null) {
			return true;
		}
		String text = value instanceof String ? (String) value : value.toString();
		return (trim ? text.trim() : text).isEmpty();
	}

	public static String trim(String value) {
		return value == null ? null : value.trim();
	}

	public static String trimL(String value) {
		if (value == null) {
			return null;
		}

		for (int i = 0, len = value.length(); i < len; i++) {
			if (value.charAt(i) > ' ') {
				return value.substring(i);
			}
		}
		return "";
	}

	public static String trimR(String value) {
		if (value == null) {
			return null;
		}

		for (int i = value.length() - 1; i >= 0; i--) {
			if (value.charAt(i) > ' ') {
				return value.substring(0, i + 1);
			}
		}
		return "";
	}

	/** 是否是整数 */
	public static boolean isNumber(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}

		for (int i = 0, len = text.length(); i < len; i++) {
			char ch = text.charAt(i);
			if (ch > '9' || ch < '0') {
				return false;
			}
		}
		return true;
	}
	public static boolean isNumber(char ch) {
		return ch >= '0' && ch <= '9';
	}

	/** 是否是hex字符(不允许空串) */
	public static boolean isHex(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}

		for (int i = 0, len = text.length(); i < len; i++) {
			char ch = text.charAt(i);
			if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
				continue;
			}

			return false;
		}
		return true;
	}

	/** 邮箱判断 */
	public static boolean isEmail(String email) {
		return !isEmpty(email) && Pattern.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", email);
	}
	/** 手机号判断 */
	public static boolean isPhone(String phone) {
		return !isEmpty(phone) && Pattern.matches("^\\+\\d{1,4}-\\d{2,11}$", phone);
	}

	/** @see #concat(Collection, String, boolean) */
	public static String concat(Collection<?> collection, String split) {
		return concat(collection, split, false);
	}

	/** 集合中的元素连接起来(split==null默认使用',') */
	public static String concat(Collection<?> collection, String split, boolean ignoreNullEl) {
		if (collection == null || collection.isEmpty()) {
			return "";
		}

		if (split == null) {
			split = ",";
		}
		StringBuilder buf = new StringBuilder(collection.size() * (10 + split.length()));
		for (Object el : collection) {
			if (ignoreNullEl && el == null) {
				continue;
			}

			buf.append(el).append(split);
		}
		if (buf.length() > 0) {
			buf.setLength(buf.length() - split.length());
		}
		return buf.toString();
	}

	/** @see #concat(Object[], String, boolean) */
	public static <T> String concat(T[] array, String split) {
		return concat(array, split, false);
	}

	/** 集合中的元素连接起来(split==null默认使用',') */
	public static <T> String concat(T[] array, String split, boolean ignoreNullEl) {
		if (array == null || array.length == 0) {
			return "";
		}

		if (split == null) {
			split = ",";
		}
		StringBuilder buf = new StringBuilder(array.length * (10 + split.length()));
		for (Object el : array) {
			if (ignoreNullEl && el == null) {
				continue;
			}

			buf.append(el).append(split);
		}
		if (buf.length() > 0) {
			buf.setLength(buf.length() - split.length());
		}
		return buf.toString();
	}

	/** 把text按照各个分割点进行分割 */
	public static List<String> split(String text, String... splitPoints) {
		List<String> result = new ArrayList<>();
		if (text == null) {
			return result;
		}

		int pos = 0;
		for (String point : splitPoints) {
			if (point == null || point.isEmpty()) {
				throw new IllegalArgumentException("split point=" + point + " in " + Arrays.toString(splitPoints));
			}

			int fond = text.indexOf(point, pos);
			if (fond == -1) {
				result.add(text.substring(pos));
				return result;
			}

			result.add(text.substring(pos, fond));
			pos = fond + point.length();
		}
		if (pos <= text.length()) {
			result.add(text.substring(pos));
		}
		return result;
	}

	/** 使用prefix做切割，找出所有prefix前缀的字符串 */
	public static List<String> splitByCycle(String text, String prefix) {
		List<String> result = new ArrayList<>();
		if (text == null) {
			return result;
		}

		int pos = text.indexOf(prefix);
		if (pos == -1) {
			return result;
		}

		for (;;) {
			int posNext = text.indexOf(prefix, pos + prefix.length());
			if (posNext == -1) {
				result.add(text.substring(pos));
				return result;
			}

			result.add(text.substring(pos, posNext));
			pos = posNext;
		}
	}

	/**
	 * 键值对字符串转换为map<br/>
	 * 算法：先找key，后找entry<br/>
	 * 例如entrySplit=", ", valSplit=": "，串= "client: 192.168.0.1, server: ssl.webull.com"<br/>
	 * 结果(value如果有"或'，自动被去掉)：
	 * 
	 * <pre>
	 * {
	 * 	client: '192.168.0.1',
	 * 	server: 'ssl.webull.com'
	 * }
	 * </pre>
	 */
	public static Map<String, String> toMap(String text, String entrySplit, String valSplit) {
		Map<String, String> result = new LinkedHashMap<>();
		if (text == null) {
			return result;
		}

		for (int pos = 0, len = text.length(); pos < len;) {
			int entryPos = text.indexOf(entrySplit, pos), valPos = text.indexOf(valSplit, pos);

			// 可能需要修订entryPos，如在val中有entrySplit时
			if (valPos != -1 && valPos < entryPos) {
				char quote = text.charAt(valPos + valSplit.length());
				if ((quote == '"' || quote == '\'') && quote != text.charAt(entryPos - 1)) { // 需要修订
					int tmp = text.indexOf(quote + entrySplit, valPos);
					if (tmp == -1) {
						throw new IllegalArgumentException("kv='" + text.substring(pos) + "', text=" + text);
					}
					entryPos = tmp + 1;
				}
			}

			String kv = null;
			if (entryPos == -1) {
				kv = text.substring(pos);
				pos = len;
			} else {
				kv = text.substring(pos, entryPos);
				pos = entryPos + entrySplit.length();
			}
			put(result, kv, valSplit);
		}

		return result;
	}

	private static void put(Map<String, String> map, String kv, String valSplit) {
		int valPos = kv.indexOf(valSplit);
		if (valPos == -1) {
			map.put(kv, null);
		} else {
			String val = kv.substring(valPos + valSplit.length());
			if (val.length() > 0 && (val.charAt(0) == '\'' || val.charAt(0) == '"') && val.charAt(0) == val.charAt(val.length() - 1)) {
				val = val.substring(1, val.length() - 1);
			}
			map.put(kv.substring(0, valPos), val);
		}
	}

	/**
	 * 耗时只有{@linkplain java.math.BigInteger#toString(int) new BigInteger(bytes).toString(16)}的40%
	 */
	public static String toHexString(byte[] bytes) {
		StringBuilder buf = new StringBuilder(bytes.length * 2);
		for (int i = 0, len = bytes.length, value; i < len; i++) {
			value = bytes[i] & 0xff;
			if (value < 0x10) {
				buf.append('0');
			}
			buf.append(Integer.toHexString(value));
		}
		return buf.toString();
	}

	private static String fetchDigit(String text, int start) {
		if (text == null) {
			return "";
		}

		StringBuilder buf = new StringBuilder();
		boolean begin = false;
		for (int len = text.length(); start < len; start++) {
			char ch = text.charAt(start);
			if (ch >= '0' && ch <= '9') {
				begin = true;
				buf.append(ch);
			} else if (begin) {
				break;
			}
		}
		return buf.toString();
	}

	public static Integer fetchInt(String text, int start) {
		String value = fetchDigit(text, start);
		return value.length() > 0 ? Integer.valueOf(value.toString()) : null;
	}

	public static Long fetchLong(String text, int start) {
		String value = fetchDigit(text, start);
		return value.length() > 0 ? Long.valueOf(value.toString()) : null;
	}

	/** Returns val represented by the specified number of hex digits. */
	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

	/** uuid方法，结果中没有-符号，耗时只有replaceAll的30% */
	public static String uuid() {
		UUID uuid = UUID.randomUUID();
		long mostSigBits = uuid.getMostSignificantBits(), leastSigBits = uuid.getLeastSignificantBits();
		return digits(mostSigBits >> 32, 8) + digits(mostSigBits >> 16, 4) + digits(mostSigBits, 4) + digits(leastSigBits >> 48, 4) + digits(leastSigBits, 12);

	}

	/**
	 * 将输入字符串经过MD5处理后返回
	 * 
	 * @param values 待处理字符串
	 * @return MD5之后的结果
	 */
	public static String md5(String... values) {
		return md5(Arrays.asList(values));
	}

	/** 将输入字符串经过MD5处理后返回 */
	public static String md5(Collection<String> values) {
		MessageDigest messageDigest = getMessageDigest("MD5");
		for (String value : values) {
			if (value != null) {
				messageDigest.update(value.getBytes(UtilIo.utf8));
			}
		}
		return toHexString(messageDigest.digest());
	}
	public static String md5(byte[] bytes) {
		MessageDigest messageDigest = getMessageDigest("MD5");
		messageDigest.update(bytes);
		return toHexString(messageDigest.digest());
	}

	/**
	 * @param algorithm 例如：MD5
	 */
	public static MessageDigest getMessageDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm.toUpperCase());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 provider not exist!");
		}
	}

	/** 提取split1和split2之间的字符串(最大范围查找) */
	public static String substring(String text, String split1, String split2) {
		if (split1 == null && split2 == null) {
			return text;
		}
		if (text == null) {
			return null;
		}

		int pos1 = split1 == null ? 0 : text.indexOf(split1);
		if (pos1 < 0) {
			return null;
		}
		if (split1 != null) {
			pos1 += split1.length(); // 找到第一分割
		}

		int pos2 = split2 == null ? text.length() : text.lastIndexOf(split2);
		return pos1 < pos2 ? text.substring(pos1, pos2) : null; // 找到第二分割
	}
	
	/** 提取split1和split2之间的字符串（顺序查找） */
	public static String substringMin(String text, String split1, String split2) {
		if (split1 == null && split2 == null) {
			return text;
		}
		if (text == null) {
			return null;
		}

		int pos1 = split1 == null ? 0 : text.indexOf(split1);
		if (pos1 < 0) {
			return null;
		}
		if (split1 != null) {
			pos1 += split1.length(); // 找到第一分割
		}

		int pos2 = split2 == null ? text.length() : text.indexOf(split2, pos1);
		return pos1 < pos2 ? text.substring(pos1, pos2) : null; // 找到第二分割
	}

	public static String substring(String text, int beginIndex, int endIndex) {
		if (text == null) {
			return null;
		}
		if (beginIndex < 0) {
			beginIndex = 0;
		}
		if (beginIndex > text.length() - 1) {
			return "";
		}
		if (endIndex > text.length()) {
			endIndex = text.length();
		}
		return text.substring(beginIndex, endIndex);
	}

	public static String toString(Object value) {
		return value == null ? null : value.toString();
	}

	public static String toString(Object value, Object defaultValue) {
		return value == null ? toString(defaultValue) : value.toString();
	}

	public static String toString(Collection<?> collection, int maxLen) {
		if (collection == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder(12 * maxLen);
		buf.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			buf.append(iterator.next()).append(", ");
		}
		if (buf.length() > 1) {
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("]");
		return buf.toString();
	}

	public static String encodeURL(Object value) {
		if (value == null) {
			return "";
		}
		try {
			return URLEncoder.encode(value.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String decodeURL(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return URLDecoder.decode(value.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 切分后自动执行trim
	 * 
	 * @see #split(String, char, boolean, Integer)
	 */
	public static List<String> split(String text, final char splitChar) {
		return split(text, splitChar, true, text.length() / 16);
	}

	/**
	 * 非正则表达式方式split，大量计算时可以节省cpu<br>
	 * 如果做缓存时，注意String的特性——共享一个char数组，潜在的jvm内存泄漏
	 * 
	 * @param text 原始文本
	 * @param splitChar 分割字符
	 * @param initialCapacity 初始化List容量大小
	 * @return 切分后字符串
	 */
	public static List<String> split(String text, final char splitChar, boolean trim, Integer initialCapacity) {
		List<String> result = (initialCapacity == null || initialCapacity < 10) ? new ArrayList<String>() : new ArrayList<String>(initialCapacity);
		int start = 0, len = text.length();
		for (int i = 0; i < len; i++) {
			if (splitChar == text.charAt(i)) {
				String tmp = start == i ? "" : text.substring(start, i);
				result.add(trim ? tmp.trim() : tmp);
				start = i + 1;
			}
		}
		if (start <= len) {
			String tmp = text.substring(start);
			result.add(trim ? tmp.trim() : tmp);
		}
		return result;
	}
	/** text按照split进行分割，每段交给action处理 */
	public static <R> List<R> split(String text, String split, Function<String, R> action) {
		List<R> result = new ArrayList<>();
		if (text == null) return result;
		
		for (int start = 0, pos;; start = pos + split.length()) {
			pos = text.indexOf(split, start);
			if (pos == -1) {
				result.add(action.apply(text.substring(start)));
				return result;
			}
			result.add(action.apply(text.substring(start, pos)));
		}
	}

	/** 数字字符串 */
	public static boolean isDigits(String text) {
		if (isEmpty(text)) {
			return false;
		}
		for (int i = 0, len = text.length(); i < len; i++) {
			char ch = text.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}
		return true;
	}

	/** 中国大陆手机号码判断 */
	public static boolean isChineseMobile(String mobile) {
		return isDigits(mobile) && mobile.length() == 11 && CHINA_PHONES.contains(mobile.substring(0, 3));
	}

	public static boolean isLength(String value, int len) {
		return value != null && value.length() == len ? true : false;
	}
	
	public static String fetchNumber(String text) {
		if (text == null || text.isEmpty()) return null;
		
		int startPos = -1;
		for (int i = 0, len = text.length(); i < len; i++) {
			if (isNumber(text.charAt(i))) {
				startPos = i;
				break;
			}
		}
		
		int endPos = text.length();
		boolean isFloat = false;
		for (int i = startPos + 1, len = text.length(); i < len; i++) {
			if (!isNumber(text.charAt(i))) {
				if (!isFloat && text.charAt(i) == '.') {
					isFloat = true;
					continue;
				}
				endPos = i;
				break;
			}
		}
		if (startPos == -1) return null;
		return text.substring(startPos, endPos);
	}
	
	public static String replaceAll(String text, String found, String replacement) {
		if (text == null || text.isEmpty()) return text;
		StringBuilder buf = new StringBuilder(text.length());
		for (int pos = 0, foundPos;;) {
			foundPos = text.indexOf(found, pos);
			if (foundPos == -1) {
				buf.append(text.substring(pos));
				break;
			}
			buf.append(text.substring(pos, foundPos)).append(replacement);
			pos = foundPos + found.length();
		}
		return buf.toString();
	}
	public static String replaceAll(String text, char found, char replacement) {
		if (text == null || text.isEmpty()) return text;
		StringBuilder buf = new StringBuilder(text.length());
		for (int pos = 0, len = text.length(); pos < len; pos++) {
			char ch = text.charAt(pos);
			buf.append(ch == found ? replacement : ch);
		}
		return buf.toString();
	}
}
