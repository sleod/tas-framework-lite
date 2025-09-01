package io.github.sleod.tas.core.json.customDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONAttachment;

import java.io.IOException;
import java.util.List;

public class CustomAttachmentListDeserializer extends StdDeserializer<List<JSONAttachment>> {
    public CustomAttachmentListDeserializer() {
        this(null);
    }

    protected CustomAttachmentListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<JSONAttachment> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ObjectMapperSingleton.mapper().readValue(jsonParser, new TypeReference<>() {});
    }
}
