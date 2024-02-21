package ch.qa.testautomation.tas.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;

public class CustomIntegerListDeserializer extends StdDeserializer<List<Integer>> {

    public CustomIntegerListDeserializer() {
        this(null);
    }

    protected CustomIntegerListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Integer> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return new ObjectMapper().readValue(jsonParser, new TypeReference<>() {
        });
    }

}
