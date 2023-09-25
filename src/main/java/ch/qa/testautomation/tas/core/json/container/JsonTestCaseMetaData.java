package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.common.enumerations.TestType;

/**
 * Haltet JSON Meta Daten aus dem JSONTestCase welche die *.tas File
 * repräsentiert
 * So können alle Werte des Testobjects hier eingetragen werden
 */
public class JsonTestCaseMetaData {
    private String setScreenshotLevel;
    private TestType testType;

    public void setScreenshotLevel(String screenshotLevel) {
        this.setScreenshotLevel = screenshotLevel;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public String getScreenshotLevel() {
        return setScreenshotLevel;
    }

    public TestType getTestType() {
        return testType;
    }
}