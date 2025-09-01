package io.github.sleod.tas.core.json.container;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JSONResultLabel extends JSONContainer {
    private String name;
    private String value;

    public JSONResultLabel(String name, String value) {
        this.name = name;
        this.value = value;
    }

}
