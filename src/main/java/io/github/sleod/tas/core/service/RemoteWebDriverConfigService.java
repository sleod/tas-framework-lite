package io.github.sleod.tas.core.service;

import io.github.sleod.tas.core.json.container.JSONDriverConfig;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sleod.tas.common.logging.SystemLogger.info;


/**
 * singleton pattern used
 * in runtime only one instance can be generated
 */
public final class RemoteWebDriverConfigService extends ConfigService{
    private static final Map<String, JSONDriverConfig> queue = new ConcurrentHashMap<>();
    private static final Object CONSUMABLE = new Object();

    public static void loadConfigs() {
        synchronized (queue) {
            getValidDriverConfigs(false).forEach(queue::putIfAbsent);
            info("Driver Config Service load with configs: " + countConfigs());
        }
    }

    public static void reload() {
        cleanUp();
        info("Driver Config Service reloading...");
        loadConfigs();
    }

    public static void cleanUp() {
        queue.clear();
    }



    public static int countConfigs() {
        return queue.size();
    }

    public static void unlockConfig(JSONDriverConfig config) {
        synchronized (queue) {
            config.setIdle(true);
        }
        notifyAllForEmpty();
    }

    public static Optional<JSONDriverConfig> getConsumableConfig() {
        synchronized (queue) {
            return queue.values().stream().filter(JSONDriverConfig::isIdle).findFirst();
        }
    }

    public static boolean isConsumable() {
        synchronized (queue) {
            return queue.values().stream().anyMatch(JSONDriverConfig::isIdle);
        }
    }

    public static JSONDriverConfig lockConfig() {
        if (!isConsumable()) {
            try {
                waitOnEmpty();
            } catch (InterruptedException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Exception while waiting for driver config!");
            }
        }
        return busy();
    }

    private static JSONDriverConfig busy() {
        synchronized (queue) {
            JSONDriverConfig config = getConsumableConfig().get();
            config.setIdle(false);
            return config;
        }
    }

    private static void notifyAllForEmpty() {
        synchronized (CONSUMABLE) {
            CONSUMABLE.notify();
        }
    }

    private static void waitOnEmpty() throws InterruptedException {
        synchronized (CONSUMABLE) {
            info("No Config is idle, please wait!");
            CONSUMABLE.wait();
        }
    }
}
