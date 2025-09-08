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
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden des Dienstes: " + configFile.getName(), MessageType.ERROR);
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
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden des Dienstes: " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public static void printServiceTable(List<BukkitTask> services, List<BungeeTask> bungeeServices) {
        String format = "| %-36s | %-5s | %-6s | %-15s | %-8s |%n";
        String separator = "+--------------------------------------+-------+--------+-----------------+----------+----------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Port", "RAM", "Name", "Group", "Dynamic");
        System.out.println(separator);

        if (services.isEmpty() && bungeeServices.isEmpty()) {
            System.out.println("| Keine Dienste gefunden.              |       |        |                 |          |          |");
            System.out.println(separator);
            return;
        }

        for (BukkitTask service : services) {
            System.out.printf(
                    format,
                    service.getName(),
                    service.getPort(),
                    service.getRam() + "MB",
                    service.getName(),
                    service.getGroup(),
                    service.isDynamic() ? "Ja" : "Nein"
            );
        }
        for (BungeeTask service : bungeeServices) {
            System.out.printf(
                    format,
                    service.getName(),
                    service.getPort(),
                    service.getRam() + "MB",
                    service.getName(),
                    service.getGroup(),
                    service.isDynamic() ? "Ja" : "Nein"
            );
        }
        System.out.println(separator);
    }
}
