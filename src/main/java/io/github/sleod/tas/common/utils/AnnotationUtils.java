package io.github.sleod.tas.common.utils;

import org.openqa.selenium.support.FindBy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static io.github.sleod.tas.common.logging.SystemLogger.info;

/**
 * Changes the annotation value for the given key of the given annotation to newValue and returns
 * see original java implementation:
 * https://www.programcreek.com/java-api-examples/?code=XDean/Java-EX/Java-EX-master/src/main/java/xdean/jex/util/reflect/AnnotationUtil.java
 */

public class AnnotationUtils {

    /**
     * change annotation value in runtime
     *
     * @param fieldAnnotation field annotation object
     * @param config          configuration of test object with map in form {"how":"value", "using":"value"}
     * @param annotationClass annotation class static context
     * @throws NoSuchFieldException   no such field found
     * @throws IllegalAccessException access rights
     */
    @SuppressWarnings("unchecked")
    public static void changeAnnotationValue(Annotation fieldAnnotation, Map<String, String> config, Class<?> annotationClass) throws NoSuchFieldException, IllegalAccessException {
        Object handler = Proxy.getInvocationHandler(fieldAnnotation);
        Field valueField = handler.getClass().getDeclaredField("memberValues");
        valueField.setAccessible(true);
        Map<String, Object> memberValues = (Map<String, Object>) valueField.get(handler);
        //reset all value except how to default
        Method[] methods = annotationClass.getDeclaredMethods();
        for (Method method : methods) {
            memberValues.put(method.getName(), method.getDefaultValue());
        }
        String how = config.get("how");
        String using = config.get("using");
        memberValues.put(how, using);
        info("modified field annotation to: " + how + " = " + using);
    }

    /**
     * change FindBy annotation special in current thread
     *
     * @param field  field with annotation
     * @param config contains how and using
     * @throws NoSuchFieldException   for fetch private fields internal annotation handler
     * @throws IllegalAccessException access privilege violation
     */
    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public static void changeFindByValue(Field field, Map<String, String> config) throws NoSuchFieldException, IllegalAccessException {
        final FindBy fieldAnnotation = field.getAnnotation(FindBy.class);
        changeAnnotationValue(fieldAnnotation, config, FindBy.class);
    }

}
