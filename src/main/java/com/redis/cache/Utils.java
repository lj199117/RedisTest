package com.redis.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.StringUtils;

import com.redis.util.UtilJson;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;
import redis.clients.util.Pool;

public class Utils {

	/**
	 * 将以逗号分隔的字符串转化成Set集合
	 * 
	 * @param elementStr
	 * @return
	 */
	public static Set<String> asSet(String elementStr) {
		if (StringUtils.isEmpty(elementStr)) {
			return Collections.emptySet();
		}
		String[] elements = elementStr.split("[,]");
		Set<String> set = new HashSet<>();
		for (String element : elements) {
			set.add(element);
		}
		return set;
	}

	public static String get0(Pool<Jedis> pool, String key) {
		if (key == null) {
			return null;
		} else {
			Jedis jedis = pool.getResource();
			Throwable var3 = null;

			String var4;
			try {
				var4 = jedis.get(key);
			} catch (Throwable var13) {
				var3 = var13;
				throw var13;
			} finally {
				if (jedis != null) {
					if (var3 != null) {
						try {
							jedis.close();
						} catch (Throwable var12) {
							var3.addSuppressed(var12);
						}
					} else {
						jedis.close();
					}
				}

			}

			return var4;
		}
	}

	public static boolean set(Pool<Jedis> pool, String key, Object obj) {
		String value = UtilJson.writeValueAsString(obj);
		try (Jedis jedis = pool.getResource()) {
			return "OK".equals(jedis.set(key, value));
		}
	}

	public static String hmset(Pool<Jedis> pool, String key, Map<String, String> hash) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.hmset(key, hash);
		}
	}

	public static boolean del(Pool<Jedis> pool, Set<String> keys){
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
	
	public static void main(String[] args) {
		try(JedisSentinelPool pool = new JedisSentinelPool("mymaster", asSet("192.168.5.128:26379,192.168.5.128:6380,192.168.5.128:6381"),
				new GenericObjectPoolConfig(), 1000, null);
			Jedis jedis = pool.getResource()) {
			jedis.set("lj", "xf");
		}
		
	}
}