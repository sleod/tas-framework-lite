package ch.qa.testautomation.tas.common.enumerations;

import lombok.Getter;

@Getter
public enum ScreenshotLevel {
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String level;

    ScreenshotLevel(String level) {
        this.level = level;
    }

}
