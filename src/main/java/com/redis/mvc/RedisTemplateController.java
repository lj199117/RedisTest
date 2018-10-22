package com.redis.mvc;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redis.cache.SentinelRedis;

public class RedisTemplateController {
	static final AtomicInteger i = new AtomicInteger(0);
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/conf/redisTemplate.xml");
		
	}
}
