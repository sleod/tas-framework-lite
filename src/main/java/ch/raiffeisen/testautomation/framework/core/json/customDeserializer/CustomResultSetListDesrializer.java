package ch.raiffeisen.testautomation.framework.core.json.customDeserializer;

import ch.raiffeisen.testautomation.framework.core.json.container.JSONResultSet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;

public class CustomResultSetListDesrializer extends StdDeserializer<List<JSONResultSet>> {
    public CustomResultSetListDesrializer() {
        this(null);
    }

    protected CustomResultSetListDesrializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<JSONResultSet> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        List<JSONResultSet> steps = new ObjectMapper().readValue(jsonParser, new TypeReference<List<JSONResultSet>>() {
        });
        return steps;
    }
}
