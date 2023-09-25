package ch.qa.testautomation.tas.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperSingleton {

    private static ObjectMapper mapper;

    private ObjectMapperSingleton(){}

    public static ObjectMapper getObjectMapper() {

        if (mapper == null) {

            mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        }

        return mapper;
    }
}