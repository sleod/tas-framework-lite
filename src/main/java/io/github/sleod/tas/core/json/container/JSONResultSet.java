package io.github.sleod.tas.core.json.container;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.sleod.tas.core.json.customDeserializer.CustomStringListDeserializer;
import lombok.Getter;

import java.util.List;

@Getter
public class JSONResultSet extends JSONContainer {

    public JSONResultSet(List<String> results) {
        this.results = results;
    }

    private List<String> results;

    @JsonDeserialize(using = CustomStringListDeserializer.class)
    public void setResults(List<String> results) {
        this.results = results;
    }
}
