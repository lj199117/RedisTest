package com.redis.test.pool;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.cache.RedisCache;
import com.redis.test.junit.AbstractTest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
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
}
 