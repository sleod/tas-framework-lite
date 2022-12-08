package ch.qa.testautomation.framework.core.json.customDeserializer;

import ch.qa.testautomation.framework.core.json.container.JSONResultLink;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
        return new ObjectMapper().readValue(jsonParser, new TypeReference<>() {});
    }
}
