package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.core.json.customDeserializer.CustomResultSetListDesrializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class JSONRunRecords extends JSONContainer {
    private int keep;
    private int runs;
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
