package ch.raiffeisen.testautomation.framework.common.utils;

import org.reflections.Reflections;

import java.util.Set;

public class ClassExtensionFinder {

	/**
	 * get all extention of class xxx, ch.raiffeisen.testautomation.example String with package name like "framework.core"
	 * @param packageName where should be scanned
	 * @return set of classes
	 */
	public static Set<Class<? extends String>> getSubTypeOf(String packageName) {
		Reflections ref = new Reflections(packageName);
		Set<Class<? extends String>> subTypes = ref.getSubTypesOf(String.class);
		return subTypes;
	}
}
