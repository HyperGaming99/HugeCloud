package eu.arolg.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.command.groupCMD;
import eu.arolg.cloud.managers.CloudManager;
import eu.arolg.cloud.managers.CommandManager;
import eu.arolg.cloud.managers.ConsoleManager;
import eu.arolg.cloud.service.specific.BukkitService;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.MessageType;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class HugeCloud {

    @Getter
    private static ConsoleManager consoleManager;

    @Getter
    private static CommandManager commandManager;

    @Getter
    private static CloudManager cloudManager;


    public static final List<BukkitService> bukkitServices = new ArrayList<>();

    public static void main(String[] args) {
        consoleManager = new ConsoleManager();
        commandManager = new CommandManager();
        cloudManager = new CloudManager();

        if (System.getProperty("os.name").contains("Windows")) {
            Field[] fields = ANSICodes.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(String.class)) {
                    try {
                        field.setAccessible(true);
                        field.set(ANSICodes.class, field.get(ANSICodes.class).toString().replace("\u001B", "\033"));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());

        String[] banner = {
                "",
                " _    _                      _____ _                 _ ",
                "| |  | |                    / ____| |               | |",
                "| |__| |_   _ _   _  ___   | |    | | ___  _   _  __| |",
                "|  __  | | | | | | |/ _ \\  | |    | |/ _ \\| | | |/ _` |",
                "| |  | | |_| | |_| |  __/  | |____| | (_) | |_| | (_| |",
                "|_|  |_|\\__,_|\\__, |\\___|   \\_____|_|\\___/ \\__,_|\\__,_|",
                "                __/ |                                  ",
                "               |___/                                   ",
                "",
                "  © " + Calendar.getInstance().get(Calendar.YEAR) + "  HugeCloud System",
                "  Made by Aro_LG",
                "  Website: https://arolg.dev/",
                ""
        };

        for (String line : banner) {
            HugeCloud.getConsoleManager().sendMessage(line, MessageType.INFO);
        }


        //Command Registration
        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.ShutdownCMD());
        HugeCloud.getCommandManager().registerCommand(new groupCMD());
        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.clearCMD());

        Thread commandSystem = new Thread(getCommandManager().reading(), "COMMAND");
        commandSystem.start();

        loadBukkitServices();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HugeCloud.getConsoleManager().sendMessage("Die Cloud wird heruntergefahren...", MessageType.INFO);
            commandSystem.interrupt();
            HugeCloud.getConsoleManager().sendMessage("Die Cloud wurde erfolgreich heruntergefahren!", MessageType.INFO);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Herunterfahren der Cloud: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }
        }));
    }



    public static void loadBukkitServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/configs");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            HugeCloud.getConsoleManager().sendMessage("Keine Gruppen gefunden.", MessageType.INFO);
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            HugeCloud.getConsoleManager().sendMessage("Keine Gruppen gefunden.", MessageType.INFO);
            return;
        }

        HugeCloud.getConsoleManager().sendMessage("Dienste gefunden:", MessageType.INFO);
        for (File configFile : configFiles) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String id = json.get("id").getAsString();
                UUID serviceId = UUID.fromString(id);
                String name = json.get("name").getAsString();
                int port = json.get("port").getAsInt();
                int ram = json.get("ram").getAsInt();
                String group = json.get("group").getAsString();
                boolean dynamic = json.get("dynamic").getAsBoolean();
                int maxPlayers = json.get("maxPlayers").getAsInt();

                // → Optional: Speichere alles in ein Objekt, falls nötig
                BukkitService bukkitService = new BukkitService(serviceId, port, ram,name, group, dynamic, maxPlayers);
                bukkitServices.add(bukkitService);
                System.out.println("BukkitService geladen: " + bukkitService);

                HugeCloud.getConsoleManager().sendMessage(
                        "- Dienst: " + id + " | Port: " + port + " | RAM: " + ram + "MB | Group: " + group +
                                " | Dynamic: " + dynamic + " | MaxPlayers: " + maxPlayers,
                        MessageType.INFO
                );
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden des Dienstes: " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }
}
