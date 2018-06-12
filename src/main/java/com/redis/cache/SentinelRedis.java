package com.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;

@Service
public class SentinelRedis {
	@Autowired JedisSentinelPool jedisSentinelPool;
	
	public void set(String key, Object obj) {
        System.out.println("master host:" + jedisSentinelPool.getCurrentHostMaster());
        try {
            Utils.set(jedisSentinelPool, key, obj);
        } catch (JedisException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            if(jedisSentinelPool != null)
                jedisSentinelPool.close();
        }
    }
}
