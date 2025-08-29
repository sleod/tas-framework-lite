package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.core.json.customDeserializer.CustomResultSetListDesrializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Setter;

import java.util.List;

public class JSONRunRecords extends JSONContainer {
    @Setter
    private int keep;
    @Setter
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

    public int getRuns() {
        return runs;
    }

    public List<JSONResultSet> getResultSets() {
        return resultSets;
    }

    @JsonDeserialize(using = CustomResultSetListDesrializer.class)
    public void setResultSets(List<JSONResultSet> resultSets) {
        this.resultSets = resultSets;
    }
}
