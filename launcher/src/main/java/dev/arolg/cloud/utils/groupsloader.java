package dev.arolg.cloud.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.tasks.specific.BungeeTask;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;

public class groupsloader {
    public static void loadBukkitServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            return;
        }
        for (File configFile : configFiles) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String id = json.get("id").getAsString();
                String name = json.get("name").getAsString();
                int port = json.get("port").getAsInt();
                int ram = json.get("ram").getAsInt();
                String group = json.get("group").getAsString();
                boolean dynamic = json.get("dynamic").getAsBoolean();

                if(!group.contains("bukkit")) {
                    continue;
                }

                BukkitTask bukkitService = new BukkitTask(id, port, ram,name, group, dynamic);
                HugeCloud.bukkitServices.add(bukkitService);
                HugeCloud.loadedServices.add(bukkitService);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("error_loading_service") + ": " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public static void loadBungeeServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            return;
        }
        for (File configFile : configFiles) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String id = json.get("id").getAsString();
                String name = json.get("name").getAsString();
                int port = json.get("port").getAsInt();
                int ram = json.get("ram").getAsInt();
                String group = json.get("group").getAsString();
                boolean dynamic = json.get("dynamic").getAsBoolean();
                if(!group.contains("proxy")) {
                    return;
                }

                BungeeTask bungeeService = new BungeeTask(id, port, ram,name, group, dynamic);
                HugeCloud.bungeeTasks.add(bungeeService);
                HugeCloud.BungeeTasks.add(bungeeService);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("error_loading_service") + ": " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }
}
