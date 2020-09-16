//package testFramework.testAnnotation;
//
//import AnnotationReflector;
//import FileOperation;
//import TestObject;
//import TestStep;
//import DriverManager;
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//import org.junit.Test;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Set;
//
//public class TestAnnotationReflection {
//
//    @Test
//    public void tests() {
//        DriverManager.setupWebDriver().initialize();
//        Set<Class<?>> annotated = AnnotationReflector.getAnnotatedClass("", TestObject.class);
//
//        String content = FileOperation.readFileToLinedString(getClass().getClassLoader().getResource("testData/google-testData.json").getPath());
//        JSONObject jsonObject = JSONObject.fromObject(content);
//
//        for (Class<?> clazz : annotated) {
//            String className = clazz.getName();
//            System.out.println(className);
//
//            AnnotationReflector.getAnnotatedMethods(clazz, TestStep.class).forEach(method -> {
//                try {
//                    TestStep testStep = method.getAnnotation(TestStep.class);
//                    if (testStep.name().equals("smoke")) {
//                        if (!testStep.using().isEmpty()) {
//                            Class<?>[] parameterTypes = method.getParameterTypes();
//                            if (parameterTypes.length == 1) {
//                                Object testData = jsonObject.get(testStep.using());
//                                method.invoke(Class.forName(className).newInstance(), parameterTypes[0].cast(testData));
//                            }
//                        } else {
//                            method.invoke(Class.forName(className).newInstance());
//                        }
//                    }
//
//                    if (testStep.name().equals("smoke array")) {
//                        if (!testStep.using().isEmpty()) {
//                            Class<?>[] parameterTypes = method.getParameterTypes();
//                            if (parameterTypes.length > 1) {
//                                JSONArray testData = jsonObject.getJSONArray(testStep.using());
//                                method.invoke(Class.forName(className).newInstance(), testData.toArray());
//                            }
//                        } else {
//                            method.invoke(Class.forName(className).newInstance());
//                        }
//                    }
//
//                    if (testStep.name().equals("smoke map")) {
//                        if (!testStep.using().isEmpty()) {
//                            Class<?>[] parameterTypes = method.getParameterTypes();
//                            if (parameterTypes.length == 1 && parameterTypes[0].equals(Map.class)) {
//                                JSONObject testData = jsonObject.getJSONObject(testStep.using());
//                                Map data = new LinkedHashMap();
//                                testData.forEach((key, name) -> data.put(key, name));
//                                method.invoke(Class.forName(className).newInstance(), data);
//                            }
//                        } else {
//                            method.invoke(Class.forName(className).newInstance());
//                        }
//                    }
//
//
//                } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }
//}