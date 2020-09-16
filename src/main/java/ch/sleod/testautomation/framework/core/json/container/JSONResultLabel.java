package ch.sleod.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JSONResultLabel {
    @JsonProperty
    private String name;
    @JsonProperty
    private String value;

    public JSONResultLabel(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public JSONResultLabel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
