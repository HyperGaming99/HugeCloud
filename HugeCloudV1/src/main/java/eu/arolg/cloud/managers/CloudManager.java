package eu.arolg.cloud.managers;

import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.service.specific.BukkitService;
import eu.arolg.cloud.service.specific.BungeeService;

import java.io.IOException;
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

    public static BukkitService getServiceByName(String name) {
        for (BukkitService service : HugeCloud.bukkitServices) {
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }


    public static BungeeService getServiceBungeeByName(String name) {
        for (BungeeService service : HugeCloud.bungeeServices) {
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }

}
