package dev.arolg.cloud.api;

import dev.arolg.cloud.managers.CloudManager;
import dev.arolg.cloud.tasks.TaskState;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupController {

    @GetMapping("/group/{name}/status")
    public String getGroupStatus(@PathVariable("name") String name) {
        TaskState state = getGroupStatusAPI(name);
        if (state == null) {
            return "Service not found";
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