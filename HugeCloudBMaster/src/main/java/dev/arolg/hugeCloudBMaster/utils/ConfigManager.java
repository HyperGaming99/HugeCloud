package dev.arolg.hugeCloudBMaster.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private static File configFile;
    private ConfigData config;
    private Gson gson;

    public ConfigManager(File dataDirectory) {
        this.configFile = new File(dataDirectory, "config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static void init(File file) {
        configFile = file;
    }

    public void loadConfig() throws IOException {
        if (!configFile.exists()) {
            writeDefaultConfig();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            config = gson.fromJson(reader, ConfigData.class);
        }
        if (config == null) {
            throw new IllegalStateException("Config konnte nicht geladen werden oder ist leer.");
        }
    }

    public void saveConfig() throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        }
    }

    public ConfigData getConfig() {
        return config;
    }

    public void writeDefaultConfig() throws IOException {
        if (configFile == null) {
            throw new IllegalStateException("Config file is not initialized. Call ConfigManager.init() first.");
        }
        if (configFile.exists()) {
            System.out.println("Config file already exists: " + configFile.getAbsolutePath());
            return;
        }
        if (!configFile.createNewFile()) {
            throw new IOException("Could not create the config file: " + configFile.getAbsolutePath());
        }
        config = new ConfigData();
        config.tablist = new Tablist();
        config.tablist.header =
                " \n &b&lHugeCloud &8&lÂ»&r &7&o%online_players%&8/&7&o%max_players% \n &8â–º &r&7Aktueller Server &8â—\u008F &b%server% &8â—„ \n";
        config.tablist.footer =
                " \n &7Version &8&lÂ»&r &b0.0.1 \n ";
        config.hub = new Hub();
        config.hub.hub = "test";
        saveConfig();
    }

    public static class ConfigData {
        public Tablist tablist;
        public Hub hub;
    }

    public static class Tablist {
        public String header;
        public String footer;
    }

    public static class Hub {
        public String hub;
    }
}
