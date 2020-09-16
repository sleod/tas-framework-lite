package ch.sleod.testautomation.framework.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;
/**
 * Deserializer for String array in Json
 */
public class CustomStringListDeserializer extends StdDeserializer<List<String>> {
    public CustomStringListDeserializer() {
        this(null);
    }

    protected CustomStringListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return new ObjectMapper().readValue(jsonParser, new TypeReference<List<String>>() {
        });
    }
}
