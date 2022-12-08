package ch.qa.testautomation.framework.core.json.container;

import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class JSONContainer {
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(this.getClass().getDeclaredMethods()).filter(method -> method.getName().startsWith("get")).forEach(method -> {
            try {
                stringBuilder.append(method.getName().substring(3)).append(": ").append(method.invoke(this)).append("\n");
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Assertions.fail("Print DTO Object failed!\n" + ex.getMessage());
            }
        });
        return stringBuilder.toString();
    }
}
