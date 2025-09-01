package io.github.sleod.tas.common.utils;

import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * Annotation Reflector for gathering all classes with the annotation
 */
public class AnnotationReflector {

    /**
     * reflect all methods with given annotation
     *
     * @param target     target class
     * @param annotation to method
     * @return list of methods
     */
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        Class<?> klass = target;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            List<Method> allMethods = new ArrayList<>(asList(klass.getDeclaredMethods()));
            for (Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    /**
     * get all annotated class in parent package
     *
     * @param inPackage  in special package. can be null or empty string.
     * @param annotation special annotation to class
     * @return annotated class in package.
     */
    public static Set<Class<?>> getAnnotatedClass(String inPackage, Class<? extends Annotation> annotation) {
        if (inPackage == null || inPackage.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "PackageName of Test Automation is empty! Method for scanning all local packages is not implemented yet!!");
        } else {
            return scanAnnotatedClass(Collections.singletonList(inPackage), annotation);
        }
    }

    /**
     * scan class with given annotation
     *
     * @param packages   packages which the class be found
     * @param annotation wanted annotation
     * @return set of classes
     */
    private static Set<Class<?>> scanAnnotatedClass(List<String> packages, Class<? extends Annotation> annotation) {
        Set<Class<?>> classes = new HashSet<>();
        for (String pack : packages) {
            classes.addAll(reflectResources(pack, annotation));
        }
        return classes;
    }

    /**
     * reflect class with given annotation
     * @param prefix     package of class
     * @param annotation wanted annotation
     * @return set of classes
     */
    private static Set<Class<?>> reflectResources(String prefix, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(prefix);
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
