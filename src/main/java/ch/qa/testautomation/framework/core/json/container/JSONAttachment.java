package ch.qa.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JSONAttachment {
    @JsonProperty
    private String name;
    @JsonProperty
    private String type;
    @JsonProperty
    private String source;

    public JSONAttachment(String name, String type, String source) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public JSONAttachment() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
