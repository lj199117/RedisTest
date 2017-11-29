package com.redis.test.pool;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.cache.RedisCache;
import com.redis.test.junit.AbstractTest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.SortingParams;

public class RedisTest extends AbstractTest {

	@Autowired
	private JedisPool jedisPool;
	@Autowired
	private RedisCache redisCache;

	@Test
	public void testJedisPool() {
		try (Jedis jedis = jedisPool.getResource()){
			jedis.set("webull-1", "webull-1");
			
			String value = jedis.get("webull-1");
			System.out.println(value);
			Set<String> set = jedis.keys("*");
			System.out.println(set);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testJedisOperate() {
		// 删除key操作
		redisCache.del("webull-1");
		Set<String> set = redisCache.keys("*");
		System.out.println(set);
		
		redisCache.del("today_cost"); // 移除key
		redisCache.lpush("today_cost", -1, "12.1", "11" , "33", "6");
		SortingParams param = new SortingParams();
		param.by("alpha");
		List<String> list = redisCache.sort("today_cost", param);
		System.out.println(list);
		
		List<String> list1 = redisCache.sort("today_cost");
		System.out.println(list1);
	}
	
	private static final int HASH_EXPIRE = 30;
	private static final int KEYS_EXPIRE = 60;
	@Test
	public void testJedisMutiOperate() {
		String key1 = "key1";
		String key2 = "key2";
		try (Jedis jedis = jedisPool.getResource(); Pipeline pipeline = jedis.pipelined()) {
			Map<String, String> hash = new HashMap<>();
			hash.put("mKey", "mValue");
			pipeline.hmset(key1, hash);
			pipeline.expire(key1, HASH_EXPIRE);
			
			pipeline.sadd(key2, key2);
			pipeline.expire(key2, KEYS_EXPIRE);
			pipeline.sync();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		Set<String> set = redisCache.keys("*");
		System.out.println(set);
	}
	
	
	@Test
	public void testShowAllKeys() {
		Set<String> set = redisCache.keys("*");
		System.out.println(set);
	}
}
 