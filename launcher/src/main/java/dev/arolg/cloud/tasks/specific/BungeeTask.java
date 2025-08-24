package dev.arolg.cloud.tasks.specific;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.utils.MessageType;
import dev.arolg.cloud.tasks.Task;
import dev.arolg.cloud.tasks.TaskState;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeTask extends Task {

    private final String name;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private final int ram;
    private TaskState status;

    private Process process;
    public BungeeTask(UUID id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        this.status = TaskState.OFFLINE;
    }

    @Override
    public void start() {
        if (status == TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist bereits online.", MessageType.WARN);
            return;
        }
        status = TaskState.ONLINE;
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

            File sourceJar = new File(System.getProperty("user.dir") + "/templates/HugeCloud-Master.jar");
            File pluginsFolder = new File(serviceFolder, "plugins");
            File targetJar = new File(pluginsFolder, "HugeCloud-Master.jar");

            if (!pluginsFolder.exists() && !pluginsFolder.mkdirs()) {
                HugeCloud.getConsoleManager().sendMessage("Failed to create plugins folder.", MessageType.ERROR);
                return;
            }

            try {
                Files.copy(sourceJar.toPath(), targetJar.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                HugeCloud.getConsoleManager().sendMessage("JAR erfolgreich kopiert.", MessageType.INFO);
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Kopieren der JAR: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }

            File velocityConfig = new File(serviceFolder, "velocity.toml");

// Inhalt deiner Config
            String configContent = """
# Config version. Do not change this
config-version = "2.7"

bind = "0.0.0.0:%port%"
motd = "<#09add3>A Velocity Server"
show-max-players = 500
online-mode = true
force-key-authentication = true
prevent-client-proxy-connections = false
player-info-forwarding-mode = "legacy"
forwarding-secret-file = "forwarding.secret"
announce-forge = false
kick-existing-players = false
ping-passthrough = "DISABLED"
sample-players-in-ping = false
enable-player-address-logging = true

[servers]
test = "127.0.0.1:30066"

try = [
    "test"
]

[forced-hosts]

[advanced]
compression-threshold = 256
compression-level = -1
login-ratelimit = 3000
connection-timeout = 5000
read-timeout = 30000
haproxy-protocol = false
tcp-fast-open = false
bungee-plugin-message-channel = true
show-ping-requests = false
failover-on-unexpected-server-disconnect = true
announce-proxy-commands = true
log-command-executions = false
log-player-connections = true
accepts-transfers = false
enable-reuse-port = false
command-rate-limit = 50
forward-commands-if-rate-limited = true
kick-after-rate-limited-commands = 0
tab-complete-rate-limit = 10
kick-after-rate-limited-tab-completes = 0

[query]
enabled = false
port = 25565
map = "Velocity"
show-plugins = false
""".replace("%port%", String.valueOf(port));

            try {
                java.nio.file.Files.writeString(
                        velocityConfig.toPath(),
                        configContent,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Schreiben der velocity.toml: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }

            File secretFile = new File(serviceFolder, "forwarding.secret");
            if (!secretFile.exists()) {
                try {
                    String secret = java.util.UUID.randomUUID().toString().replace("-", "");
                    java.nio.file.Files.writeString(secretFile.toPath(), secret);
                } catch (IOException e) {
                    HugeCloud.getConsoleManager().sendMessage("Fehler beim Erstellen von forwarding.secret: " + e.getMessage(), MessageType.ERROR);
                    e.printStackTrace();
                }
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
    public TaskState getStatus() {
        return status;
    }
}
