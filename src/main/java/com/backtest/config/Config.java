package com.backtest.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class Config {
    private static final Configuration CONFIG;

    static {
        CONFIG = loadConfiguration();
    }

    private static Configuration loadConfiguration() {
        try {
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(new Parameters().properties()
                                    .setFile(new File("src/main/resources/application.properties")));

            return builder.getConfiguration();

        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static String getApiKey() {
        return CONFIG.getString("polygon_api_key");
    }

    public static String getDatabasePassword() {
        return CONFIG.getString("database_password");
    }
}