package ch.qa.testautomation.tas.core.json.container;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JSONAttachment extends JSONContainer{
    private String name;
    private String type;
    private String source;

    public JSONAttachment(String name, String type, String source) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public JSONAttachment() {
    }

}
