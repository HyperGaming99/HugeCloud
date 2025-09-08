package dev.arolg.cloud.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "hugecloud-launcher.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;
    private Config config;

    public ConfigManager() {
        this.configFile = new File(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            config = new Config();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load configuration file: " + e.getMessage(), e);
            }
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration file: " + e.getMessage(), e);
        }
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {
        private String apiKey = "xxxx-xxxx-xxxx-xxxx";
        private String clientAPIKey = "xxxx-xxxx-xxxx-xxxx";
        private String url = "http://localhost:8080";
        public String getApiKey() {
            return apiKey;
        }

        public String getUrl() {
            return url;
        }

        public String getClientAPIKey() {
            return clientAPIKey;
        }

        public void setClientAPIKey(String clientAPIKey) {
            this.clientAPIKey = clientAPIKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}