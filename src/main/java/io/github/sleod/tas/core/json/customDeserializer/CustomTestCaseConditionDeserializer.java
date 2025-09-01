package io.github.sleod.tas.core.json.customDeserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONTestCaseConditions;

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
