package com.github.diamond.client.extend.utils;


public class StringConverter {

	public static Object convertObjectFromString(Class<?> type, String value) {
		if (type == String.class) {
			return value;
		} else if (type == Float.class || type == float.class) {
			return Float.valueOf(value);
		} else if (type == Double.class || type == double.class) {
			return Double.valueOf(value);
		} else if (type == Byte.class || type == byte.class) {
			return Byte.valueOf(value);
		} else if (type == Short.class || type == short.class) {
			return Short.valueOf(value);
		} else if (type == Integer.class || type == int.class) {
			return Integer.valueOf(value);
		} else if (type == Long.class || type == long.class) {
			return Long.valueOf(value);
		} else if (type == Character.class || type == char.class) {
			return Character.valueOf(value.toCharArray()[0]);
		} else if (type == Boolean.class || type == boolean.class) {
			return Boolean.valueOf(value);
		}else{
			throw new RuntimeException("DRM 不支持此类型的资源");
		}
	}
}
