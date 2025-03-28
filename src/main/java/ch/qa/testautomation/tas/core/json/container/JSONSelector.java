package ch.qa.testautomation.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JSONSelector extends JSONContainer {

    private String how;

    public String getHow() {
        return how;
    }

    @JsonProperty("How")
    public void setHow(String how) {
        this.how = how;
    }

    public String getUsing() {
        return using;
    }

    @JsonProperty("Using")
    public void setUsing(String using) {
        this.using = using;
    }

    private String using;
}
