package dev.arolg.cloud.tasks.specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.utils.MessageType;
import dev.arolg.cloud.tasks.Task;
import dev.arolg.cloud.tasks.TaskState;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BukkitTask extends Task {
    private final String name;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private final int ram;
    private  TaskState status;

    private Process process;
    private BufferedWriter processWriter;


    public BukkitTask(UUID id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        status = TaskState.OFFLINE;
    }
    @Override
    public void start() {
        if (status == TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Service " + name + " is already running.", MessageType.WARN);
            return;
        }
        status = TaskState.ONLINE;
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
        if (status != TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist nicht online.", MessageType.WARN);
            return;
        }

        stop();
        start();
        HugeCloud.getConsoleManager().sendMessage("Group " + name + " wurde neu gestartet.", MessageType.INFO);
    }

    @Override
    public void stop() {
        if (status != TaskState.ONLINE) {
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
            status = TaskState.OFFLINE;
        }
    }

    @Override
    public void create(String version) {
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

            File serverPropertiesFile = new File(serviceFolder, "server.properties");
            if (!serverPropertiesFile.exists()) {
                try {
                    Files.writeString(serverPropertiesFile.toPath(), "server-port=" + port + "\n" +
                            "motd=HugeCloud Server\n" +
                            "online-mode=false\n");
                } catch (IOException e) {
                    HugeCloud.getConsoleManager().sendMessage("Failed to create server.properties file: " + e.getMessage(), MessageType.ERROR);
                    e.printStackTrace();
                    return;
                }
            }

            String downloadUrl = versionGetter(version);
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

    public TaskState getStatus() {
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

    public static String versionGetter(String version) {
        if (version.contains("1.21.1")) {
            return "https://fill-data.papermc.io/v1/objects/39bd8c00b9e18de91dcabd3cc3dcfa5328685a53b7187a2f63280c22e2d287b9/paper-1.21.1-133.jar";
        }else if (version.contains("1.21.3")) {
            return "https://fill-data.papermc.io/v1/objects/87e973e1d338e869e7fdbc4b8fadc1579d7bb0246a0e0cf6e5700ace6c8bc17e/paper-1.21.3-83.jar";
        }else if (version.contains("1.21.4")) {
            return "https://fill-data.papermc.io/v1/objects/5ee4f542f628a14c644410b08c94ea42e772ef4d29fe92973636b6813d4eaffc/paper-1.21.4-232.jar";
        }else if (version.contains("1.21.5")) {
            return "https://fill-data.papermc.io/v1/objects/2ae6ae22adf417699746e0f89fc2ef6cb6ee050a5f6608cee58f0535d60b509e/paper-1.21.5-114.jar";
        }else if (version.contains("1.21.6")) {
            return "https://fill-data.papermc.io/v1/objects/35e2dfa66b3491b9d2f0bb033679fa5aca1e1fdf097e7a06a80ce8afeda5c214/paper-1.21.6-48.jar";
        }else if (version.contains("1.21.7")) {
            return "https://fill-data.papermc.io/v1/objects/83838188699cb2837e55b890fb1a1d39ad0710285ed633fbf9fc14e9f47ce078/paper-1.21.7-32.jar";
        }else if (version.contains("1.21.8")) {
            return "https://fill-data.papermc.io/v1/objects/937eb53ec237c95f016241e806a80baf4b85cb90c86a86d98b93fd218fe2b02f/paper-1.21.8-29.jar";
        }


        return "https://fill-data.papermc.io/v1/objects/5ee4f542f628a14c644410b08c94ea42e772ef4d29fe92973636b6813d4eaffc/paper-1.21.4-232.jar";
    }
}
