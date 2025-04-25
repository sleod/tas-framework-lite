package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.common.enumerations.ScreenshotLevel;
import ch.qa.testautomation.tas.common.enumerations.TestType;
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