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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 对象工具方法
 * 
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 */
public class UtilObj {
	
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {	//NOSONAR
		if (obj == null) return true;
		if (obj instanceof String) return ((String) obj).isEmpty();
		if (obj instanceof Collection) return ((Collection) obj).isEmpty();
		if (obj instanceof Map) return ((Map) obj).isEmpty();
		if (obj.getClass().isArray()) return Array.getLength(obj) == 0;
		throw new IllegalArgumentException("isEmpty(" + obj + ")");
	}
	
	/** use {@linkplain UtilReflection#getClassLoader()} */
	@Deprecated
	public static ClassLoader getClassLoader() {
		return UtilReflection.getClassLoader();
	}

	private static Field getField(Class<?> clazz, String fieldName) throws IllegalArgumentException {
		if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException(clazz + " not found fieldName=" + fieldName);
		}
		if (clazz.isInterface()) {
			throw new IllegalArgumentException(clazz + " not found fieldName=" + fieldName);
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}

		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null && !superClass.isInterface()) {
			return getField(superClass, fieldName);
		}
		throw new IllegalArgumentException(clazz + " not found fieldName=" + fieldName);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object bean, String fieldName) {
		if (bean == null) {
			return null;
		}

		Class<?> clazz = bean.getClass();
		try {
			Field field = getField(clazz, fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return (T) field.get(bean);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("在 " + bean + " 中找不到field：" + fieldName, e);
		}
	}

	public static void setFieldValue(Object bean, String fieldName, Object value) {
		if (bean == null) {
			return;
		}

		Class<?> clazz = bean.getClass();
		try {
			Field field = getField(clazz, fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(bean, value);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("在 " + bean + " 中找不到field：" + fieldName, e);
		}
	}

	public static Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		String v = value.toString().trim();
		if (v.isEmpty()) {
			return null;
		}
		return Integer.parseInt(v);
	}

	public static Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Long) {
			return (Long) value;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		String v = value.toString().trim();
		if (v.isEmpty()) {
			return null;
		}
		return Long.parseLong(v);
	}

	public static BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		if (value instanceof Float) {
			return BigDecimal.valueOf((Float) value);
		}
		if (value instanceof Double) {
			return BigDecimal.valueOf((Double) value);
		}
		if (value instanceof Number) {
			return BigDecimal.valueOf(((Number) value).longValue());
		}
		return new BigDecimal(value.toString());
	}

	public static BigDecimal toBigDecimal(Object value, int scale, RoundingMode mode) {
		BigDecimal result = toBigDecimal(value);
		return result == null ? null : result.setScale(scale, mode);
	}

	/**
	 * 获取对象属性
	 * 
	 * @return bean: null，返回null；bean：Map，返回Map.get；其他直接反射readMethod执行
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T getPropValue(Object bean, Object prop) {
		if (bean == null) {
			return null;
		}
		if (bean instanceof Map) {
			return (T) ((Map) bean).get(prop);
		}

		Class<?> clazz = bean.getClass();
		PropertyDescriptor propDesc = BeanUtils.getPropertyDescriptor(clazz, prop.toString());
		// if (propDesc == null) throw new IllegalArgumentException("Property[" + prop + "] not in " + bean);
		Method getter = propDesc.getReadMethod();
		// if (readMethod == null) throw new IllegalArgumentException("Property[" + prop + "] getter not in " + bean);
		if (!Modifier.isPublic(getter.getDeclaringClass().getModifiers())) {
			getter.setAccessible(true);
		}
		try {
			return (T) getter.invoke(bean);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Property[" + prop + "] getter invoke in " + bean, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getValueByPath(Object root, int pos, Object[] paths) {
		if (paths.length == 0) {
			return (T) root;
		}
		final Object key = paths[pos], value = getPropValue(root, key);
		if (value == null) {
			return null;
		}
		if (pos == paths.length - 1) {
			return (T) value;
		}
		return getValueByPath(value, pos + 1, paths);
	}

	/**
	 * 从root对象根开始，获取属性值
	 * 
	 * @param root 值开始根
	 * @param keyPaths 属性路径
	 * @return 结果值
	 */
	public static <T> T getValueByPath(Object root, Object... keyPaths) {
		return getValueByPath(root, 0, keyPaths);
	}

	/**
	 * 设置属性
	 * 
	 * @param bean 非null
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setPropValue(Object bean, Object prop, Object value) {
		if (bean instanceof Map) {
			((Map) bean).put(prop, value);
			return;
		}

		Class<?> clazz = bean.getClass();
		PropertyDescriptor propDesc = BeanUtils.getPropertyDescriptor(clazz, prop.toString());
		// if (propDesc == null) throw new IllegalArgumentException("Property[" + prop + "] not in " + clazz);
		Method setter = propDesc.getWriteMethod();
		// if (method == null) throw new IllegalArgumentException("Property[" + prop + "] setter not in " + clazz);
		if (!Modifier.isPublic(setter.getDeclaringClass().getModifiers())) {
			setter.setAccessible(true);
		}
		try {
			setter.invoke(bean, value);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Property[" + prop + "] setter invoke in " + bean, e);
		}
	}

	/**
	 * @param pos 当前设置路径位置
	 * @param paths 路径key，可以是你所期望的任意对象
	 * @see #setValueByPath(Object, Object, Object...)
	 */
	private static void setValueByPath(Object root, Object value, final int pos, Object[] paths) {
		final Object key = paths[pos];
		if (pos == paths.length - 1) {
			setPropValue(root, key, value);
			return;
		}

		Object nextRoot = getPropValue(root, key);
		if (nextRoot == null) {
			nextRoot = new HashMap<>();
			setPropValue(root, key, nextRoot);
		}

		setValueByPath(nextRoot, value, pos + 1, paths);
	}

	/**
	 * 设置数据值
	 * 
	 * @param root 数据根（非空）
	 * @param value 要存储的数据
	 * @param paths 数据的路径（非空）可以是你所期望的任意对象
	 */
	public static void setValueByPath(Object root, Object value, Object... paths) {
		setValueByPath(root, value, 0, paths);
	}

	/** 考虑了null的{@linkplain Object#equals(Object)} */
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
		}
		
		if (obj1 instanceof Date && obj2 instanceof Date) {
			return ((Date) obj1).getTime() == ((Date) obj2).getTime();
		}
		return obj1.equals(obj2);
	}

	public static boolean equals(byte[] arr1, byte[] arr2) {
		if (arr1 == arr2) {
			return true;
		}
		if (arr1 == null || arr2 == null || arr1.length != arr2.length) {
			return false;
		}
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}
		return true;
	}

	/** 对象比较 (null转换为无穷大) */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return 0;
		}
		if (obj1 == null) {
			return 1;
		}
		if (obj2 == null) {
			return -1;
		}
		if (obj1 instanceof Comparable) {
			return ((Comparable) obj1).compareTo(obj2);
		}
		if (obj2 instanceof Comparable) {
			return -((Comparable) obj2).compareTo(obj1);
		}
		return obj1.toString().compareTo(obj2.toString());
	}

	public static boolean isTrue(Boolean value) {
		return Boolean.TRUE.equals(value);
	}

	public static <T, C extends Collection<? extends T>> C fillTo(Collection<?> beans, String fieldName, C target) {
		for (Object bean : beans) {
			target.add(getFieldValue(bean, fieldName));
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return (T) field.get(clazz);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalArgumentException(clazz + " field=" + fieldName, e);
		}
	}
	
	public static int mod(byte[] data, int size) {
		return Arrays.hashCode(data) % size;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object target, String methodName, Object... args) {
		if (target == null) return null;
		
		Class<?> clazz = target.getClass();
		List<Method> methods = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
				for (int i = 0, len = method.getParameterTypes().length; i < len; i++) {
					Class<?> argClass = method.getParameterTypes()[i];
					Object arg = args[i];
					if (arg != null && !argClass.isInstance(arg)) continue;
				}
				methods.add(method);
			}
		}
		if (methods.isEmpty() || methods.size() > 1) throw new IllegalArgumentException(target + ", method=" + methodName + ", args=" + Arrays.toString(args));

		Method method = methods.get(0);
		if (!method.isAccessible()) method.setAccessible(true);
		return (T) ReflectionUtils.invokeMethod(method, target, args);
	}
	
	/** 获取clazz类中静态字段类型是指定fieldType类型的值 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> getStaticFieldValues(Class<?> clazz, Class<T> fieldType) {
		try {
			Map<String, T> result = new HashMap<>();
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())
						&& fieldType.isAssignableFrom(field.getType())) {
					result.put(field.getName(), (T) field.get(clazz));
				}
			}
			return result;
		} catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}
		throw new IllegalStateException("Should never get here");
	}
}
