package com.archermind.txtbl.taskfactory.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PrivateMethod {
	public static Object doTest(Class c, String methodName, Object instance,
			Class[] cs, Object[] objects) {
		Object result = null;
		try {
			Method m = c.getDeclaredMethod(methodName, cs);
			m.setAccessible(true);
			result = m.invoke(instance, objects);
			m.setAccessible(false);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
