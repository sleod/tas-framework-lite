//package testFramework.testAnnotation;
//
//import ch.raiffeisen.testautomation.framework.core.annotations.TestObject;
//import org.junit.Test;
//import org.reflections.Reflections;
//
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.net.URL;
//import java.util.Enumeration;
//import java.util.Set;
//
//public class TestReflection {
//
//    @Test
//    public void testtest(){
//        Set<Class<?>> classes = reflectResources("ch.raiffeisen.testautomation", TestObject.class);
//        System.out.println(classes.size());
//        try {
//            Enumeration<URL> resources = getClass().getClassLoader().getResources("ch.raiffeisen.testautomation");
//            while (resources.hasMoreElements()){
//                System.out.println(resources.nextElement().getPath());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private static Set<Class<?>> reflectResources(String prefix, Class<? extends Annotation> annotation) {
//        Reflections reflections = new Reflections(prefix);
//        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(annotation);
//        return typesAnnotatedWith;
//    }
//}
