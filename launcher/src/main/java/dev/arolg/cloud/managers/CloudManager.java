package dev.arolg.cloud.managers;

import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.tasks.specific.BungeeTask;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CloudManager {

    public Properties getproperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static BukkitTask getServiceByName(String name) {
        for (BukkitTask service : HugeCloud.bukkitServices) {
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }


    public static BungeeTask getServiceBungeeByName(String name) {
        for (BungeeTask task : HugeCloud.bungeeTasks) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    public static List<BukkitTask> getBukkitServices() {
        return HugeCloud.bukkitServices;
    }
}
