/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redis.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * 异常统计器
 * @author <a href="mailto:yh@webull.com">杨浩</a>
 */
public class ExceptionCounter {
	protected static final Logger logger = LogManager.getLogger(ExceptionCounter.class);
	public static ExceptionCounter global= new ExceptionCounter();
	
	private @Value("${exceptionCounter.timeWindow:3600000}") int timeWindow = 3600 * 1000;
	private @Value("${exceptionCounter.exCountMax:5}") int exCountMax = 5;
	private volatile long exCountEndTime;
	private final Map<String, AtomicInteger> exCountMap = new ConcurrentHashMap<>();
	
	
	private int count(String key) {
		long now = System.currentTimeMillis();
		if (now > exCountEndTime) {	//过去1小时了，重新统计
			exCountEndTime = now + timeWindow;
			exCountMap.clear();
		}
		
		AtomicInteger count = exCountMap.computeIfAbsent(key, k -> new AtomicInteger());
		return count.incrementAndGet();
	}
	
	
	/** 统计：timeWindow内相同异常数，如果在限制内，即：未超出限制数 */
	public boolean inLimit(Exception e) {
		try {
			StackTraceElement[] eles = e.getStackTrace();
			if (eles.length == 0) return count(e.getClass().getName()) <= exCountEndTime;
			return count(e.getStackTrace()[0].toString()) <= exCountMax;
		} catch (Exception ex) {
			logger.warn("逻辑外异常:{}", ex, ex);
			return false;
		}
	}
	
	/** 限制内，即：未超出限制数 */
	public boolean inLimit(String key) {
		try {
			return count(key) <= exCountMax;
		} catch (Exception ex) {
			logger.warn("逻辑外异常:{}", ex, ex);
			return false;
		}
	}
}
