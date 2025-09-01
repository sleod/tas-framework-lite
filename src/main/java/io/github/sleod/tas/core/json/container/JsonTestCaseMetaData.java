package io.github.sleod.tas.core.json.container;

import io.github.sleod.tas.common.enumerations.ScreenshotLevel;
import io.github.sleod.tas.common.enumerations.TestType;
import lombok.Getter;
import lombok.Setter;

/**
 * Haltet JSON Meta Daten aus dem JSONTestCase welche die *.tas File
 * repräsentiert, so können alle Werte des TestObjects hier eingetragen werden
 */
@Getter
@Setter
public class JsonTestCaseMetaData {
    private ScreenshotLevel screenshotLevel;
    private TestType testType;
}