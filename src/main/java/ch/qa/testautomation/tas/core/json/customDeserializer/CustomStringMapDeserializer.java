package ch.qa.testautomation.tas.core.json.customDeserializer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
        MapContent mapContent = new ObjectMapper().readValue(jsonParser, MapContent.class);
        return mapContent.getContent();
    }

    private static class MapContent {
        public Map<String, String> getContent() {
            return content;
        }

        @JsonAnySetter
        public void add(String key, String value) {
            content.put(key, value);
        }

        private Map<String, String> content = new LinkedHashMap<>();
    }
}
