/*
 * Copyright 2014-2026 the original author or authors.
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
package com.redis.test.junit;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

/**
 * 基于WEB的测试基类
 * 
 * @author <a href="mailto:yanghao@01mi.net">杨浩</a>
 */
@ContextHierarchy({ @ContextConfiguration({ "classpath:/conf/app.xml", "classpath:/junit-app.xml" }),
		@ContextConfiguration({ "classpath:/junit-servlet.xml" }) })
public abstract class WebTest extends AbstractWebTest {
}
