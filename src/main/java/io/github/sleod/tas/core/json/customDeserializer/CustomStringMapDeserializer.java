package io.github.sleod.tas.core.json.customDeserializer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import lombok.Getter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializer for String array in Json
 */
public class CustomStringMapDeserializer extends StdDeserializer<Map<String, String>> {

    protected CustomStringMapDeserializer(Class<?> vc) {
        super(vc);
    }

    protected CustomStringMapDeserializer() {
        this(null);
    }

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        MapContent mapContent = ObjectMapperSingleton.mapper().readValue(jsonParser, MapContent.class);
        return mapContent.getContent();
    }

    @Getter
    private static class MapContent {

        @JsonAnySetter
        public void add(String key, String value) {
            content.put(key, value);
        }

        private Map<String, String> content = new LinkedHashMap<>();
    }
}
