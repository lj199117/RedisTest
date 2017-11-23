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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

/**
 * @author <a href="mailto:yh@webull.com">杨浩</a>
 */
public class UtilSpring {
	protected static final Logger logger = LogManager.getLogger(UtilSpring.class);

	public static void printTask(ThreadPoolTaskScheduler scheduler) {
		for (Runnable run : scheduler.getScheduledThreadPoolExecutor().getQueue().toArray(new Runnable[0])) {
			if (run instanceof FutureTask) {
				Callable<?> callable = UtilObj.getFieldValue(run, "callable");
				DelegatingErrorHandlingRunnable runnable = UtilObj.getFieldValue(callable, "task");
				Object delegate = UtilObj.getFieldValue(runnable, "delegate"); 
				if (delegate instanceof ScheduledMethodRunnable) {
					ScheduledMethodRunnable methodRunnable = (ScheduledMethodRunnable) delegate;
					Object target = methodRunnable.getTarget();
					Method method = methodRunnable.getMethod();
					logger.info("{}.{}({})", target.getClass().getSimpleName(), method.getName(), (Object) method.getParameterTypes());
				}
			}
		}
	}
}
