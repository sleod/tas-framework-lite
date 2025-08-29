package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.core.json.customDeserializer.CustomStringListDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
