package io.github.sleod.tas.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONTestCaseStep;

import java.io.IOException;
import java.util.List;

/**
 * Deserializer for special object array in Json
 */
public class CustomStepListDeserializer extends StdDeserializer<List<JSONTestCaseStep>> {

    public CustomStepListDeserializer() {
        this(null);
    }

    protected CustomStepListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<JSONTestCaseStep> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {
        });
    }
}
