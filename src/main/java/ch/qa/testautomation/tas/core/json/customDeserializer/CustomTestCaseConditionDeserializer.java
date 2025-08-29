package ch.qa.testautomation.tas.core.json.customDeserializer;


import ch.qa.testautomation.tas.core.json.ObjectMapperSingleton;
import ch.qa.testautomation.tas.core.json.container.JSONTestCaseConditions;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CustomTestCaseConditionDeserializer extends StdDeserializer<JSONTestCaseConditions> {

    public CustomTestCaseConditionDeserializer() {
        this(null);
    }

    protected CustomTestCaseConditionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public JSONTestCaseConditions deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {
        });
    }

}
