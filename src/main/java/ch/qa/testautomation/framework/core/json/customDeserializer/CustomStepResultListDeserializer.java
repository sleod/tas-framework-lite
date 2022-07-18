package ch.qa.testautomation.framework.core.json.customDeserializer;

import ch.qa.testautomation.framework.core.json.container.JSONStepResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        List<JSONStepResult> steps = new ObjectMapper().readValue(jsonParser, new TypeReference<List<JSONStepResult>>() {
        });
        return steps;
    }
}
