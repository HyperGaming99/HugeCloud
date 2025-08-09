package eu.arolg.cloud.service.specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.service.Service;
import eu.arolg.cloud.service.ServiceState;
import eu.arolg.cloud.utils.MessageType;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BukkitService extends Service {
    private final String name;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private final int ram;
    private  ServiceState status;

    private Process process;
    private BufferedWriter processWriter;


    public BukkitService(UUID id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        status = ServiceState.OFFLINE;
    }
    @Override
    public void start() {
        if (status == ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Service " + name + " is already running.", MessageType.WARN);
            return;
        }
        status = ServiceState.ONLINE;
        File serviceFolder = new File(System.getProperty("user.dir") + "/static/" + name);
        File jarFile = new File(serviceFolder, "server.jar");

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
            processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist gestartet.", MessageType.INFO);
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to start service: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void restart() {
        if (status != ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist nicht online.", MessageType.WARN);
            return;
        }

        stop();
        start();
        HugeCloud.getConsoleManager().sendMessage("Group " + name + " wurde neu gestartet.", MessageType.INFO);
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

            File eulaFile = new File(serviceFolder, "eula.txt");
            try {
                Files.writeString(eulaFile.toPath(), "eula=true");
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to create EULA file: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
                return;
            }

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
                HugeCloud.getConsoleManager().sendMessage("Das Setup der Gruppe " + name + " wurde erfolgreich abgeschlossen.", MessageType.INFO);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to download JAR file: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }
        }else {

        }
    }

    public void sendCommand(String command) {
        if (processWriter != null) {
            try {
                processWriter.write(command);
                processWriter.newLine();
                processWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            HugeCloud.getConsoleManager().sendMessage("No running process to send command.", MessageType.ERROR);
        }
    }

    public String getName() {
        return name;
    }

    public ServiceState getStatus() {
        return status;
    }

    public int getRam() {
        return ram;
    }

    public int getPort() {
        return port;
    }

    public String getGroup() {
        return group;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
