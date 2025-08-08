package eu.arolg.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.command.groupCMD;
import eu.arolg.cloud.managers.CloudManager;
import eu.arolg.cloud.managers.CommandManager;
import eu.arolg.cloud.managers.ConsoleManager;
import eu.arolg.cloud.service.ServiceState;
import eu.arolg.cloud.service.specific.BukkitService;
import eu.arolg.cloud.service.specific.BungeeService;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.MessageType;
import lombok.Getter;
import org.jline.reader.LineReader;

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
    public static final List<BungeeService> bungeeServices = new ArrayList<>();
    static List<BukkitService> loadedServices = new ArrayList<>();
    static List<BungeeService> loadedServicesBungee = new ArrayList<>();


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
        if (!new File(System.getProperty("user.dir") + "/local/").exists()) {
            onInit();
            return;
        }

        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.ShutdownCMD());
        HugeCloud.getCommandManager().registerCommand(new groupCMD());
        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.clearCMD());

        Thread commandSystem = new Thread(getCommandManager().reading(), "COMMAND");

        loadBukkitServices();
        loadBungeeServices();

        HugeCloud.getConsoleManager().sendMessage("Starte HugeCloud...", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Starten der Cloud: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage("HugeCloud erfolgreich gestartet!", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Lade Dienste...", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden der Dienste: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        printServiceTable(bukkitServices, bungeeServices);
        HugeCloud.getConsoleManager().sendMessage("Dienste erfolgreich geladen!", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Starte Command-System...", MessageType.INFO);
        commandSystem.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Starten des Command-Systems: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage("Command-System erfolgreich gestartet!", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Warten: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HugeCloud.getConsoleManager().sendMessage("Die Cloud wird heruntergefahren...", MessageType.INFO);
            commandSystem.interrupt();
            for (BukkitService service : bukkitServices) {
                if (service.getStatus() == ServiceState.ONLINE) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Fehler beim Stoppen des Dienstes " + service.getName() + ": " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
            for (BungeeService service : bungeeServices) {
                if (service.getStatus() == ServiceState.ONLINE) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Fehler beim Stoppen des Dienstes " + service.getName() + ": " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }

            HugeCloud.getConsoleManager().sendMessage("Die Cloud wurde erfolgreich heruntergefahren!", MessageType.INFO);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Herunterfahren der Cloud: " + e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }
        }));
    }


    public static void onInit() {
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Herzlich Willkommen bei HugeCloud!", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Bitte warten Sie, während die Cloud initialisiert wird...", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler bei der Initialisierung: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage("Die Cloud wurde erfolgreich initialisiert!", MessageType.INFO);
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        String group;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage("AGBS & Minecraft Eula:", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Auswahlmöglichkeiten: 'yes' 'no'", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            LineReader reader = HugeCloud.getConsoleManager().createLineReader();
            final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
            group = reader.readLine(prefix + "HugeCloud Setup » ");
            if (group.contains("no")) {
                HugeCloud.getConsoleManager().sendMessage("Sie haben die AGBs und Eula abgelehnt. Das Setup wird abgebrochen.", MessageType.WARN);
                HugeCloud.getConsoleManager().sendMessage("Bitte akzeptieren Sie die AGBs und Eula, um fortzufahren.", MessageType.ERROR);
                System.exit(0);
            }
            if (!group.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage("Sie müssen yes oder no schreiben es darf nicht leer sein!", MessageType.ERROR);
        }

        HugeCloud.getConsoleManager().sendMessage("Sie haben die AGBs und Eula akzeptiert.", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Bitte warten Sie, während die Cloud gestartet wird...", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Starten der Cloud: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
        //create folder
        File localFolder = new File(System.getProperty("user.dir") + "/local");
        if (!localFolder.exists() && !localFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Erstellen des lokalen Ordners.", MessageType.ERROR);
            return;
        }
        File groupsFolder = new File(System.getProperty("user.dir") + "/local/groups");
        if (!groupsFolder.exists() && !groupsFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Erstellen des Gruppenordners.", MessageType.ERROR);
            return;
        }

        main(new String[]{});
    }


    public static void loadBukkitServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            return;
        }
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

                if(!group.contains("bukkit")) {
                    continue;
                }

                BukkitService bukkitService = new BukkitService(serviceId, port, ram,name, group, dynamic);
                bukkitServices.add(bukkitService);
                loadedServices.add(bukkitService);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden des Dienstes: " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public static void loadBungeeServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            return;
        }
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
                if(!group.contains("proxy")) {
                    return;
                }

                BungeeService bungeeService = new BungeeService(serviceId, port, ram,name, group, dynamic);
                bungeeServices.add(bungeeService);
                loadedServicesBungee.add(bungeeService);
            } catch (Exception e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Laden des Dienstes: " + configFile.getName(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public static void printServiceTable(List<BukkitService> services, List<BungeeService> bungeeServices) {
        String format = "| %-36s | %-5s | %-6s | %-15s | %-8s |%n";
        String separator = "+--------------------------------------+-------+--------+-----------------+----------+----------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Port", "RAM", "Name", "Group", "Dynamic");
        System.out.println(separator);

        if (services.isEmpty() && bungeeServices.isEmpty()) {
            System.out.println("| Keine Dienste gefunden.              |       |        |                 |          |          |");
            System.out.println(separator);
            return;
        }

        for (BukkitService service : services) {
            System.out.printf(
                    format,
                    service.getName(),
                    service.getPort(),
                    service.getRam() + "MB",
                    service.getName(),
                    service.getGroup(),
                    service.isDynamic() ? "Ja" : "Nein"
            );
        }
        for (BungeeService service : bungeeServices) {
            System.out.printf(
                    format,
                    service.getName(),
                    service.getPort(),
                    service.getRam() + "MB",
                    service.getName(),
                    service.getGroup(),
                    service.isDynamic() ? "Ja" : "Nein"
            );
        }
        System.out.println(separator);
    }

}
