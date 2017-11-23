package com.redis.test.pool;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.test.junit.AbstractTest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTest extends AbstractTest {

	@Autowired
	private JedisPool jedisPool;

	@Test
	public void testJedisPool() {
		Jedis jedis = jedisPool.getResource();
		jedis.set("webull-1", "webull-1");
		
		String value = jedis.get("webull-1");
		System.out.println(value);
	}
}
