package eu.arolg.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.command.TestCMD;
import eu.arolg.cloud.managers.CloudManager;
import eu.arolg.cloud.managers.CommandManager;
import eu.arolg.cloud.managers.ConsoleManager;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.MessageType;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HugeCloud {

    @Getter
    private static ConsoleManager consoleManager;

    @Getter
    private static CommandManager commandManager;

    @Getter
    private static CloudManager cloudManager;

    private static List<String> loadedServices = new ArrayList<>();

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
        HugeCloud.getCommandManager().registerCommand(new TestCMD());
        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.ShutdownCMD());
        HugeCloud.getCommandManager().registerCommand(new eu.arolg.cloud.command.ServiceCMD());

        Thread commandSystem = new Thread(getCommandManager().reading(), "COMMAND");
        commandSystem.start();

        loadServices();



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

    private static void loadServices() {
        File configsFolder = new File(System.getProperty("user.dir") + "/services/configs");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            HugeCloud.getConsoleManager().sendMessage("No services found to load.", MessageType.INFO);
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            HugeCloud.getConsoleManager().sendMessage("No services found to load.", MessageType.INFO);
            return;
        }

        HugeCloud.getConsoleManager().sendMessage("Loading services from configs...", MessageType.INFO);
        for (File configFile : configFiles) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject serviceData = JsonParser.parseReader(reader).getAsJsonObject();
                String serviceName = serviceData.get("name").getAsString();
                loadedServices.add(serviceName); // Service zur Liste hinzufügen
                HugeCloud.getConsoleManager().sendMessage("Loaded service: " + serviceName, MessageType.INFO);
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to load service from file: " + configFile.getName(), MessageType.ERROR);
            }
        }
    }

    public static List<String> getLoadedServices() {
        return loadedServices;
    }

}
