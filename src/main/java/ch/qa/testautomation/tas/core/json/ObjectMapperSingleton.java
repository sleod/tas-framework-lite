package ch.qa.testautomation.tas.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperSingleton {

    private static ObjectMapper MAPPER;

    private ObjectMapperSingleton() {
    }

    public static ObjectMapper mapper() {
        if (MAPPER == null) {
            MAPPER = new ObjectMapper();
            MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        }
        return MAPPER;
    }

    public static String prettyJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return "{}";
        }
        try {
            return mapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper().readValue(rawJson, Object.class));
        } catch (Exception e) {
            // If it's not valid JSON, return raw text
            return rawJson;
        }
    }

    /**
     * Converts a TestPlan object to JSON string
     */
    public static String toJson(Object obj) {
        try {
            return mapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

}