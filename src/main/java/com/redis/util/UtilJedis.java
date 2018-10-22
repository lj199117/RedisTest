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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JavaType;

import java.util.Set;
import java.util.TreeSet;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

/**
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 * @since 0.1.0
 */
public class UtilJedis {
	private static Long retTrue = 1l;
	private static final int defaultBatchSize = 200;

	public static int getBatchSize(Integer batchSize) {
		return batchSize == null || batchSize <= 0 ? defaultBatchSize : batchSize;
	}
	
	/// key操作
	public static String type(JedisPool pool, String key) {
		if (key == null) return null;
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.type(key);
		}
	}
	public static Set<String> keys(JedisPool pool, String pattern) {
		if (pattern == null) return new HashSet<>();
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.keys(pattern);
		}
	}
	public static boolean exists(JedisPool pool, String key) {
		if (key == null) return false;
		
		try (Jedis jedis = pool.getResource()) {
			return Boolean.TRUE.equals(jedis.exists(key));
		}
	}
	public static void del(JedisPool pool, String... keys) {
		if (keys == null || keys.length == 0) return;

		try (Jedis jedis = pool.getResource()) {
			jedis.del(keys);
		}
	}
	public static void del(JedisPool pool, Collection<?> keys) {
		del(pool, UtilCollection.toStringArrayDistinct(keys));
	}
	/** @return key的剩余生存时间（-2：key不存在, -1: 未设置过期, 其他：剩余的秒） */
	public static int ttl(JedisPool pool, String key) {
		if (key == null || key.isEmpty()) return -2;

		try (Jedis jedis = pool.getResource()) {
			return jedis.ttl(key).intValue();
		}
	}
	public static Map<String, String> mget(JedisPool pool, String... keys) {
		if (keys == null || keys.length == 0) return new HashMap<>();

		Map<String, String> result = new HashMap<>();
		List<String> values;
		try (Jedis jedis = pool.getResource()) {
			values = jedis.mget(keys);
		}
		
		for (int i = 0; i < keys.length; i++) {
			result.put(keys[i], values.get(i));
		}
		return result;
	}
	public static <T> Map<String, T> mget(JedisPool pool, Class<T> clazz, String... keys) {
		Map<String, T> result = new HashMap<>();
		for (Entry<String, String> entry : mget(pool, keys).entrySet()) {
			result.put(entry.getKey(), UtilJson.readValue(entry.getValue(), clazz));
		}
		return result;
	}
	public static Map<String, String> mget0(JedisPool pool, String... keys) {
		if (keys == null || keys.length == 0) return new HashMap<>();
		
		List<String> values;
		try (Jedis jedis = pool.getResource()) {
			values = jedis.mget(keys);
		}
		
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			result.put(keys[i], values.get(i));
		}
		return result;
	}
	public static Map<String, String> mget(JedisPool pool, Collection<String> keys, Integer batchSize) {
		if (keys == null || keys.size() == 0) return new HashMap<>();

		Map<String, String> result = new HashMap<>();
		for (Set<String> batch : UtilCollection.splitToSet(keys, getBatchSize(batchSize))) {
			result.putAll(mget0(pool, batch.toArray(new String[batch.size()])));
		}
		return result;
	}
	public static void psetex(JedisPool pool, String key, long milliseconds, final String value) {
		try (Jedis jedis = pool.getResource()) {
			jedis.psetex(key, milliseconds, value);
		}
	}
	/// end key操作

	public static void setex(JedisPool pool, String key, Object obj, int seconds) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			jedis.setex(key, seconds, value);
		}
	}
	/** 相比{@linkplain #setex(JedisPool, String, Object, int)}直接存储value到redis，而不是先转json */
	public static void setex0(JedisPool pool, String key, String value, int seconds) {
		try (Jedis jedis = pool.getResource()) {
			jedis.setex(key, seconds, value);
		}
	}

	/** 如果key不存在，将设置值，并设置过期时间；如果key存在，无动作 */
	public static boolean setnx(JedisPool pool, String key, Object obj, int seconds) {
		String value = UtilJson.writeValueAsString(obj);

		boolean result;
		try (Jedis jedis = pool.getResource()) {
			result = retTrue.equals(jedis.setnx(key, value));
			if (result) jedis.expire(key, seconds);
		}
		return result;
	}

	public static boolean setnx(JedisPool pool, String key, Object obj) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.setnx(key, value));
		}
	}

	public static boolean set(JedisPool pool, String key, Object obj) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.set(key, value));
		}
	}

	public static boolean hset(JedisPool pool, String key, String field, String value) {
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.hset(key, field, value));
		}
	}

	public static String hmset(JedisPool pool, String key, Map<String, String> hash) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hmset(key, hash);
		}
	}
	public static void hmsetPexpireAt(JedisPool pool, String key, Map<String, String> hash, long pexpireAtTime) {
		try (Jedis jedis = pool.getResource();
				Pipeline pipeline = jedis.pipelined();) {
			pipeline.hmset(key, hash);
			pipeline.pexpireAt(key, pexpireAtTime);
			pipeline.sync();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	/** 仅当值存在时才做所有操作 */
	public static boolean hmsetPexpireAtWhenExist(JedisPool pool, String key, Map<String, String> hash, long pexpireAtTime) {
		try (Jedis jedis = pool.getResource()) {
			if (retTrue.equals(jedis.pexpireAt(key, pexpireAtTime))) {
				jedis.hmset(key, hash);
				return true;
			}
		}
		return false;
	}
	/** 仅当值存在时才做所有操作 */
	public static boolean hmsetPexpireWhenExist(JedisPool pool, String key, Map<String, String> hash, long pexpireTime) {
		try (Jedis jedis = pool.getResource()) {
			if (retTrue.equals(jedis.pexpire(key, pexpireTime))) {
				jedis.hmset(key, hash);
				return true;
			}
		}
		return false;
	}

	public static boolean rpush(JedisPool pool, String key, Object obj) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.rpush(key, value));
		}
	}

	public static boolean lpush(JedisPool pool, String key, Object obj) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.lpush(key, value));
		}
	}

	public static boolean sadd(JedisPool pool, String key, Object obj) {
		return sadd0(pool, key, UtilJson.writeValueAsString(obj));
	}
	public static boolean sadd0(JedisPool pool, String key, String member) {
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.sadd(key, member));
		}
	}
	public static int scard(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.scard(key).intValue();
		}
	}
	public static boolean sismember(JedisPool pool, String key, String member) {
		if (key == null || member == null) return false;
		
		try (Jedis jedis = pool.getResource()) {
			return Boolean.TRUE.equals(jedis.sismember(key, member));
		}
	}

	public static boolean zadd(JedisPool pool, String key, Object obj, double score) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return retTrue.equals(jedis.zadd(key, score, value));
		}
	}

	public static <T> T get(JedisPool pool, String key, Class<T> clazz) {
		String json = get0(pool, key);
		return UtilString.isEmpty(json) ? null : UtilJson.readValue(json, clazz);
	}
	public static String get0(JedisPool pool, String key) {
		if (key == null) return null;
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.get(key);
		}
	}
	/** @return [string, long] */
	public static Object[] getPttl(JedisPool pool, String key) {
		if (key == null) return new Object[] {null, -2};
		
		try (Jedis jedis = pool.getResource();
				Pipeline pipeline = jedis.pipelined()) {
			pipeline.get(key);
			pipeline.pttl(key);
			return pipeline.syncAndReturnAll().toArray();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	public static long incr(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.incr(key);
		}
	}
	public static long[] incrPexpire(JedisPool pool, String key, long pexpire) {
		try (Jedis jedis = pool.getResource()) {
			long result = jedis.incr(key);
			if (result == 1) {
				jedis.pexpire(key, pexpire);
				return new long[] {result, pexpire};
			}
			
			long pttl = jedis.pttl(key);
			if (pttl > pexpire || pttl == -1) {
				jedis.pexpire(key, pexpire);
				return new long[] {result, pexpire};
			}
			return new long[] {result, pttl};
		}
	}

	public static String hget(JedisPool pool, String key, String field) {
		String json;
		try (Jedis jedis = pool.getResource()) {
			json = jedis.hget(key, field);
		}
		if (UtilString.isEmpty(json)) return null;
		return json;
	}

	public static Map<String, String> hgetAll(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			if (!jedis.exists(key)) return null;
			return jedis.hgetAll(key);
		}
	}
	public static <T> T hgetAll(JedisPool pool, String key, Class<T> clazz) {
		Map<String, String> hash;
		try (Jedis jedis = pool.getResource()) {
			hash = jedis.hgetAll(key);
		}
		return hash == null ? null : UtilJson.convertValue(hash, clazz);
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Map<String, String>> hgetAlls(JedisPool pool, Set<String> keys, Integer batchSize) {
		if (keys == null || keys.isEmpty()) return new HashMap<>();
		
		Map<String, Map<String, String>> result = new HashMap<>(keys.size());
		try (Jedis jedis = pool.getResource();
				Pipeline pipeline = jedis.pipelined()) {
			for (List<String> batch : UtilCollection.split(keys, getBatchSize(batchSize))) {
				for (String key : batch) {
					pipeline.hgetAll(key);
				}
				List<Object> values = pipeline.syncAndReturnAll();
				for (int i = 0, len = batch.size(); i < len; i++) {
					result.put(batch.get(i), (Map<String, String>) values.get(i));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}
	
	public static boolean hsetAlls(JedisPool pool, Map<String, Map<String, String>> map){
		try(Jedis jedis = pool.getResource()){
			Pipeline pipelined = jedis.pipelined();
			map.entrySet().forEach(entry -> {
				pipelined.hmset(entry.getKey(), entry.getValue());
			});
			pipelined.sync();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return true;
	}
	
	public static boolean del(JedisPool pool,Set<String> keys){
		try(Jedis jedis = pool.getResource()){
			Pipeline pipelined = jedis.pipelined();
			keys.forEach(key -> {
				pipelined.del(key);
			});
			pipelined.sync();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return true;
	}

	public static Long zcount(JedisPool pool, String key, String start, String end) {
		try (Jedis jedis = pool.getResource()) {
			if (null == start || null == end) {
				return null;
			}
			if (!jedis.exists(key)) return null;
			Long zcount = jedis.zcount(key,start, end);
			return zcount;
		}
	}

	public static <T> List<T> lrange(JedisPool pool, String key, long start, long end, Class<T> clazz) {
		List<String> list = new ArrayList<String>();
		try (Jedis jedis = pool.getResource()) {
			list = jedis.lrange(key, start, end);
		}
		if (list.size() == 0) return null;
		// 将List<String>转换为List<T>
		List<T> result = new ArrayList<T>();
		for (int i = 0; i < list.size(); i++) {
			result.add(UtilJson.readValue(list.get(i), clazz));
		}
		return result;
	}
	public static List<String> lrange0(JedisPool pool, String key, long start, long end) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.lrange(key, start, end);
		}
	}

	public static Long llen(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.llen(key);
		}
	}

	public static String lpop(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.lpop(key);
		}
	}
	
	public static void ltrim(JedisPool pool, String key, long start, long end) {
		try (Jedis jedis = pool.getResource()) {
			jedis.ltrim(key, start, end);
		}
	}

	public static <T> Set<T> smembers(JedisPool pool, String key, Class<T> clazz) {
		Set<String> set = smembers(pool, key);
		if (set.size() == 0) return null;
		// 将Set<String>转换为Set<T>
		Set<T> result = new TreeSet<T>();
		for (String json : set) {
			result.add(UtilJson.readValue(json, clazz));
		}
		return result;
	}
	public static Set<String> smembers(JedisPool pool, String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.smembers(key);
		}
	}
	public static void srem(JedisPool pool, String key, String... members) {
		if (members == null || members.length == 0) return;
		
		try (Jedis jedis = pool.getResource()) {
			jedis.srem(key, members);
		}
	}
	public static void srem(JedisPool pool, String key, Collection<?> members) {
		srem(pool, key, UtilCollection.toStringArrayDistinct(members));
	}

	public static <T> List<T> zrange(JedisPool pool, String key, long start, long end, Class<T> clazz) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrange(key, start, end);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), clazz));
		}
		return result;
	}
	public static Set<Tuple> zrangeWithScores0(JedisPool pool, String key, long start, long end) {
		if (key == null) return new HashSet<>();
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.zrangeWithScores(key, start, end);
		}
	}
	public static Set<Tuple> zrevrangeByScoreWithScores(JedisPool pool, String key, final double max,
		      final double min, final int offset, final int count) {
		if (key == null) return new HashSet<>();
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		}
	}

	public static <T> List<T> zrangeByScore(JedisPool pool, String key, String min, String max, Class<T> clazz) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrangeByScore(key, min, max);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), clazz));
		}
		return result;
	}
	
	public static <T> List<T> zrangeByScore(JedisPool pool, String key, double min, double max, int offset, int limit, Class<T> clazz) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrangeByScore(key, min, max, offset, limit);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), clazz));
		}
		return result;
	}
	public static <T> List<T> zrevRangeByScore(JedisPool pool, String key, double min, double max, int offset, int limit, Class<T> clazz) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrevrangeByScore(key, min, max, offset, limit);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), clazz));
		}
		return result;
	}
	
	public static <T> List<T> zrangeByScore(JedisPool pool, String key, double min, double max, int offset, int limit, JavaType type) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrangeByScore(key, min, max, offset, limit);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), type));
		}
		return result;
	}

	public static <T> List<T> zrevrange(JedisPool pool, String key, long start, long end, Class<T> clazz) {
		Set<String> set = new HashSet<String>();
		try (Jedis jedis = pool.getResource()) {
			set = jedis.zrevrange(key, start, end);
		}
		if (set.size() == 0) return null;
		List<T> result = new ArrayList<T>();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			result.add(UtilJson.readValue(iter.next(), clazz));
		}
		return result;
	}
	public static Set<Tuple> zrevrangeWithScores0(JedisPool pool, String key, long start, long end) {
		if (key == null) return new HashSet<>();
		
		try (Jedis jedis = pool.getResource()) {
			return jedis.zrevrangeWithScores(key, start, end);
		}
	}

	public static void zremrangeByScore(JedisPool pool, String key, String start, String end) {
		if (key == null) return;
		try (Jedis jedis = pool.getResource()) {
			jedis.zremrangeByScore(key, start, end);
		}
	}
	public static int zremrangeByScore(JedisPool pool, String key, double start, double end) {
		if (key == null) return 0;
		try (Jedis jedis = pool.getResource()) {
			return jedis.zremrangeByScore(key, start, end).intValue();
		}
	}

	//server指令
	public static String info(JedisPool pool) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.info();
		}
	}
	public static String info(JedisPool pool, String section) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.info(section);
		}
	}

}
