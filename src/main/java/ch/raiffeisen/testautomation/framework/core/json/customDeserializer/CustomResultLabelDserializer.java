package ch.raiffeisen.testautomation.framework.core.json.customDeserializer;

import ch.raiffeisen.testautomation.framework.core.json.container.JSONResultLabel;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;

public class CustomResultLabelDserializer extends StdDeserializer<List<JSONResultLabel>> {

    protected CustomResultLabelDserializer(Class<?> vc) {
        super(vc);
    }

    protected CustomResultLabelDserializer() {
        this(null);
    }

    @Override
    public List<JSONResultLabel> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        List<JSONResultLabel> labels = new ObjectMapper().readValue(jsonParser, new TypeReference<List<JSONResultLabel>>() {
        });
        return labels;
    }
}
