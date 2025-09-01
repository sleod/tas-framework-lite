package io.github.sleod.tas.core.json.container;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.sleod.tas.core.json.customDeserializer.CustomIntegerListDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class JSONTestCaseConditions extends JSONContainer {

    @Setter
    private int limit;
    private List<Integer> index;
    @Setter
    private boolean useRandomLine;

    @JsonDeserialize(using = CustomIntegerListDeserializer.class)
    public void setIndex(List<Integer> index) {
        this.index = index;
    }

}
