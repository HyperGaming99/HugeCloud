package eu.arolg.cloud.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.service.specific.BukkitServiceSetup;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.MessageType;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.utils.PortFinder;
import org.jline.reader.LineReader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class groupCMD extends Command {

    public groupCMD() {
        super("group");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            HugeCloud.getConsoleManager().sendMessage(" - list", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - create", MessageType.INFO);
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
            onCreate(args);
        } else if (args[0].equalsIgnoreCase("list")) {
            onList();
        } else if (args[0].equalsIgnoreCase("start")) {
            onStart(args);
        } else {
            HugeCloud.getConsoleManager().sendMessage("Unknown subcommand: " + args[0], MessageType.ERROR);
        }
    }

    public void onCreate(String[] args) {
        final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
        LineReader reader = HugeCloud.getConsoleManager().createLineReader();

        String serviceName;
        // Prompt for service name
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage("Bitte geben Sie den Namen der Gruppe ein, die Sie erstellen möchten:", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();

            serviceName = reader.readLine(prefix + "Group setup » ");
            if (serviceName.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage("Setup abgebrochen.", MessageType.INFO);
                return;
            }
            if (!serviceName.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage("Der Gruppenname darf nicht leer sein.", MessageType.ERROR);
        }

        // Prompt for RAM
        String ram;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage("Bitte geben Sie die Arbeitsspeichermenge in MB für die Gruppe ein:", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            ram = reader.readLine(prefix + "Group setup » ");
            if (ram.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage("Setup abgebrochen.", MessageType.INFO);
                return;
            }
            try {
                if (Integer.parseInt(ram) > 0) {
                    break;
                }
            } catch (NumberFormatException e) {
                HugeCloud.getConsoleManager().sendMessage("Bitte geben Sie eine gültige Zahl ein.", MessageType.ERROR);
            }
        }


        // Prompt for and validate port
        int port;
        while (true) {
            try {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessageLeer();
                HugeCloud.getConsoleManager().sendMessage("Bitte geben Sie den Port an für die Gruppe.", MessageType.INFO);
                HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
                HugeCloud.getConsoleManager().sendMessageLeer();
                String portInput = reader.readLine(prefix + "Group setup » ");
                port = Integer.parseInt(portInput);
                if (PortFinder.available(port)) {
                    break;
                } else {
                    HugeCloud.getConsoleManager().sendMessage("Port is already in use. Please enter a different port.", MessageType.ERROR);
                }
            } catch (NumberFormatException e) {
                HugeCloud.getConsoleManager().sendMessage("Invalid port. Please enter a valid number.", MessageType.ERROR);
            }
        }

        // Confirm details
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Bitte überprüfen Sie die folgenden Details:", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Name: " + serviceName, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("RAM: " + ram + " MB", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Group: " + "ol", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Port: " + port, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessageLeer();

        String confirmation = reader.readLine(prefix + "Group setup » " + "Möchten Sie diese Gruppe erstellen? (yes/no): ");
        if (!confirmation.equalsIgnoreCase("yes") && !confirmation.equalsIgnoreCase("y")) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage("Setup abgebrochen.", MessageType.INFO);
            return;
        }

        BukkitServiceSetup.setup(serviceName, Integer.parseInt(ram), port, "Server", false);
    }

    public void onList() {
        File configsFolder = new File(System.getProperty("user.dir") + "/services/configs");
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
                JsonObject serviceData = JsonParser.parseReader(reader).getAsJsonObject();
                String serviceName = serviceData.get("name").getAsString();
                String serviceId = serviceData.get("id").getAsString();
                HugeCloud.getConsoleManager().sendMessage("- " + serviceName + " (ID: " + serviceId + ")", MessageType.INFO);
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage("Failed to read config file: " + configFile.getName(), MessageType.ERROR);
            }
        }
    }

    public void onStart(String[] args) {
        if (args.length < 2) {
            HugeCloud.getConsoleManager().sendMessage("Usage: service start <serviceName>", MessageType.INFO);
            return;
        }

        String serviceName = args[1];
        File serviceFolder = new File(System.getProperty("user.dir") + "/services/" + serviceName);
        File jarFile = new File(serviceFolder, "server.jar");

        if (!jarFile.exists()) {
            HugeCloud.getConsoleManager().sendMessage("Service JAR not found for: " + serviceName, MessageType.ERROR);
            return;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-Xmx1024M", "-Xms1024M", "-jar", jarFile.getAbsolutePath(), "nogui");
            processBuilder.directory(serviceFolder);
            processBuilder.inheritIO();
            processBuilder.start();
            HugeCloud.getConsoleManager().sendMessage("Service " + serviceName + " started successfully.", MessageType.INFO);
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to start service: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }


    @Override
    public List<String> getTabCompletion(String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list", "start");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return Arrays.asList("Service1", "Service2", "Service3");
        }
        return super.getTabCompletion(args);
    }
}