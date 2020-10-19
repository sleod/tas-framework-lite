package ch.raiffeisen.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class JSONContainer {
    public Set<String> getJsonProperties() {
        Field[] fields = this.getClass().getDeclaredFields();
        HashSet<String> properties = new HashSet<>(fields.length);
        for (Field field : fields) {
            if (field.isAnnotationPresent(JsonProperty.class)) {
                properties.add(field.getName());
            }
        }
        return properties;
    }

    public boolean isProperty(String name) {
        try {
            return this.getClass().getDeclaredField(name).isAnnotationPresent(JsonProperty.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
