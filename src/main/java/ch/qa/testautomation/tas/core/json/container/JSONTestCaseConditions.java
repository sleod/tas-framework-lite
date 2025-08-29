package ch.qa.testautomation.tas.core.json.container;


import ch.qa.testautomation.tas.core.json.customDeserializer.CustomIntegerListDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
