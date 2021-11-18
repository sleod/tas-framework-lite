package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.core.json.customDeserializer.CustomResultSetListDesrializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class JSONRunRecords {
    @JsonProperty
    private int keep;
    @JsonProperty
    private int runs;
    @JsonProperty
    private List<JSONResultSet> resultSets;

    public JSONRunRecords(int keep, int runs, List<JSONResultSet> resultSets) {
        this.keep = keep;
        this.runs = runs;
        this.resultSets = resultSets;
    }

    public int getKeep() {
        return keep;
    }

    public void setKeep(int keep) {
        this.keep = keep;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public List<JSONResultSet> getResultSets() {
        return resultSets;
    }

    @JsonDeserialize(using = CustomResultSetListDesrializer.class)
    public void setResultSets(List<JSONResultSet> resultSets) {
        this.resultSets = resultSets;
    }
}
