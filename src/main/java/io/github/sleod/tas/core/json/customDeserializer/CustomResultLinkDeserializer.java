package io.github.sleod.tas.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONResultLink;

import java.io.IOException;
import java.util.List;

public class CustomResultLinkDeserializer extends StdDeserializer<List<JSONResultLink>> {

    protected CustomResultLinkDeserializer(Class<?> vc) {
        super(vc);
    }

    protected CustomResultLinkDeserializer() {
        this(null);
    }

    @Override
    public List<JSONResultLink> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {});
    }
}
