package com.example.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new IOException("Unable to find config.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties", e);
        }
    }
    public static String getUsername() {
    return properties.getProperty("kanban.admin.username");
}

    public static String getPassword() {
        return properties.getProperty("kanban.admin.password");
    }

    public static String getLocalAppUrl() {
        return properties.getProperty("local.app.url");
    }

    public static String getRemoteAppUrl() {
        return properties.getProperty("remote.app.url");
    }
    public static String getApiUsername() { return properties.getProperty("api.username"); }
    public static String getApiToken() { return properties.getProperty("api.token"); }
}

