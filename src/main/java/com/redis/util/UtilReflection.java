/*
 * Copyright 2017 the original author or authors.
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author <a href="https://github.com/takeseem/">杨浩</a>
 */
public class UtilReflection {
	private static final Method methodGetDeclaredMethods = ReflectionUtils.findMethod(ReflectionUtils.class, "getDeclaredMethods", Class.class);
	private static final Method methodGetDeclaredFields = ReflectionUtils.findMethod(ReflectionUtils.class, "getDeclaredFields", Class.class);
	
	private UtilReflection() {
        throw new AssertionError("No " + UtilReflection.class + " for you!");
    }
	
	public static ClassLoader getClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader == null ? UtilReflection.class.getClassLoader() : classLoader;
	}
	
	public static Class<?> classForName(String className) {
		return classForName(className, false);
	}
	public static Class<?> classForName(String className, boolean ignoreClassNotFoundException) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			if (ignoreClassNotFoundException) return null;
			throw new IllegalStateException(e);
		}
	}
	/** 如果class存在，执行action */
	public static void classForName(String className, Consumer<Class<?>> action) {
		URL url = getClassLoader().getResource(UtilString.replaceAll(className, '/', '.') + ".class");
		if (url == null) return;
		
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(className + ": " + url, e);
		}
		if (clazz != null) action.accept(clazz);
	}
	
	public static void setAccessible(AccessibleObject obj) {
		if (!obj.isAccessible()) obj.setAccessible(true);
	}
	
	public static Class<?> getLambdaTargetClass(Object lambda) {
		if (lambda == null) return null;
		
		Class<?> clazz = lambda.getClass();
		if (AnnotationUtils.findAnnotation(clazz, FunctionalInterface.class) == null) throw new IllegalArgumentException("Not lambda: " + lambda);
		
		int modifier = Modifier.STATIC | Modifier.PRIVATE;
		for (Method method : getDeclaredMethods(clazz)) {
			if ((method.getModifiers() & modifier) != 0) {
				return method.getParameterTypes()[0];
			}
		}
		return null;
	}
	public static String getLambdaMsg(Object lambda) {
		Class<?> targetClass = getLambdaTargetClass(lambda);
		if (targetClass == null) return String.valueOf(lambda);
		return targetClass.getName() + " <- " + lambda;
	}
	
	/** @see ReflectionUtils#invokeMethod(Method, Object, Object...) */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Method method, Object target, Object... args) {
		setAccessible(method);
		return (T) ReflectionUtils.invokeMethod(method, target, args);
	}
	/** @see ReflectionUtils#getDeclaredMethods(Class) */
	public static Method[] getDeclaredMethods(Class<?> clazz) {
		return invoke(methodGetDeclaredMethods, ReflectionUtils.class, clazz);
	}
	/** @see ReflectionUtils#getDeclaredFields(Class) */
	public static Field[] getDeclaredFields(Class<?> clazz) {
		return invoke(methodGetDeclaredFields, ReflectionUtils.class, clazz);
	}
	
	
	/** obj是clazz的实例对象 */
	public static boolean isInstance(Object obj, Class<?> clazz) {
		if (obj == null) return !clazz.isPrimitive();
		if (!clazz.isPrimitive()) return clazz.isInstance(obj);
		if (clazz == int.class) return obj instanceof Byte || obj instanceof Short || obj instanceof Integer;
		throw new IllegalStateException("暂时未实现 clazz=" + clazz + ", obj=" + obj);
	}
	public static <T> T newInstance(Constructor<T> constructor, Object... args) {
		return BeanUtils.instantiateClass(constructor, args);
	}
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className, Object... args) {
		Class<T> clazz = null;
		try {
			clazz = (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		return newInstance(clazz, args);
	}
	
	/** 如果构造器存在重载，结果依赖构造器顺序 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> clazz, Object... args) {	//NOSONAR
		if (args.length == 0) {
			try {
				return clazz.newInstance();
			} catch (ReflectiveOperationException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
		
		List<Constructor<?>> ctors = new ArrayList<>();
		for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
			if (match(args, ctor.getParameterTypes())) ctors.add(ctor); 
		}
		if (ctors.isEmpty()) throw new IllegalArgumentException("Not found Constructor in " + clazz + ", args=" + Arrays.toString(args));
		
		Collections.sort(ctors, (a, b) -> compare(a.getParameterTypes(), b.getParameterTypes()));
		return (T) newInstance(ctors.get(0), args);
		
	}
	
	/** @see ReflectionUtils#getField(Field, Object) */
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object target, Field field) {
		setAccessible(field);
		return (T) ReflectionUtils.getField(field, target);
	}
	/** @see ReflectionUtils#getField(Field, Object) */
	public static <T> T getField(Object target, String fieldName) {
		if (target == null) return null;
		
		Class<?> clazz = target instanceof Class ? (Class<?>) target : target.getClass();
		List<Field> fields = getDeclaredFields(clazz, f -> f.getName().equals(fieldName), 1);
		if (fields.isEmpty()) throw new IllegalArgumentException("field=null, name=" + fieldName + ", target=" + target);
		return getField(target, fields.get(0));
	}
	public static List<Field> getDeclaredFields(Class<?> clazz, Predicate<Field> predicate, int limit) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> searchType = clazz; searchType != null && searchType != Object.class;  searchType = searchType.getSuperclass()) {
			for (Field field : getDeclaredFields(searchType)) {
				if (predicate == null || predicate.test(field)) {	//NOSONAR
					fields.add(field);
					if (limit > 0 && fields.size() >= limit) return fields;	//NOSONAR
				}
			}
		}
		return fields;
	}
	
	/**
	 * 智能执行方法
	 * @param target 目标对象
	 * @param name	方法名(非空)
	 * @param args	参数(非空)
	 * @return
	 */
	public static <T> T invokeMethod(Object target, String name, Object... args) {
		if (target == null) return null;
		Class<?> clazz = target instanceof Class ? (Class<?>) target : target.getClass();
		return invoke(findMethod(clazz, name, args), target, args);
	}
	/**
	 * 智能查找方法
	 * @param target 目标对象
	 * @param methodName	方法名(非空)
	 * @param args	参数(非空)
	 */
	public static Method findMethod(Class<?> clazz, String name, Object... args) {	//NOSONAR
		List<Method> methods = new ArrayList<>();
		for (Class<?> searchType = clazz; searchType != null; searchType = searchType.getSuperclass()) {
			for (Method method : getDeclaredMethods(searchType)) {
				if (method.getName().equals(name) && match(args, method.getParameterTypes())) {
					methods.add(method);
				}
			}
		}
		if (methods.isEmpty()) throw new IllegalArgumentException("Not found method: " + name + " in " + clazz + ", args=" + Arrays.toString(args));
		
		Collections.sort(methods, (a, b) -> compare(a.getParameterTypes(), b.getParameterTypes()));
		return methods.get(0);
	}
	/** args与types类型精确匹配 */
	private static boolean match(Object[] args, Class<?>[] types) {
		if (args.length != types.length) return false;
		for (int i = 0, len = args.length; i < len; i++) {
			if (!isInstance(args[i], types[i])) return false;
		}
		return true;
	}
	/** 类型兼容比较，类型越具体越靠前 */
	private static int compare(Class<?>[] classes1, Class<?>[] classes2) {
		if (classes1 == classes2) return 0;
		for (int i = 0; i < classes1.length; i++) {
			int result = compare(classes1[i], classes2[i]);
			if (result != 0) return result;
		}
		return 0;
	}
	/** 类型兼容比较，类型越具体越靠前 */
	private static int compare(Class<?> class1, Class<?> class2) {
		if (class1 == class2) return 0;
		return class1.isAssignableFrom(class2) ? 1 : -1;
	}
}
