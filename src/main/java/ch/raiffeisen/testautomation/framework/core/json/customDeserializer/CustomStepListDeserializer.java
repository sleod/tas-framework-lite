package ch.raiffeisen.testautomation.framework.core.json.customDeserializer;

import ch.raiffeisen.testautomation.framework.core.json.container.JSONTestCaseStep;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
        List<JSONTestCaseStep> steps = new ObjectMapper().readValue(jsonParser, new TypeReference<List<JSONTestCaseStep>>() {
        });
        return steps;
    }
}
