package ch.qa.testautomation.tas.core.json.container;


import ch.qa.testautomation.tas.core.json.customDeserializer.CustomIntegerListDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class JSONTestCaseConditions extends JSONContainer {

    private int limit;
    private List<Integer> index;
    private boolean useRandomLine;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Integer> getIndex() {
        return index;
    }

    @JsonDeserialize(using = CustomIntegerListDeserializer.class)
    public void setIndex(List<Integer> index) {
        this.index = index;
    }

    public boolean isUseRandomLine() {
        return useRandomLine;
    }

    public void setUseRandomLine(boolean useRandomLine) {
        this.useRandomLine = useRandomLine;
    }
}
