package com.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisSentinelPool;

@Service
public class SentinelRedis {
	@Autowired JedisSentinelPool jedisSentinelPool;
	
	public void set(String key, Object obj) {
		Utils.set(jedisSentinelPool, key, obj);
	}
}
