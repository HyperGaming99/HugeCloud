package dev.arolg.cloud.api;

import dev.arolg.cloud.managers.CloudManager;
import dev.arolg.cloud.tasks.TaskState;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.utils.LanguageManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GroupController {

    @GetMapping("/group/{name}/status")
    public String getGroupStatus(@PathVariable("name") String name) {
        TaskState state = getGroupStatusAPI(name);
        if (state == null) {
            return LanguageManager.getMessage("api.group.status.unknown");
        }
        if(state == TaskState.OFFLINE) {
            return "offline";
        }else if(state == TaskState.ONLINE) {
            return "online";
        }else if(state == TaskState.STARRING) {
            return "starting";
        }else if(state == TaskState.STOPPING) {
            return "stopping";
        }
        return "unknown";
    }

    @GetMapping("/groups")
    public List<String> getAllGroups() {
        List<BukkitTask> bukkitServices = CloudManager.getBukkitServices();
        List<String> groupInfo = new ArrayList<>();

        for (BukkitTask service : bukkitServices) {
            groupInfo.add(service.getName() + " - localhost:" + service.getPort());
        }

        return groupInfo;
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