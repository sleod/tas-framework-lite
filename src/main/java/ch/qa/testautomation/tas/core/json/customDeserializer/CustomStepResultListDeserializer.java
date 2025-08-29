package ch.qa.testautomation.tas.core.json.customDeserializer;

import ch.qa.testautomation.tas.core.json.ObjectMapperSingleton;
import ch.qa.testautomation.tas.core.json.container.JSONStepResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;

public class CustomStepResultListDeserializer extends StdDeserializer<List<JSONStepResult>> {
    public CustomStepResultListDeserializer() {
        this(null);
    }

    protected CustomStepResultListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<JSONStepResult> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {
        });
    }
}
