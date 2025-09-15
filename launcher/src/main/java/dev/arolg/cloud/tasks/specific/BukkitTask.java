package dev.arolg.cloud.tasks.specific;

import com.google.gson.*;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.utils.ConfigManager;
import dev.arolg.cloud.utils.LanguageManager;
import dev.arolg.cloud.utils.MessageType;
import dev.arolg.cloud.tasks.Task;
import dev.arolg.cloud.tasks.TaskState;
import okhttp3.*;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BukkitTask extends Task {
    private final String name;
    private final String group;
    private final boolean dynamic;
    private int port;
    private final int ram;
    private String id;
    private  TaskState status;


    public BukkitTask(String id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        this.id = id;
        status = TaskState.OFFLINE;
    }
    @Override
    public void start() throws IOException, InterruptedException {
        if (status == TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_already_running", name), MessageType.WARN);
            return;
        }
        HttpClient client = HttpClient.newHttpClient();

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();

        String url = config.getUrl()+ "/api/client/servers/" + id + "/power";

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

        String url = config.getUrl()+ "/api/client/servers/" + id + "/power";

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
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("waiting_for_service", name), MessageType.INFO);
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        if (status != TaskState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_not_running", name), MessageType.WARN);
            return;
        }

        HttpClient client = HttpClient.newHttpClient();

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();

        String url = config.getUrl()+ "/api/client/servers/" + id + "/power";

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
    }

    @Override
    public void create(String version) throws IOException, InterruptedException {
        if(dynamic) {


            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();
            if(config.getSecrectKey() == "xxxx-xxxx-xxxx-xxxx") {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("" +
                        ""), MessageType.ERROR);
                return;
            }
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
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("no_free_allocation"), MessageType.ERROR);
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
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_create_server") + ": " + serverResponse.body(), MessageType.ERROR);
                return;
            }

            File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
            if (!configsFolder.exists() && !configsFolder.mkdirs()) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_create_configs_folder"), MessageType.ERROR);
                return;
            }

            try {
                String cid = getOrCreateSecret(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            JsonObject serverJson = gson.fromJson(serverResponse.body(), JsonObject.class);
            JsonObject attributes = serverJson.getAsJsonObject("attributes");
            String serverId = attributes.get("uuid").getAsString();
            JsonObject serviceData = new JsonObject();
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
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("failed_to_save_service_config"), MessageType.ERROR);
                e.printStackTrace();
                return;
            }

            setPort(portNumber);
            setID(serverId);

            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("service_created", name), MessageType.INFO);
        }else {

        }
    }

     public String getOrCreateSecret(String SERVER_ID) throws Exception {
         try {
             String filePath = "/config/paper-global.yml";
             OkHttpClient client = new OkHttpClient();
             ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();
             String BASE_URL = config.getUrl() + "/api/client/servers/";
             String API_KEY = config.getClientAPIKey();

             // Neuer Inhalt der Datei
             String content = ""
                     + "velocity-support:\n"
                     + "  enabled: true\n"
                     + "  online-mode: false\n"
                     + "  secret: test\n";

             // API-Request bauen
             Request request = new Request.Builder()
                     .url(BASE_URL + SERVER_ID + "/files/write?file=" + filePath)
                     .post(RequestBody.create(content, MediaType.parse("text/plain")))
                     .addHeader("Authorization", "Bearer " + API_KEY)
                     .addHeader("Accept", "Application/vnd.pterodactyl.v1+json")
                     .addHeader("Content-Type", "text/plain")
                     .build();

             try (Response response = client.newCall(request).execute()) {
                 if (response.isSuccessful()) {
                     System.out.println("Datei erfolgreich geschrieben!");
                 } else {
                     System.out.println("Fehler: " + response.code() + " - " + response.message());
                     System.out.println(response.body().string());
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "NOT_IMPLEMENTED";
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

    public String getId() {
        return id;
    }

    public void setPort(int newPort) {
        port = newPort;
    }

    public void setID(String newID) {
        this.id = id;
    }
    public boolean isDynamic() {
        return dynamic;
    }
}
