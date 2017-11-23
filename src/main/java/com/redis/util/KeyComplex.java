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

import java.io.Serializable;
import java.util.Arrays;

/**
 * 复合的hash key实现，严格意义比较，如果需要特殊（Number和Date特殊比较请使用{@link KeyComplex2}）
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 */
public class KeyComplex implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Object[] datas;
	
	public KeyComplex(Object... datas) {
		this.datas = datas;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> T get(int index) {
		return (T) datas[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(datas);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof KeyComplex)) return false;
		
		KeyComplex other = (KeyComplex) obj;
		return Arrays.equals(datas, other.datas);
	}

	@Override
	public String toString() {
		return "KeyComplex:" + Arrays.toString(datas);
	}
	
}
