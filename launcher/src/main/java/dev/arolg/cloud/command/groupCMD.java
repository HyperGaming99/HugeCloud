package dev.arolg.cloud.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.managers.CloudManager;
import dev.arolg.cloud.tasks.TaskState;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.tasks.specific.BungeeTask;
import dev.arolg.cloud.utils.ANSICodes;
import dev.arolg.cloud.utils.LanguageManager;
import dev.arolg.cloud.utils.MessageType;
import org.jline.reader.LineReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class groupCMD extends Command {

    public groupCMD() {
        super("group");
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length == 0) {
            HugeCloud.getConsoleManager().sendMessage(" - list", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - start <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - stop <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - status <groupName>", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(" - restart <groupName>", MessageType.INFO);
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
            if (args.length < 2) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.usage.status"), MessageType.ERROR);
                return;
            }
            String id = args[1];
            TaskState state = null;
            if(CloudManager.getServiceByName(id) != null) {
                state = CloudManager.getServiceByName(id).getStatus();
            } else if(CloudManager.getServiceBungeeByName(id) != null) {
                state = CloudManager.getServiceBungeeByName(id).getStatus();
            } else {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.notfound", id), MessageType.ERROR);
                return;
            }

            if(state == TaskState.OFFLINE) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.offline", id), MessageType.INFO);
            }else if(state == TaskState.ONLINE) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.online", id), MessageType.INFO);
            }else if(state == TaskState.STARRING) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.starting", id), MessageType.INFO);
            }else if(state == TaskState.STOPPING) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.stopping", id), MessageType.INFO);
            }else {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.unknown", id), MessageType.INFO);
            }
        }else  if(args[0].equalsIgnoreCase("restart")) {
            onRestart(args);
        }
        else {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.usage.general"), MessageType.ERROR);
        }
    }

    public void onCreate(String[] args) throws Exception {
        final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
        LineReader reader = HugeCloud.getConsoleManager().createLineReader();

        String serviceName;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.info"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.exit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();

            serviceName = reader.readLine(prefix + "Group setup » ");
            if (serviceName.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.aborted"), MessageType.INFO);
                return;
            }
            if (!serviceName.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.noname"), MessageType.ERROR);
        }

        String ram;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.ram"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.exit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            ram = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
            if (ram.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.aborted"), MessageType.INFO);
                return;
            }
            try {
                if (Integer.parseInt(ram) > 0) {
                    break;
                }
            } catch (NumberFormatException e) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.ramerror"), MessageType.ERROR);
            }
        }


        String group;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.group"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.groupexample"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.groupexit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();

            group = reader.readLine(prefix + "Group setup » ");
            if (group.equalsIgnoreCase("exit")) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.aborted"), MessageType.INFO);
                return;
            }
            if (!group.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.nogroup"), MessageType.ERROR);
        }

        String version = "";
        if(group.contains("bukkit")) {
            while (true) {
                HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                HugeCloud.getConsoleManager().sendMessageLeer();
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.version"), MessageType.INFO);
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.versionexample"), MessageType.INFO);
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.versionexit"), MessageType.INFO);
                HugeCloud.getConsoleManager().sendMessageLeer();

                version = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
                if (version.equalsIgnoreCase("exit")) {
                    HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
                    HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.aborted"), MessageType.INFO);
                    return;
                }
                if (!version.isBlank()) {
                    break;
                }
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.noversion"), MessageType.ERROR);
            }
        }


        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.summary"), MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage("Name: " + serviceName, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("RAM: " + ram + " MB", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Group: " + group, MessageType.INFO);
        if(group.contains("bukkit")) {
            HugeCloud.getConsoleManager().sendMessage("Version: " + version, MessageType.INFO);
        }
        HugeCloud.getConsoleManager().sendMessageLeer();

        String confirmation = reader.readLine(prefix + LanguageManager.getMessage("command.group.create.confirm") + " » ");
        if (!confirmation.equalsIgnoreCase("yes") && !confirmation.equalsIgnoreCase("y")) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.create.aborted"), MessageType.INFO);
            return;
        }

        boolean dynamic = true;

        int ramValue = Integer.parseInt(ram);
        if (group.contains("proxy")) {
            BungeeTask bungeeService = new BungeeTask("1", 0, ramValue, serviceName, group, dynamic);
            HugeCloud.bungeeTasks.add(bungeeService);
            bungeeService.create("1");
        }else {
            BukkitTask bukkitService = new BukkitTask("1", 0, ramValue, serviceName, group, dynamic);
            HugeCloud.bukkitServices.add(bukkitService);
            bukkitService.create(version);
        }
    }

    public void onList() {
        File configsFolder = new File(System.getProperty("user.dir") + "/local/groups/");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.list.nogroups"), MessageType.INFO);
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.list.nogroups"), MessageType.INFO);
            return;
        }

        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.list.header"), MessageType.INFO);
        for (File configFile : configFiles) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject serviceData = JsonParser.parseReader(reader).getAsJsonObject();
                String serviceName = serviceData.get("name").getAsString();
                String serviceId = serviceData.get("id").getAsString();
                HugeCloud.getConsoleManager().sendMessage("- " + serviceName + " (ID: " + serviceId + ")", MessageType.INFO);
            } catch (IOException e) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.list.error", configFile.getName()), MessageType.ERROR);
            }
        }
    }

    public void onStart(String[] args) throws IOException, InterruptedException {

        String id = args[1];
        if(CloudManager.getServiceByName(id) == null) {
            if(CloudManager.getServiceBungeeByName(id) == null) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.notfound", id), MessageType.ERROR);
                return;
            } else {
                BungeeTask service = CloudManager.getServiceBungeeByName(id);
                service.start();
            }
        }else {
            BukkitTask service = CloudManager.getServiceByName(id);
            service.start();
        }
    }

    public void onRestart(String[] args) throws IOException, InterruptedException {

        String id = args[1];
        if(CloudManager.getServiceByName(id) == null) {
            if(CloudManager.getServiceBungeeByName(id) == null) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.notfound", id), MessageType.ERROR);
                return;
            }else {
                BungeeTask service = CloudManager.getServiceBungeeByName(id);
                service.restart();
            }
        }else {
            BukkitTask service = CloudManager.getServiceByName(id);
            service.restart();
        }
    }

    public void onStop(String[] args) throws IOException, InterruptedException {

        String id = args[1];
        if(CloudManager.getServiceByName(id) == null) {
            if(CloudManager.getServiceBungeeByName(id) == null) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("command.group.status.notfound", id), MessageType.ERROR);
                return;
            }else {
                BungeeTask service = CloudManager.getServiceBungeeByName(id);
                service.stop();
            }
        }else {
            BukkitTask service = CloudManager.getServiceByName(id);
            service.stop();
        }
    }
}