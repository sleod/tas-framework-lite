package ch.qa.testautomation.framework.core.json.customDeserializer;

import ch.qa.testautomation.framework.core.json.container.JSONResultLabel;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
        return new ObjectMapper().readValue(jsonParser, new TypeReference<>() {});
    }
}
