package com.obsglobal.util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class for utility unit tests.
 */
public class UtilTestHelper {
	public static void testPrivateConstructor(Class<?> type) throws Exception {
		final Constructor<?> c = type.getDeclaredConstructors()[0];
		c.setAccessible(true);

		InstantiationException targetException = null;
		try {
			c.newInstance((Object[]) null);
			fail("Instantiated "+ type.getName());
		}
		catch (InvocationTargetException ite) {
			assertEquals(ite.getTargetException().getClass(), InstantiationException.class);
			targetException = (InstantiationException) ite.getTargetException();
		}

		throw targetException; 
	}
}
