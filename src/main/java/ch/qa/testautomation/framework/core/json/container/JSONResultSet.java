package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.core.json.customDeserializer.CustomStringListDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class JSONResultSet extends JSONContainer {

    public JSONResultSet(List<String> results) {
        this.results = results;
    }

    private List<String> results;

    public List<String> getResults() {
        return results;
    }

    @JsonDeserialize(using = CustomStringListDeserializer.class)
    public void setResults(List<String> results) {
        this.results = results;
    }
}
