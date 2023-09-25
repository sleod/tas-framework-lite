package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.core.json.customDeserializer.CustomStringMapDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONPageConfig extends JSONContainer{

    private Map<String, Map<String, String>> configurations = new LinkedHashMap<>();

    public Map<String, String> getConfiguration(String key) {
        return configurations.get(key);
    }

    @JsonAnySetter
    @JsonDeserialize(using = CustomStringMapDeserializer.class)
    public void SetConfiguration(String fieldName, Map<String, String> config) {
        configurations.put(fieldName, config);
    }

    public Map<String, Map<String, String>> getConfigurations() {
        return configurations;
    }
}