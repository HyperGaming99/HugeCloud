package eu.arolg.cloud.service.specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.service.Service;
import eu.arolg.cloud.service.ServiceState;
import eu.arolg.cloud.utils.MessageType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeService extends Service {

    private final String name;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private final int ram;
    private ServiceState status;

    private Process process;
    public BungeeService(UUID id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        this.status = ServiceState.OFFLINE;
    }

    @Override
    public void start() {
        if (status == ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist bereits online.", MessageType.WARN);
            return;
        }
        status = ServiceState.ONLINE;
        File serviceFolder = new File(System.getProperty("user.dir") + "/static/" + name);
        File jarFile = new File(serviceFolder, "bungee.jar");

        if (!jarFile.exists()) {
            HugeCloud.getConsoleManager().sendMessage("Service JAR not found for: " + name, MessageType.ERROR);
            return;
        }

        try {
            int ram = getRam();
            int ramh = ram / 2;

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-Xmx" + ram + "M",
                    "-Xms" + ramh + "M",
                    "-jar",
                    jarFile.getAbsolutePath(),
                    "nogui"
            );
            processBuilder.directory(serviceFolder);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist gestartet.", MessageType.INFO);
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to start service: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (status != ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist nicht online.", MessageType.WARN);
            return;
        }

        if (process != null && process.isAlive()) {
            process.destroy(); // sanfter Versuch
            try {
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Stoppen von " + name, MessageType.ERROR);
                e.printStackTrace();
            }

            HugeCloud.getConsoleManager().sendMessage("Group " + name + " wurde gestoppt.", MessageType.INFO);
            status = ServiceState.OFFLINE;
        }
    }

    @Override
    public void create() {
        if(dynamic) {
            File serviceFolder = new File(System.getProperty("user.dir") + "/static/" + name);
            if (!serviceFolder.exists() && !serviceFolder.mkdirs()) {
                HugeCloud.getConsoleManager().sendMessage("Failed to create service folder.", MessageType.ERROR);
                return;
            }

            File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
            if (!configsFolder.exists() && !configsFolder.mkdirs()) {
                HugeCloud.getConsoleManager().sendMessage("Failed to create configs folder.", MessageType.ERROR);
                return;
            }

            UUID serviceId = UUID.randomUUID();
            JsonObject serviceData = new JsonObject();
            serviceData.addProperty("id", serviceId.toString());
            serviceData.addProperty("port", port);
            serviceData.addProperty("name", name);
            serviceData.addProperty("ram", ram);
            serviceData.addProperty("group", group);
            serviceData.addProperty("dynamic", dynamic);
            File configFile = new File(configsFolder, name + ".json");
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.writeString(configFile.toPath(), gson.toJson(serviceData));
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to write config file: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
                return;
            }

            String downloadUrl = "https://fill-data.papermc.io/v1/objects/61249fa5b9b33bc7e3223581eab6aedad790a295caf0e39da2ff3c8ec9d9117f/velocity-3.4.0-SNAPSHOT-523.jar";
            File jarFile = new File(serviceFolder, "bungee.jar");

            try (var in = new BufferedInputStream(new URL(downloadUrl).openStream());
                 var fileOutputStream = new FileOutputStream(jarFile)) {

                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage("Das Setup der Gruppe " + name + " wurde erfolgreich abgeschlossen.", MessageType.INFO);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to download JAR file: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }
        }else {

        }
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public int getPort() {
        return port;
    }

    public int getRam() {
        return ram;
    }
    public ServiceState getStatus() {
        return status;
    }
}
