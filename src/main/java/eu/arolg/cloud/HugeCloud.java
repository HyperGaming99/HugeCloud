package eu.arolg.cloud;

import eu.arolg.cloud.managers.CloudManager;
import eu.arolg.cloud.managers.CommandManager;
import eu.arolg.cloud.managers.ConsoleManager;
import eu.arolg.cloud.service.specific.BukkitService;
import eu.arolg.cloud.service.specific.BungeeService;
import eu.arolg.cloud.utils.starter;
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


    public static final List<BukkitService> bukkitServices = new ArrayList<>();
    public static final List<BungeeService> bungeeServices = new ArrayList<>();
    public static List<BukkitService> loadedServices = new ArrayList<>();
    public static List<BungeeService> loadedServicesBungee = new ArrayList<>();


    public static void main(String[] args) {
        consoleManager = new ConsoleManager();
        commandManager = new CommandManager();
        cloudManager = new CloudManager();

        starter.onstart();
    }
}
