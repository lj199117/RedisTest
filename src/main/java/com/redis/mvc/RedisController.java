package com.redis.mvc;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redis.cache.SentinelRedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisController {
	static final AtomicInteger i = new AtomicInteger(0);
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/conf/app.xml");
//		testSentinel(context);
		testRedis(context);
	}
	
	private static void testRedis(ApplicationContext context) throws Exception {
		JedisPool pool = context.getBean(JedisPool.class);
		try (Jedis jedis = pool.getResource()){
			jedis.set("webull-1", "webull-1");
			
			String value = jedis.get("webull-1");
			System.out.println(value);
			Set<String> set = jedis.keys("*");
			System.out.println(set);
			
			try {
				Thread.sleep(10000);
			} catch (Exception e) {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void testSentinel(ApplicationContext context) throws Exception {
		SentinelRedis redisSvc = context.getBean(SentinelRedis.class);
		Thread t = new Thread(() -> {
			while(true) {
				String val = "key?" + i.incrementAndGet();
				redisSvc.set("key", val);
				System.out.println(val);

				try {
					Thread.sleep(10000);
				} catch (Exception e) {}
			}
		});
		t.start();
		t.join();
	}
	
	
}
