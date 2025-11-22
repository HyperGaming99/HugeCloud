package dev.arolg.cloud.tasks.specific;

import com.google.gson.*;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.utils.ConfigManager;
import dev.arolg.cloud.utils.LanguageManager;
import dev.arolg.cloud.utils.MessageType;
import dev.arolg.cloud.tasks.Task;
import dev.arolg.cloud.tasks.TaskState;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
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
        this.id = id;
        this.status = TaskState.OFFLINE;
    }

    @Override
    public void start() throws IOException, InterruptedException {
        if (status == TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_already_running", name), MessageType.WARN);
            return;
        }
        HttpClient client = HttpClient.newHttpClient();

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();

        String url = config.getUrl() + "/api/client/servers/" + id + "/power";

        String jsonBody = """
                {
                    "signal": "start"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getClientAPIKey())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_started", name), MessageType.INFO);
        } else {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_start_service", name) + ": " + response.body(), MessageType.ERROR);
        }
        status = TaskState.ONLINE;
    }

    @Override
    public void restart() throws IOException, InterruptedException {
        if (status != TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_not_running", name), MessageType.WARN);
            return;
        }

        HttpClient client = HttpClient.newHttpClient();

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();

        String url = config.getUrl() + "/api/client/servers/" + id + "/power";

        String jsonBody = """
                {
                    "signal": "reboot"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getClientAPIKey())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_restarted", name), MessageType.INFO);
        } else {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_restart_service", name) + ": " + response.body(), MessageType.ERROR);
        }
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_restarting", name), MessageType.INFO);
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        if (status != TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_not_running", name), MessageType.WARN);
            return;
        }

        HttpClient client = HttpClient.newHttpClient();

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();

        String url = config.getUrl() + "/api/client/servers/" + id + "/power";

        String jsonBody = """
                {
                    "signal": "stop"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getClientAPIKey())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_stopped", name), MessageType.INFO);
        } else {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_stop_service", name) + ": " + response.body(), MessageType.ERROR);
        }

        status = TaskState.OFFLINE;
    }

    @Override
    public void create(String version) throws Exception {
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
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_fetch_allocations") + ": " + allocationResponse.body(), MessageType.ERROR);
            return;
        }

        JsonObject allocationJson = gson.fromJson(allocationResponse.body(), JsonObject.class);
        JsonArray dataArray = allocationJson.getAsJsonArray("data");

        int freeAllocationId = -1;
        ;
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
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("no_free_allocation"), MessageType.ERROR);
            return;
        }

        String jsonBody = """
                {
                  "name": "My New Server",
                  "user": 1,
                  "egg": 23,
                  "environment": {
                    "VELOCITY_VERSION": "latest"
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
        jsonBody = jsonBody.replace("\"default\": 1", "\"default\": " + freeAllocationId);

        // Optional: JSON validieren / formatieren
        JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);
        jsonBody = gson.toJson(jsonObject);

        // === Schritt 3: Server erstellen ===
        HttpRequest serverRequest = HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl() + "/api/application/servers"))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> serverResponse = client.send(serverRequest, HttpResponse.BodyHandlers.ofString());

        if (!(serverResponse.statusCode() == 201)) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_create_server") + ": " + serverResponse.body(), MessageType.ERROR);
            return;
        }

        // === Schritt 4: Konfigurationsdatei speichern ===

        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() && !configsFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_create_configs_folder"), MessageType.ERROR);
            return;
        }

        JsonObject serverJson = gson.fromJson(serverResponse.body(), JsonObject.class);
        JsonObject attributes = serverJson.getAsJsonObject("attributes");
        String serverId = attributes.get("uuid").getAsString();
        JsonObject serviceData = new JsonObject();
        String key = getOrCreateSecret(serverId);
        ConfigManager.Config cfg = HugeCloud.getConfigManager().getConfig();
        cfg.setSecrectKey(key);
        HugeCloud.getConfigManager().saveConfig();
        serviceData.addProperty("secret", key);
        serviceData.addProperty("id", serverId);
        serviceData.addProperty("port", String.valueOf(portNumber));
        serviceData.addProperty("name", name);
        serviceData.addProperty("ram", String.valueOf(ram));
        serviceData.addProperty("group", group);
        serviceData.addProperty("dynamic", String.valueOf(dynamic));
        File configFile = new File(configsFolder, name + ".json");
        try {
            Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(configFile.toPath(), gson2.toJson(serviceData));
        } catch (Exception e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("error_saving_service_config") + ": " + configFile.getName(), MessageType.ERROR);
            e.printStackTrace();
            return;
        }

        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_created", name), MessageType.INFO);
    }


    public String getOrCreateSecret(String service) throws Exception {
        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();
        String serverId = service;
        String filePath = "/forwarding.secret"; // exakter Name der Datei!
        String apiKey = config.getClientAPIKey();

        HttpClient client = HttpClient.newHttpClient();
        String url = config.getUrl() + "/api/client/servers/" + serverId + "/files/contents?file=" +
                URLEncoder.encode(filePath, StandardCharsets.UTF_8);

        int maxRetries = 10;
        int retryDelay = 5000; // 5 Sekunden

        for (int i = 0; i < maxRetries; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/vnd.pterodactyl.v1+json")
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body(); // Dateiinhalt vorhanden
            } else if (response.statusCode() == 409) {
                Thread.sleep(retryDelay);
            } else if (response.statusCode() == 404) {
                return null;
            } else {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("error_fetching_file", filePath) + ": " + response.body(), MessageType.ERROR);
                return null;
            }
        }

        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_fetch_file_after_retries", filePath), MessageType.ERROR);
        return null;
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
