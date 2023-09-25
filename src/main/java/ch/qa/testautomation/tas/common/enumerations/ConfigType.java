package ch.qa.testautomation.tas.common.enumerations;

/**
 * enum of test types that can be used in test case content
 */
public enum ConfigType {

    REAL_DEVICE("real_device"),
    EMULATOR_DEVICE("emulator_device"),
    EMULATOR_DEVICE_WEB("emulator_device_web"),
    REAL_DEVICE_WEB("real_device_web"),
    GRID_SERVICE("grid_service");

    private final String type;

    ConfigType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

}
