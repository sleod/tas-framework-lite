package io.github.sleod.tas.core.service;

import io.github.sleod.tas.common.enumerations.ConfigType;
import io.github.sleod.tas.core.json.container.JSONDriverConfig;
import io.github.sleod.tas.core.json.deserialization.JSONContainerFactory;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;
import static io.github.sleod.tas.configuration.PropertyResolver.*;

/**
 * Service class for retrieving and validating driver configurations.
 */
public class ConfigService {

    public static Map<String, JSONDriverConfig> getValidDriverConfigs(boolean isMobile) {
        String fileName = isMobile ? getMobileAppDriverConfig() : getRemoteWebDriverConfig();
        Map<String, JSONDriverConfig> configs = JSONContainerFactory.getDriverConfigs(getMobileDriverConfigLocation(), fileName).entrySet()
                .stream().filter(config -> isMobile ? isValidMobileConfig(config.getValue()) : isValidRemoteConfig(config.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (configs.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.OBJECT_NOT_FOUND, "Valid Mobile Driver Config files");
        }
        return configs;
    }

    /**
     * Check if the configuration type is valid.
     *
     * @param config The JSONDriverConfig object to validate.
     * @return True if the configuration type is valid, otherwise throws an exception.
     */
    private static boolean hasConfigType(JSONDriverConfig config) {
        if (!isValid(config.getConfigType())) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Mobile Driver Config has no ConfigType definition!");
        } else {
            return true;
        }
    }

    /**
     * Validate the mobile driver configuration.
     *
     * @param config The JSONDriverConfig object to validate.
     * @return True if the configuration is valid, otherwise false.
     */
    private static boolean isValidMobileConfig(JSONDriverConfig config) {
        return hasConfigType(config)
               && isValid(config.getPlatformName())
               && isValid(config.getHubURL())
               && ((ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.REAL_DEVICE)
                    && (isValid(config.getUdid()) || isValid(config.getRealDeviceUuid())))
                   || ConfigType.valueOf(config.getConfigType().toUpperCase()).equals(ConfigType.EMULATOR_DEVICE));
    }

    /**
     * Validate the remote web driver configuration.
     *
     * @param config The JSONDriverConfig object to validate.
     * @return True if the configuration is valid, otherwise false.
     */
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
