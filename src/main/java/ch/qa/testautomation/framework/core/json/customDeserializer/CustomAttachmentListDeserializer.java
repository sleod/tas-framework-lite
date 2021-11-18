package ch.qa.testautomation.framework.core.json.customDeserializer;

import ch.qa.testautomation.framework.core.json.container.JSONAttachment;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
        List<JSONAttachment> attachments = new ObjectMapper().readValue(jsonParser, new TypeReference<List<JSONAttachment>>() {
        });
        return attachments;
    }
}