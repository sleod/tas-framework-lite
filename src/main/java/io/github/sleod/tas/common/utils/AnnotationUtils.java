package io.github.sleod.tas.common.utils;

import io.github.sleod.tas.common.enumerations.WebDriverName;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.annotations.SearchCriteria;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.openqa.selenium.support.FindBy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
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
     */
    @SuppressWarnings("unchecked")
    public static void changeAnnotationValue(Annotation fieldAnnotation, Map<String, String> config, Class<?> annotationClass) {
        try {
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
            if (annotationClass.equals(SearchCriteria.class)) {
                memberValues.put("locator", how + "=" + using);
            } else {
                memberValues.put(how, using);
            }
            info("modified field annotation to: " + how + " = " + using);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Exception by change annotation value in runtime: " + fieldAnnotation);
        }
    }

    /**
     * change FindBy annotation special in current thread
     *
     * @param field  field with annotation
     * @param config contains how and using
     */
    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public static void changeFindByValue(Field field, Map<String, String> config) {
        if (PropertyResolver.getWebDriverName().equals(WebDriverName.PLAYWRIGHT.getName())) {
            Arrays.stream(field.getAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(SearchCriteria.class)).findFirst()
                    .ifPresent(annotation -> {
                        changeAnnotationValue(field.getAnnotation(SearchCriteria.class), config, SearchCriteria.class);
                    });
        } else {
            Arrays.stream(field.getAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(FindBy.class)).findFirst()
                    .ifPresent(annotation -> changeAnnotationValue(field.getAnnotation(FindBy.class), config, FindBy.class));
        }

    }

}
