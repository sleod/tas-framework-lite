package ch.qa.testautomation.tas.core.service;

import ch.qa.testautomation.tas.common.enumerations.ConfigType;
import ch.qa.testautomation.tas.core.json.container.JSONDriverConfig;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;

import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.*;

public class ConfigService {

    public static Map<String, JSONDriverConfig> getValidDriverConfigs(boolean isMobile) {
        String fileName = isMobile ? getMobileAppDriverConfig() : getRemoteWebDriverConfig();
        Map<String, JSONDriverConfig> configs = JSONContainerFactory.getDriverConfigs(getMobileDriverConfigLocation(), fileName).entrySet()
                .stream().filter(config -> isMobile ? isValidMobileConfig(config.getValue()) : isValidRemoteConfig(config.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (configs.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.OBJECT_NOT_FOUND, "Valid Mobile Driver Config files");
        }
        return configs;
    }

    private static boolean hasConfigType(JSONDriverConfig config) {
        if (!isValid(config.getConfigType())) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Mobile Driver Config has no ConfigType definition!");
        } else {
            return true;
        }
    }

    private static boolean isValidMobileConfig(JSONDriverConfig config) {
        return hasConfigType(config)
                && isValid(config.getPlatformName())
                && isValid(config.getHubURL())
                && ((ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.REAL_DEVICE)
                && (isValid(config.getUdid()) || isValid(config.getRealDeviceUuid())))
                || ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.EMULATOR_DEVICE));
    }

    private static boolean isValidRemoteConfig(JSONDriverConfig config) {
        return hasConfigType(config)
                && isValid(config.getPlatformName())
                && isValid(config.getPlatformVersion())
                && isValid(config.getHubURL())
                && isValid(config.getBrowserName())
                && (ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.REAL_DEVICE_WEB)
                || ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.EMULATOR_DEVICE_WEB)
                || ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.GRID_SERVICE));
    }
}
