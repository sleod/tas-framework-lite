package io.github.sleod.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.sleod.tas.core.json.customDeserializer.CustomStringMapDeserializer;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class JSONPageConfig extends JSONContainer{

    private final Map<String, Map<String, String>> configurations = new LinkedHashMap<>();

    public Map<String, String> getConfiguration(String key) {
        return configurations.get(key);
    }

    @JsonAnySetter
    @JsonDeserialize(using = CustomStringMapDeserializer.class)
    public void SetConfiguration(String fieldName, Map<String, String> config) {
        configurations.put(fieldName, config);
    }

}