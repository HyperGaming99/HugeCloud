package dev.arolg.cloud.tasks.specific;

import com.google.gson.*;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.utils.ConfigManager;
import dev.arolg.cloud.utils.MessageType;
import dev.arolg.cloud.tasks.Task;
import dev.arolg.cloud.tasks.TaskState;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeTask extends Task {

    private final String name;
    private String id;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private int ram;
    private TaskState status;

    private Process process;
    public BungeeTask(String id, int port, int ram, String name, String group, boolean dynamic) {
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
    public void create(String version) throws IOException, InterruptedException {
        if(dynamic) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();
            HttpClient client = HttpClient.newHttpClient();

            int nodeId = 1;

            String allocationUrl = config.getUrl() + "/api/application/nodes/" + nodeId + "/allocations";

            HttpRequest allocationRequest = HttpRequest.newBuilder()
                    .uri(URI.create(allocationUrl))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Accept", "Application/vnd.pelican.v1+json")
                    .build();

            HttpResponse<String> allocationResponse = client.send(allocationRequest, HttpResponse.BodyHandlers.ofString());

            if (allocationResponse.statusCode() != 200) {
                System.err.println("❌ Fehler beim Abrufen der Allocations: " + allocationResponse.body());
                return;
            }

            JsonObject allocationJson = gson.fromJson(allocationResponse.body(), JsonObject.class);
            JsonArray dataArray = allocationJson.getAsJsonArray("data");

            int freeAllocationId = -1;;
            int portNumber = -1;

            for (JsonElement element : dataArray) {
                JsonObject attributes = element.getAsJsonObject().getAsJsonObject("attributes");
                boolean assigned = attributes.get("assigned").getAsBoolean();
                if (!assigned) {
                    freeAllocationId = attributes.get("id").getAsInt();
                    portNumber = attributes.get("port").getAsInt();
                    break;
                }
            }

            if (freeAllocationId == -1) {
                System.err.println("❌ Keine freie Allocation gefunden.");
                return;
            }

            // === Schritt 2: JSON-Body erstellen ===
            String jsonBody = """
        {
          "name": "My New Server",
          "user": 1,
          "egg": 1,
          "environment": {
            "MINECRAFT_VERSION": "latest",
            "SERVER_JARFILE": "server.jar"
          },
          "limits": {
            "memory": 1024,
            "swap": 0,
            "disk": 20048,
            "io": 500,
            "cpu": 100
          },
          "feature_limits": {
            "databases": 0,
            "allocations": 1,
            "backups": 0
          },
          "allocation": {
            "default": 1
          }
        }
        """;

            // Ersetze dynamische Werte
            jsonBody = jsonBody.replace("\"memory\": 1024", "\"memory\": " + ram);
            jsonBody = jsonBody.replace("My New Server", name);
            jsonBody = jsonBody.replace("\"MINECRAFT_VERSION\": \"latest\"", "\"MINECRAFT_VERSION\": \"" + version + "\"");
            jsonBody = jsonBody.replace("\"default\": 1", "\"default\": " + freeAllocationId);

            // Optional: JSON validieren / formatieren
            JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);
            jsonBody = gson.toJson(jsonObject);

            // === Schritt 3: Server erstellen ===
            HttpRequest serverRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl() + "/api/application/servers"))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Accept", "Application/vnd.pelican.v1+json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> serverResponse = client.send(serverRequest, HttpResponse.BodyHandlers.ofString());

            if (!(serverResponse.statusCode() == 201)) {
                System.err.println("❌ Fehler beim Erstellen des Servers: " + serverResponse.body());
            }

            File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
            if (!configsFolder.exists() && !configsFolder.mkdirs()) {
                HugeCloud.getConsoleManager().sendMessage("Failed to create configs folder.", MessageType.ERROR);
                return;
            }

            JsonObject serverJson = gson.fromJson(serverResponse.body(), JsonObject.class);
            JsonObject attributes = serverJson.getAsJsonObject("attributes");
            String serverId = attributes.get("uuid").getAsString();
            JsonObject serviceData = new JsonObject();
            serviceData.addProperty("id", serverId);
            serviceData.addProperty("port", portNumber);
            serviceData.addProperty("name", name);
            serviceData.addProperty("ram", ram);
            serviceData.addProperty("group", group);
            serviceData.addProperty("dynamic", dynamic);
            File configFile = new File(configsFolder, name + ".json");
            try {
                Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
                Files.writeString(configFile.toPath(), gson2.toJson(serviceData));
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to write config file: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
                return;
            }

            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage("Das Setup der Gruppe " + name + " wurde erfolgreich abgeschlossen.", MessageType.INFO);
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
