package com.redis.test.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.redis.test.junit.AbstractTest2;
import com.redis.util.UtilJedis;
import com.redis.util.UtilJson;
import com.redis.util.UtilString;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

public class RedisTemplateTest extends AbstractTest2 {

	@Autowired @Qualifier("redisTemplateDefault")
	private RedisTemplate<String, String> redisTemplateDefault;
	@Autowired @Qualifier("redisTemplateString")
	private RedisTemplate<String, String> redisTemplateString;
	@Autowired
	private JedisPool jedisPool;
	
	@Test
    public void testRedisObj() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("123", "hello");
        properties.put("abc", 456);
    
        redisTemplateDefault.opsForHash().putAll("hash", properties);
    
        Map<Object, Object> ans = redisTemplateDefault.opsForHash().entries("hash");
        System.out.println("ans: " + ans);
        
        // redisTemplateString
        redisTemplateString.opsForHash().putAll("hashStr", properties);
        Map<Object, Object> hashStr = redisTemplateString.opsForHash().entries("hashStr");
        System.out.println("hashStr: " + hashStr);
    }
	@Test
	public void testZset() {
		// 按照值来排序的取值而不是根据分数
		Set<String> set = redisTemplateString.boundZSetOps("TimelineRankTask:Rank:timelineRank")
				.rangeByLex(Range.range().lt(1000).gt(500), Limit.limit().count(20).offset(0));
		System.out.println("set: " + set);
		
		// 按照分数来排序取值
		Set<String> set1 = redisTemplateString.boundZSetOps("TimelineRankTask:Rank:timelineRank")
				.reverseRangeByScore(190, 200);
		System.out.println("set1: " + set1);
	}
	
	private static final double hotMinScore = 300; // 热度视频的最小得分定义
	private static final int pageSize = 20;
	private static final JavaType hashMapType = UtilJson.getTypeFactory().constructParametricType(HashMap.class, String.class, String.class);
	@Test
	public void testMockFetchHotPage() {
		for (int i = 0; i < 100; i++) {
			doIt();
		}
	}
	private void doIt() {
		Double curScore = Double.MAX_VALUE;
		// 从用户缓存拿到用户看过的最大得分的视频
		String key = "User:FetchHot:" + 10001;
		BoundHashOperations<String, String, String> bo=redisTemplateString.boundHashOps(key);
		String timelineConfig = bo.get("timeline");
		Map<String, String> config = new HashMap<>();
		if(!UtilString.isEmpty(timelineConfig)) {
			config = UtilJson.readValue(timelineConfig, hashMapType);
			
			curScore = Double.parseDouble(config.get("curScore"));
		}
		
		Set<Tuple> set1 = UtilJedis.zrevrangeByScoreWithScores(jedisPool, "TimelineRankTask:Rank:timelineRank", 
				curScore, hotMinScore, !UtilString.isEmpty(timelineConfig) ? 1 : 0, pageSize);
		double minScore = Double.MAX_VALUE;
		for (Tuple set : set1) {
			System.out.print(set.getElement()+",");
			if(minScore > set.getScore())
				minScore = set.getScore();
		}
		System.out.println("minScore="+minScore);
		if(curScore.compareTo(minScore) == 0) {
			minScore = Double.MAX_VALUE;
		}
		config.put("curScore", String.valueOf(minScore));
//		config.put("isDesc", "true");
		bo.put("timeline", UtilJson.writeValueAsString(config));
	}
	
	
}

 