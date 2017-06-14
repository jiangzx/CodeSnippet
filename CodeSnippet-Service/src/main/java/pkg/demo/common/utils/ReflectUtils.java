package pkg.demo.common.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.Lists;

import pkg.demo.common.pojo.CustomException;

public class ReflectUtils {

	public static String toString(Object obj) {
		final StringBuilder buf = new StringBuilder();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers())) {
				buf.append(f.getName() + ":\t[");
				Object value = null;
				try {
					value = findGetter(obj.getClass(), f.getName()).invoke(obj, null);
				} catch (Exception e) {
					value = e.toString();
				}
				if (value != null) {
					buf.append(value);
					buf.append("]\n");
				} else {
					buf.append("null");
					buf.append("]\n");
				}
			}
		}
		return buf.toString();
	}

	public static String toString(Object obj, String[] excludeFields) {
		final StringBuilder buf = new StringBuilder();
		Field[] fields = obj.getClass().getDeclaredFields();
		Map<String, String> excludeMap = new HashMap<>();
		if (excludeFields != null && excludeFields.length > 0) {
			for (String excludeField : excludeFields) {
				excludeMap.put(excludeField, excludeField);
			}
		}
		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers())) {
				if (excludeMap.containsKey(f.getName())) {
					continue;
				}
				buf.append(f.getName() + ":\t[");
				Object value = null;
				try {
					value = findGetter(obj.getClass(), f.getName()).invoke(obj, null);
				} catch (Exception e) {
					value = e.toString();
				}
				if (value != null) {
					buf.append(value);
					buf.append("]\n");
				} else {
					buf.append("null");
					buf.append("]\n");
				}
			}
		}
		return buf.toString();
	}

	public static Method findGetter(Class clazz, String name) throws Exception {
		BeanInfo info = Introspector.getBeanInfo(clazz);
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			if (name.equals(pd.getName())) {
				return pd.getReadMethod();
			}
		}
		throw new NoSuchFieldException(clazz + " has no field " + name);
	}

	public static <T> Field[] getFields(T instance) {
		Field[] f1 = null, f2 = null;
		Class<?> s_cls = instance.getClass().getSuperclass();
		Class<? extends Object> cls = instance.getClass();
		if (s_cls != null) {
			f1 = s_cls.getDeclaredFields();
		}
		if (cls != null) {
			f2 = cls.getDeclaredFields();
		}
		return ArrayUtils.addAll(f1, f2);
	}

	public static <T> void clonePropertiesWithNull(T target, T source) {
		if (target == null || source == null) {
			throw new CustomException("clone object cannot be null");
		}
		List<Field> targetFields = FieldUtils.getAllFieldsList(target.getClass());

		try {
			List<Field> targetNullFields = Lists.newArrayList();
			for (Field targetField : targetFields) {
				Object val = FieldUtils.readField(targetField, target, true);
				if (val == null) {
					targetNullFields.add(targetField);
				}
			}

			for (Field targetNullField : targetNullFields) {
				Object sourceVal = FieldUtils.readField(targetNullField, source, true);
				FieldUtils.writeField(targetNullField, target, sourceVal, true);
			}
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

}
