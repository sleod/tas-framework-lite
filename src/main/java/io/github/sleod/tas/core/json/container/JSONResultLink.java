package io.github.sleod.tas.core.json.container;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JSONResultLink extends JSONContainer {
    private String name;
    private String url;
    private String type;

    public JSONResultLink(String name, String type, String url) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

}
