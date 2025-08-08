package eu.arolg.cloud.service.specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.utils.MessageType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

public class BukkitServiceSetup {

    public static void setup(String serviceName, int ram, int port,  String group, boolean dynamic) {
// Create service folder
        File serviceFolder = new File(System.getProperty("user.dir") + "/local/groups/" + serviceName);
        if (!serviceFolder.exists() && !serviceFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create service folder.", MessageType.ERROR);
            return;
        }

        // Create configs folder
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/configs");
        if (!configsFolder.exists() && !configsFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create configs folder.", MessageType.ERROR);
            return;
        }

        // Generate UUID and write to name.json
        UUID serviceId = UUID.randomUUID();
        JsonObject serviceData = new JsonObject();
        serviceData.addProperty("id", serviceId.toString());
        serviceData.addProperty("port", port);
        serviceData.addProperty("name", serviceName);
        serviceData.addProperty("ram", ram);
        serviceData.addProperty("group", group);
        serviceData.addProperty("dynamic", dynamic);
        serviceData.addProperty("maxPlayers", 100);
        File configFile = new File(configsFolder, serviceName + ".json");
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(configFile.toPath(), gson.toJson(serviceData));
        } catch (Exception e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to write config file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
            return;
        }

        // Create eula.txt
        File eulaFile = new File(serviceFolder, "eula.txt");
        try {
            Files.writeString(eulaFile.toPath(), "eula=true");
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create EULA file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
            return;
        }

        // Download JAR file
        String downloadUrl = "https://fill-data.papermc.io/v1/objects/5ee4f542f628a14c644410b08c94ea42e772ef4d29fe92973636b6813d4eaffc/paper-1.21.4-232.jar";
        File jarFile = new File(serviceFolder, "server.jar");

        try (var in = new BufferedInputStream(new URL(downloadUrl).openStream());
             var fileOutputStream = new FileOutputStream(jarFile)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage("Das Setup der Gruppe " + serviceName + " wurde erfolgreich abgeschlossen.", MessageType.INFO);
        } catch (Exception e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to download JAR file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }


    public static BukkitService getServiceByName(String name) {
        for (BukkitService service : HugeCloud.bukkitServices) {
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }
}
