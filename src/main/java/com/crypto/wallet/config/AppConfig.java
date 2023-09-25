package com.crypto.wallet.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                log.error("Cant start application without config.properties file");
                System.exit(-1);
            }
            properties.load(is);
        } catch (IOException e) {
            log.error("Cant start application without config.properties file {}", e.getMessage());
        }
    }

    public static String getCoincapApiBaseUrl() {
        return properties.getProperty("coincap.api.base.url");
    }

    public static int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("thread.pool.size"));
    }

    public static int getThreadPoolTerminationTimeout() {
        return Integer.parseInt(properties.getProperty("thread.pool.termination.timeout"));
    }

    public static int getTestMultipleExecutionsNumber() {
        return Integer.parseInt(properties.getProperty("test.multiple.executions.number"));
    }

    public static String getCoinCapHistoryInterval() {
        return properties.getProperty("coincap.api.assets.history.interval");
    }

    public static String getCoinCapHistoryStart() {
        return properties.getProperty("coincap.api.assets.history.start");
    }

    public static String getCoinCapHistoryEnd() {
        return properties.getProperty("coincap.api.assets.history.end");
    }

}