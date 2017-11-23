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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 * @since 0.1.0
 */
public class UtilLog {
	private static Map<String, Logger> loggers = new ConcurrentHashMap<>();
	
	
	/** 所有的日志记录器都是用Log4j2. (注意： 必须在使用任何logger前先调用此方法，否则某些日志框架可能不生效) */
	public static void init() {
		// log4j2关闭shutdownHook功能，由应用自己控制何时关闭
		UtilSys.setPropIfEmpty("log4j.shutdownHookEnabled", "false");
		
		// c3p0 - 详见takeseem-framework: classpath:mchange-commons.properties，采用com.mchange.v2.log.log4j.Log4jMLog
//		UtilSys.setPropertyIfEmpty("com.mchange.v2.log.MLog", "com.mchange.v2.log.log4j.Log4jMLog");	//NOSONAR
		
		// JUL: 必须在访问java.util.logging.LogManager.manager(源码static块初始化)前设置-Djava.util.logging.manager，否则还是jdk默认实现
		UtilSys.setPropIfEmpty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		
		// vertx logging
		UtilSys.setPropIfEmpty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
		
		// netty4
		UtilReflection.classForName("io.netty.util.internal.logging.InternalLoggerFactory", clazz -> {
			Class<?> factoryClass = UtilReflection.classForName("io.netty.util.internal.logging.Log4J2LoggerFactory");
			Object factory = UtilReflection.getField(factoryClass, "INSTANCE");
			UtilReflection.invokeMethod(clazz, "setDefaultFactory", factory);
		});
		// netty3
		UtilReflection.classForName("org.jboss.netty.logging.InternalLoggerFactory", clazz -> {
			Object factory = UtilReflection.newInstance("org.jboss.netty.logging.Slf4JLoggerFactory");
			UtilReflection.invokeMethod(clazz, "setDefaultFactory", factory);
		});
	}
	
	/** 关闭log4j2 {@link LogManager#shutdown()} */
	public static void shutdown() {
		LogManager.shutdown();
	}
	
	/**
	 * 提供cache的logger获取，因{@linkplain LogManager#getLogger(String)}耗时约2.4ms，在大量动态获取logger时性能不够
	 * @return name空时，返回=log.${name}
	 */
	public static Logger getLogger(String name) {
		if (name == null || name.isEmpty()) name = "log." + name;
		return loggers.computeIfAbsent(name, key -> LogManager.getLogger(key));
	}
	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}
	
	/**
	 * 根据路径来获取日志记录器
	 */
	public static Logger getLoggerByPath(String rootName, String pathInfo) {
		String pathName = pathToName(pathInfo);
		if (rootName == null || rootName.isEmpty()) return getLogger(pathName);
		return getLogger(rootName + '.' + pathName);
	}
	/** 路径转换为logger的点分割名称，去掉首尾的点 */
	private static String pathToName(String pathInfo) {
		if (pathInfo == null || pathInfo.isEmpty()) return pathInfo;
		
		StringBuilder buf = new StringBuilder();
		for (int i = 0, len = pathInfo.length(); i < len; i++) {
			char ch = pathInfo.charAt(i);
			if (ch == '/' || ch == '\\') ch = '.';
			buf.append(ch);
		}
		if (buf.charAt(buf.length() - 1) == '.') buf.deleteCharAt(buf.length() - 1);
		if (buf.length() > 0 && buf.charAt(0) == '.') return buf.substring(1);
		return buf.toString();
	}
	
}
