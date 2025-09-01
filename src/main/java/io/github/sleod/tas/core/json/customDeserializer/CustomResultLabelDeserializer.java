package io.github.sleod.tas.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONResultLabel;

import java.io.IOException;
import java.util.List;

public class CustomResultLabelDeserializer extends StdDeserializer<List<JSONResultLabel>> {

    protected CustomResultLabelDeserializer(Class<?> vc) {
        super(vc);
    }

    protected CustomResultLabelDeserializer() {
        this(null);
    }

    @Override
    public List<JSONResultLabel> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {});
    }
}
