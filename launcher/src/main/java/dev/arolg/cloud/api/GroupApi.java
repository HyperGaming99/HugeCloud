package dev.arolg.cloud.api;

import dev.arolg.cloud.managers.CloudManager;
import dev.arolg.cloud.tasks.TaskState;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.utils.LanguageManager;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class GroupApi {

    public static void start() {
        // Starte Server auf Port 8080
        ipAddress("0.0.0.0");
        port(1000);

        // Endpoint: GET /group/:name/status
        get("/group/:name/status", (req, res) -> {
            String name = req.params(":name");
            TaskState state = getGroupStatusAPI(name);

            if (state == null) {
                return LanguageManager.getMessage("api.group.status.unknown");
            }

            return switch (state) {
                case OFFLINE -> "offline";
                case ONLINE -> "online";
                case STARRING -> "starting";
                case STOPPING -> "stopping";
                default -> "unknown";
            };
        });

        // Endpoint: GET /groups
        get("/groups", (req, res) -> {
            List<BukkitTask> bukkitServices = CloudManager.getBukkitServices();
            List<String> groupInfo = new ArrayList<>();

            for (BukkitTask service : bukkitServices) {
                groupInfo.add(service.getName() + " - localhost:" + service.getPort());
            }

            // Gibt JSON zurück
            res.type("application/json");
            return groupInfo;
        });
    }

    public static TaskState getGroupStatusAPI(String name) {
        if (name == null || name.isEmpty()) {
            return TaskState.OFFLINE;
        }
        BukkitTask service = CloudManager.getServiceByName(name);
        if (service != null) {
            return service.getStatus();
        }
        return TaskState.OFFLINE;
    }
}
