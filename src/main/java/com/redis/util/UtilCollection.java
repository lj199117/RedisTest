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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 * @since 0.1.0
 */
public class UtilCollection {
	
	/** 注意集合不能为空 */
	public static <T> T[] toArray(Collection<T> collection) {
		Class<?> elClass = collection.iterator().next().getClass();
		return toArray(collection, elClass);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<T> collection, Class<?> elClass) {
		return collection.toArray((T[]) Array.newInstance(elClass, collection.size()));
	}
	
	public static boolean notEmpty(Collection<?> coll) {
		return coll != null && !coll.isEmpty();
	}
	public static boolean isEmpty(Collection<?> coll) {
		return coll == null || coll.isEmpty();
	}
	public static boolean notEmpty(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	
	
	public static <T> List<Set<T>> splitToSet(Collection<T> values, int size) {
		if (isEmpty(values)) return new ArrayList<>();
		
		List<Set<T>> result = new ArrayList<>(values.size() / size + 1);
		Set<T> tmp = new HashSet<>(size);
		for (T value : new LinkedHashSet<>(values)) {
			tmp.add(value);
			if (tmp.size() >= size) {
				result.add(tmp);
				tmp = new HashSet<>(size);
			}
		}
		if (!tmp.isEmpty()) result.add(tmp);
		return result;
	}
	public static <T> List<List<T>> split(Collection<T> values, int size) {
		if (isEmpty(values)) return new ArrayList<>();
		
		List<List<T>> result = new ArrayList<>(values.size() / size + 1);
		List<T> tmp = new ArrayList<>(size);
		for (T value : values) {
			tmp.add(value);
			if (tmp.size() >= size) {
				result.add(tmp);
				tmp = new ArrayList<>(size);
			}
		}
		if (!tmp.isEmpty()) result.add(tmp);
		return result;
	}
	
	/** 返回两个map不匹配的数据 */
	public static <K, V> Map<K, V> getChanged(Map<K, V> src, Map<K, V> dst) {
		Set<K> all = new HashSet<>(src.keySet());
		all.addAll(dst.keySet());
		
		Map<K, V> result = new HashMap<>();
		for (K key : all) {
			V srcV = src.get(key), dstV = dst.get(key);
			if (!UtilObj.equals(srcV, dstV)) {	//存在差异
				if (dstV != null) result.put(key, dstV);
			}
		}
		return result;
	}
	
	public static Integer size(Map<?, ?> map) {
		return map == null ? null : map.size();
	}
	public static Integer size(Collection<?> collection) {
		return collection == null ? null : collection.size();
	}
	
	/** 如果本身是set，直接返回 */
	public static <T> Set<T> toSet(Collection<T> olds) {
		return olds == null ? null : olds instanceof Set ? (HashSet<T>) olds : new HashSet<>(olds);
	}
	/** 如果本身是set，直接返回 */
	public static <T> Map<T, T> toMap(Collection<T> elements) {
		if (elements == null) return null;
		
		Map<T, T> map = new HashMap<>();
		for (T element : elements) {
			map.put(element, element);
		}
		return map;
	}
	
	/** @return [增加的, 删除的, 更新的] */
	@SuppressWarnings("unchecked")
	public static <T> Set<T>[] getDiff(Collection<T> olds, Collection<T> news) {
		Set<T> add = new HashSet<>(), del = new HashSet<>(), update = new HashSet<>();
		if (olds == null || olds.isEmpty()) {
			add = new HashSet<>(news);
		} else if (news == null || news.isEmpty()) {
			del = new HashSet<>(olds);
		} else {
			Set<T> all = new HashSet<>(olds);
			all.addAll(news);
			Map<T, T> oldMap = toMap(olds), newMap = toMap(news);
			for (T element : all) {
				T old = oldMap.get(element), n = newMap.get(element);
				if (UtilObj.equals(old, n)) continue;
				
				if (old == null) {
					add.add(element);
				} else if (n == null) {
					del.add(element);
				} else {
					update.add(element);
				}
			}
		}
		
		Set<T>[] result = (Set<T>[]) Array.newInstance(Set.class, 3);
		result[0] = add;
		result[1] = del;
		result[2] = update;
		return result;
	}
	
	public static <T> T firstKey(TreeMap<T, ?> tree) {
		return tree == null || tree.isEmpty() ? null : tree.firstKey();
	}
	public static <T> T lastKey(TreeMap<T, ?> tree) {
		return tree == null || tree.isEmpty() ? null : tree.lastKey();
	}
	
	public static String[] toStringArray(Collection<?> elements) {
		return toStrings(elements, new ArrayList<>());
	}
	public static String[] toStringArrayDistinct(Collection<?> elements) {
		return toStrings(elements, new HashSet<>());
	}
	public static <T extends Collection<String>> String[] toStrings(Collection<?> elements, T collect) {
		for (Object element : elements) {
			if (element != null) collect.add(element.toString());
		}
		return collect.toArray(new String[collect.size()]);
	}
	
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C sub(C elements, int fromIndex, int toIndex) {
		if (elements == null) return null;

		Class<?> clazz = elements.getClass();
		C result;
		if (clazz.getName().equals("java.util.HashMap$KeySet")) {
			clazz = HashSet.class;
		} else if (clazz.getName().equals("java.util.HashMap$Values")) {
			clazz = ArrayList.class;
		}
		try {
			result = (C) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		if (elements.isEmpty()) return result;
		
		int pos = 0;
		for (T el : elements) {
			if (pos >= toIndex) break;
			if (pos >= fromIndex) result.add(el);
			pos++;
		}
		return result;
	}
	
	/**
	 * 用map的数据更新old
	 * @return 被替换的数据
	 */
	public static <K, V> Map<K, V> update(Map<K, V> old, Map<K, V> map) {
		Map<K, V> result = new HashMap<>();
		if (old == map) return result;
		
		for (Entry<K, V> entry : map.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			if (UtilObj.equals(old.get(key), value)) continue;
			
			result.put(key, old.put(key, value));
		}
		return result;
	}
	
	public static int[] tointArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer value : list) {
			ret[i++] = value;
		}
		return ret;
	}
	
	public static <K, V> Map<V, K> inverse(Map<K, V> map) {
		Map<V, K> result = new HashMap<>();
		for (Entry<K, V> entry : map.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		return result;
	}
	
	public static List<byte[]> split(byte[] bytes, int size) {
		List<byte[]> result = new ArrayList<>(size);
		if (bytes == null) return result;
		if (size == 1 && bytes.length == 0) {
			result.add(bytes);
			return result;
		}
		
		int len = bytes.length / size;
		if (len * size < bytes.length) len++;
		
		for (int i = 0, from = 0, to = 0; i < size && to < bytes.length; i++) {
			to = from + len;
			if (to > bytes.length) to = bytes.length;
			result.add(Arrays.copyOfRange(bytes, from, to));
			from = to;
		}
		return result;
	}
}
