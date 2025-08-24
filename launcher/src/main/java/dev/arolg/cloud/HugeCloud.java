package dev.arolg.cloud;

import dev.arolg.cloud.api.CloudAPI;
import dev.arolg.cloud.managers.CloudManager;
import dev.arolg.cloud.managers.CommandManager;
import dev.arolg.cloud.managers.ConsoleManager;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.tasks.specific.BungeeTask;
import dev.arolg.cloud.utils.starter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class HugeCloud {
    @Getter
    private static ConsoleManager consoleManager;

    @Getter
    private static CommandManager commandManager;

    @Getter
    private static CloudManager cloudManager;


    public static final List<BukkitTask> bukkitServices = new ArrayList<>();
    public static final List<BungeeTask> bungeeTasks = new ArrayList<>();
    public static List<BukkitTask> loadedServices = new ArrayList<>();
    public static List<BungeeTask> BungeeTasks = new ArrayList<>();


    public static void main(String[] args) {
        consoleManager = new ConsoleManager();
        commandManager = new CommandManager();
        cloudManager = new CloudManager();


        starter.onstart();
        CloudAPI.main(args);
    }
}
