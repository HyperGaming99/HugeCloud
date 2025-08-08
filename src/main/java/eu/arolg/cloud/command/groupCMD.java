package eu.arolg.cloud.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.managers.CloudManager;
import eu.arolg.cloud.service.ServiceState;
import eu.arolg.cloud.service.ServiceType;
import eu.arolg.cloud.service.specific.BukkitService;
import eu.arolg.cloud.service.specific.BungeeService;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.MessageType;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.utils.PortFinder;
import org.jline.reader.LineReader;

import java.io.*;
import java.util.UUID;

public class groupCMD extends Command {

    public groupCMD() {
        super("group");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            HugeCloud.getConsoleManager().sendMessage(" - list", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - start <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - stop <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - status <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - create", MessageType.INFO);
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
            onCreate(args);
        } else if (args[0].equalsIgnoreCase("list")) {
            onList();
        } else if (args[0].equalsIgnoreCase("start")) {
            onStart(args);
        } else if (args[0].equalsIgnoreCase("stop")) {
            onStop(args);
        }else if (args[0].equalsIgnoreCase("status")) {
            BukkitService service = CloudManager.getServiceByName(args[1]);
            ServiceState state = service.getStatus();
            HugeCloud.getConsoleManager().sendMessage("Status von " + service.getName() + ": " + state, MessageType.INFO);
        }else {
            HugeCloud.getConsoleManager().sendMessage("Unknown subcommand: " + args[0], MessageType.ERROR);
        }
    }

    public void onCreate(String[] args) {
        final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
        LineReader reader = HugeCloud.getConsoleManager().createLineReader();

        String serviceName;
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


        String group;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage("Bitte geben Sie ein Typ an:", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Auswahlmöglichkeiten: 'proxy', 'server', 'lobby'", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();

            group = reader.readLine(prefix + "Group setup » ");
            if (group.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage("Setup abgebrochen.", MessageType.INFO);
                return;
            }
            if (!group.equalsIgnoreCase("proxy") || !group.equalsIgnoreCase("server") || !group.equalsIgnoreCase("lobby")) {
                break;
            }
            if (!group.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage("Die Group darf nicht leer sein.", MessageType.ERROR);
        }
        int port;
        if(!group.contains("proxy")) {
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
        }else  {
            port = PortFinder.getFreePort(ServiceType.PROXY);
            HugeCloud.getConsoleManager().sendMessage("Der Port wurde automatisch auf " + port + " gesetzt.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Sie können den Port später in der Konfigurationsdatei ändern.", MessageType.INFO);
        }


        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Bitte überprüfen Sie die folgenden Details:", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Name: " + serviceName, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("RAM: " + ram + " MB", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Group: " + group, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Port: " + port, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessageLeer();

        String confirmation = reader.readLine(prefix + "Group setup » " + "Möchten Sie diese Gruppe erstellen? (yes/no): ");
        if (!confirmation.equalsIgnoreCase("yes") && !confirmation.equalsIgnoreCase("y")) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage("Setup abgebrochen.", MessageType.INFO);
            return;
        }

        boolean dynamic = true;

        int ramValue = Integer.parseInt(ram);
        //is proxy
        if (group.contains("proxy")) {
            BungeeService bungeeService = new BungeeService(UUID.randomUUID(), port, ramValue, serviceName, group, dynamic);
            HugeCloud.bungeeServices.add(bungeeService);
            bungeeService.create();
        }else {
            BukkitService bukkitService = new BukkitService(UUID.randomUUID(), port, ramValue, serviceName, group, dynamic);
            HugeCloud.bukkitServices.add(bukkitService);
            bukkitService.create();
        }
    }

    public void onList() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
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

        String id = args[1];
        System.out.println(HugeCloud.bukkitServices);
        if(CloudManager.getServiceByName(id) == null) {
            if(CloudManager.getServiceBungeeByName(id) == null) {
                HugeCloud.getConsoleManager().sendMessage("Es wurde kein Dienst mit der ID '" + id + "' gefunden.", MessageType.ERROR);
                return;
            }else {
                BungeeService service = CloudManager.getServiceBungeeByName(id);
                service.start();
            }
        }else {
            BukkitService service = CloudManager.getServiceByName(id);
            service.start();
        }
    }

    public void onStop(String[] args) {

        String id = args[1];
        if(CloudManager.getServiceByName(id) == null) {
            if(CloudManager.getServiceBungeeByName(id) == null) {
                HugeCloud.getConsoleManager().sendMessage("Es wurde kein Dienst mit der ID '" + id + "' gefunden.", MessageType.ERROR);
                return;
            }else {
                BungeeService service = CloudManager.getServiceBungeeByName(id);
                service.stop();
            }
        }else {
            BukkitService service = CloudManager.getServiceByName(id);
            service.stop();
        }
    }
}