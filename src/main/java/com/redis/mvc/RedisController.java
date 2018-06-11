package com.redis.mvc;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redis.cache.SentinelRedis;

public class RedisController {
	static final AtomicInteger i = new AtomicInteger(0);
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/conf/app.xml");
		SentinelRedis redisSvc = context.getBean(SentinelRedis.class);
		Thread t = new Thread(() -> {
			while(true) {
				String val = "key?" + i.incrementAndGet();
				redisSvc.set("key", val);
				System.out.println(val);
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
			}
		});
		t.start();
		t.join();
	}
}
