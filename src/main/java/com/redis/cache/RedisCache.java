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
package com.redis.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;

/**
 * @author <a href="mailto:yh@ustockex.com">杨浩</a>
 * @since 0.1.0
 */
public class RedisCache {
	protected final Logger logger = LogManager.getLogger(getClass());
	private @Autowired(required = false) JedisPool pool;
	private static final Long long1 = 1L;

	public void setPool(JedisPool pool) {
		this.pool = pool;
	}

	// key操作
	public void del(String... keys) {
		if (keys != null && keys.length > 0) {
			try (Jedis jedis = pool.getResource()) {
				jedis.del(keys);
			}
		}
	}

	/** @see Jedis#exists(String) */
	public boolean exists(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.exists(key);
		}
	}

	/**
	 * @param seconds
	 *            0立即过期
	 */
	public boolean expire(String key, int seconds) {
		try (Jedis jedis = pool.getResource()) {
			return long1.equals(jedis.expire(key, seconds));
		}
	}

	public Set<String> keys(final String pattern) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.keys(pattern);
		}
	}

	/**
	 * @param milliseconds
	 *            0立即过期
	 */
	public boolean pexpire(String key, int milliseconds) {
		return pexpire(key, (long) milliseconds);
	}

	/**
	 * @param milliseconds
	 *            0立即过期
	 */
	public boolean pexpire(String key, long milliseconds) {
		try (Jedis jedis = pool.getResource()) {
			return long1.equals(jedis.pexpire(key, milliseconds));
		}
	}

	public Long pttl(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.pttl(key);
		}
	}

	public boolean rename(String oldKey, String newKey) {
		try (Jedis jedis = pool.getResource()) {
			return "OK".equals(jedis.rename(oldKey, newKey));
		}
	}

	public boolean renamenx(String oldKey, String newKey) {
		try (Jedis jedis = pool.getResource()) {
			return "OK".equals(jedis.renamenx(oldKey, newKey));
		}
	}

	/** @see Jedis#sort(String) */
	public List<String> sort(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.sort(key);
		}
	}

	/** @see Jedis#sort(String) */
	public List<String> sort(String key, SortingParams sortingParameters) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.sort(key, sortingParameters);
		}
	}

	/** @see Jedis#sort(String, SortingParams, String) */
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.sort(key, sortingParameters, dstkey);
		}
	}

	public Long ttl(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.ttl(key);
		}
	}

	public String type(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.type(key);
		}
	}
	// end key操作

	// string操作
	public String get(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.get(key);
		}
	}

	/**
	 * getset可以和incr组合实现一个有原子性的复位操作的计数器
	 * 
	 * <pre>
	 * getset mycount 0 --> 返回当前的mycount值，注意mycount可能为null，并且返回的值是incr之前已经用过的
	 * </pre>
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @since 0.1.0
	 */
	public String getset(String key, String value) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.getSet(key, value);
		}
	}

	public Long incr(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.incr(key);
		}
	}

	public Long incr(String key, int expire) {
		try (Jedis jedis = pool.getResource()) {
			long value = jedis.incr(key);
			if (expire != -1) {
				// 设置过期时间
				jedis.expire(key, expire);
			}
			return value;
		}
	}

	/** 如果key不存在，将设置值，并设置过期时间；如果key存在，无动作 */
	public boolean setnx(String key, String value, int seconds) {
		boolean result;
		try (Jedis jedis = pool.getResource()) {
			result = long1.equals(jedis.setnx(key, value));
			if (result) {
				jedis.expire(key, seconds);
			}
		}
		return result;
	}

	public Long incrBy(String key, long integer) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.incrBy(key, integer);
		}
	}

	public Double incrByFloat(String key, double value) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.incrByFloat(key, value);
		}
	}

	public boolean set(String key, String value) {
		try (Jedis jedis = pool.getResource()) {
			return "OK".equals(jedis.set(key, value));
		}
	}

	public boolean setex(String key, int seconds, String value) {
		try (Jedis jedis = pool.getResource()) {
			return "OK".equals(jedis.setex(key, seconds, value));
		}
	}

	/**
	 * 往列表左边插入元素.
	 * 
	 * <pre>
	 * seconds -1-->不过期 0 -->立即过期 其他值-->每次都会重新计时，即覆盖之前的过期时间
	 * </pre>
	 * 
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @author longyang
	 * @date 2016年8月31日 下午8:04:40
	 */
	public boolean lpush(String key, int seconds, String... values) {
		boolean result;
		try (Jedis jedis = pool.getResource()) {
			result = long1 <= jedis.lpush(key, values);
			if (result && seconds != -1) {
				jedis.expire(key, seconds);
			}
		}
		return result;
	}

	/**
	 * 从左往右获取列表.
	 * 
	 * @param key
	 * @param start
	 *            列表起始，从0开始
	 * @param end
	 *            列表结束，-1表示到最后一个元素
	 * @return
	 * @author longyang
	 * @date 2016年8月31日 下午8:02:18
	 */
	public List<String> lrange(String key, long start, long end) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.lrange(key, start, end);
		}
	}

	/**
	 * 移除列表中的元素.
	 * 
	 * @param key
	 * @param count
	 *            count > 0 -->从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count<br>
	 *            count < 0 -->从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值<br>
	 *            count = 0 --> 移除表中所有与 VALUE 相等的值。
	 * @param value
	 * @author longyang
	 * @date 2016年9月1日 上午9:52:37
	 */
	public void lrem(String key, long count, String value) {
		try (Jedis jedis = pool.getResource()) {
			jedis.lrem(key, count, value);
		}
	}

	/**
	 * set集合加入元素.
	 * 
	 * seconds -1-->不过期 0 -->立即过期 其他值-->每次都会重新计时，即覆盖之前的过期时间
	 * 
	 * @param key
	 * @param seconds
	 * @param members
	 * @return
	 * @author longyang
	 * @date 2016年9月1日 上午11:03:22
	 */
	public boolean sadd(String key, int seconds, String... members) {
		boolean result;
		try (Jedis jedis = pool.getResource()) {
			result = long1 <= jedis.sadd(key, members);
			if (result && seconds != -1) {
				jedis.expire(key, seconds);
			}
		}
		return result;
	}

	/**
	 * 扫描set集合.
	 * 
	 * @param key
	 * @return
	 * @author longyang
	 * @date 2016年9月1日 上午11:07:30
	 */
	public Set<String> smembers(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.smembers(key);
		}
	}

	/**
	 * 从set集合中移除元素.
	 * 
	 * @param key
	 * @param members
	 * @author longyang
	 * @date 2016年9月1日 上午11:24:46
	 */
	public void srem(String key, String... members) {
		try (Jedis jedis = pool.getResource()) {
			jedis.srem(key, members);
		}
	}
	// end string操作
	
	public void hset(String key, String field, String value) {
		if (key == null || field == null) {
			return;
		}
		try (Jedis jedis = pool.getResource()) {
			jedis.hset(key, field, value);
		}
	}
	
	public boolean hsetnx(String key, String field, String value) {
		if (key == null || field == null) {
			return false;
		}
		try (Jedis jedis = pool.getResource()) {
			return jedis.hsetnx(key, field, value) == 1;
		}
	}
	
	public void hmset(String key, Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return;
		}
		try (Jedis jedis = pool.getResource()) {
			jedis.hmset(key, map);
		}
	}
	
	/**
	 * hmget
	 * @param key
	 * @param fields 如果给定的域不存在于哈希表，那么返回一个 nil 值
	 * @return 按照fields顺序返回list
	 * @since 0.1.0
	 */
	public List<String> hmget(String key, String... fields) {
		if (key == null || fields == null || fields.length == 0) {
			return null;
		}
		try (Jedis jedis = pool.getResource()) {
			return jedis.hmget(key, fields);
		}
	}
	
	public Set<String> hkeys(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hkeys(key);
		}
	}
	
	public Map<String, String> hgetAll(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hgetAll(key);
		}
	}
	
	/**
	 * 往列表右边插入元素.
	 * 
	 * <pre>
	 * seconds -1-->不过期 0 -->立即过期 其他值-->每次都会重新计时，即覆盖之前的过期时间
	 * </pre>
	 * 
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 */
	public boolean rpush(String key, int seconds, String... values) {
		boolean result;
		try (Jedis jedis = pool.getResource()) {
			result = long1 <= jedis.rpush(key, values);
			if (result && seconds != -1) {
				jedis.expire(key, seconds);
			}
		}
		return result;
	}
	
	/**
	 * 阻塞的从队列中去头元素<br/>
	 * 它是 LPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。
	 * @param seconds 超时时间 0永不超时
	 * @param keys 队列
	 * @return map key：队列名；value：该队列的头元素
	 * @since 0.1.0
	 */
	public Map<String, String> blpop(int seconds, String... keys) {
		Map<String, String> map = new HashMap<>();
		try (Jedis jedis = pool.getResource()) {
			List<String> list = jedis.blpop(seconds, keys);
			if (list == null || list.size() != 2) {
				return null;
			}
			map.put(list.get(0), list.get(1)); // 第一个元素是队列名，第二个元素是该队列的头元素
		}
		return map;
	}
}
