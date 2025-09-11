package dev.arolg.cloud.utils;

import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.command.ShutdownCMD;
import dev.arolg.cloud.command.clearCMD;
import dev.arolg.cloud.command.groupCMD;
import dev.arolg.cloud.tasks.TaskState;
import dev.arolg.cloud.tasks.specific.BukkitTask;
import dev.arolg.cloud.tasks.specific.BungeeTask;
import org.jline.reader.LineReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Calendar;

public class starter {
    public static void onstart() {
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
            try {
                onInit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        HugeCloud.getCommandManager().registerCommand(new ShutdownCMD());
        HugeCloud.getCommandManager().registerCommand(new groupCMD());
        HugeCloud.getCommandManager().registerCommand(new clearCMD());

        Thread commandSystem = new Thread(HugeCloud.getCommandManager().reading(), "COMMAND");

        groupsloader.loadBukkitServices();
        groupsloader.loadBungeeServices();

        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting"), MessageType.INFO);
        //HugeCloud.getConsoleManager().sendMessage("Starte HugeCloud...", MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting.success"), MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.loading.services"), MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.loading.services.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }
        groupsloader.printServiceTable(HugeCloud.bukkitServices, HugeCloud.bungeeTasks);
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.loading.services.success"), MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting.commandsystem"), MessageType.INFO);
        commandSystem.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting.commandsystem.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.starting.commandsystem.success"), MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.startup.error", e.getMessage()), MessageType.ERROR);
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
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.shutdown.start"), MessageType.INFO);
            commandSystem.interrupt();
            for (BukkitTask service : HugeCloud.bukkitServices) {
                if (service.getStatus() == TaskState.ONLINE) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.shutdown.error", service.getName(), e.getMessage()), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
            for (BungeeTask service : HugeCloud.bungeeTasks) {
                if (service.getStatus() == TaskState.ONLINE) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.shutdown.error", service.getName(), e.getMessage()), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }

            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.shutdown.success"), MessageType.INFO);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.shutdown.finalerror", e.getMessage()), MessageType.ERROR);
                e.printStackTrace();
            }

            for (BungeeTask service : HugeCloud.bungeeTasks) {
                if (service.getStatus() == TaskState.OFFLINE) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.startup.error", service.getName(), e.getMessage()), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
            for (BukkitTask service : HugeCloud.bukkitServices) {
                if (service.getStatus() == TaskState.OFFLINE) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.startup.error", service.getName(), e.getMessage()), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    public static void onInit() throws IOException {
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        HugeCloud.getConsoleManager().sendMessageLeer();
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.welcome"), MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.start"), MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree"), MessageType.INFO);
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
        String group;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.yesno"), MessageType.WARN);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.exit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            LineReader reader = HugeCloud.getConsoleManager().createLineReader();
            final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
            group = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
            if (group.contains("no")) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.decline"), MessageType.ERROR);
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.exit"), MessageType.INFO);
                System.exit(0);
            }
            if (!group.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.error"), MessageType.ERROR);
        }

        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.agree.thanks"), MessageType.INFO);

        //Pelican Api Key dieser muss volle Admin rechte haben
        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.api"), MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.api.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }

        String apiKey;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey.warn"), MessageType.WARN);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey.exit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            LineReader reader = HugeCloud.getConsoleManager().createLineReader();
            final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
            apiKey = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
            if (apiKey.contains("exit")) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey.exit"), MessageType.WARN);
                System.exit(0);
            }
            if (!apiKey.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey.error"), MessageType.ERROR);
        }

        String clientApiKey;
        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.clientapikey"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.clientapikey.warn"), MessageType.WARN);
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.clientapikey.exit"), MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            LineReader reader = HugeCloud.getConsoleManager().createLineReader();
            final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
            clientApiKey = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
            if (clientApiKey.contains("exit")) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.clientapikey.exit"), MessageType.WARN);
                System.exit(0);
            }
            if (!clientApiKey.isBlank()) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.clientapikey.error"), MessageType.ERROR);
        }

        ConfigManager.Config config = HugeCloud.getConfigManager().getConfig();
        config.setApiKey(apiKey);
        config.setClientAPIKey(clientApiKey);


        String url;

        while (true) {
            HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
            HugeCloud.getConsoleManager().sendMessageLeer();
            HugeCloud.getConsoleManager().sendMessage("Pelican URL:", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessage("Die URL muss auf http:// oder https:// enden!", MessageType.WARN);
            HugeCloud.getConsoleManager().sendMessage("Geben Sie 'exit' ein, um das Setup zu verlassen.", MessageType.INFO);
            HugeCloud.getConsoleManager().sendMessageLeer();
            LineReader reader = HugeCloud.getConsoleManager().createLineReader();
            final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
            url = reader.readLine(prefix + LanguageManager.getMessage("hugecloud.setup") + " » ");
            if (url.contains("exit")) {
                HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.apikey.exit"), MessageType.WARN);
                System.exit(0);
            }
            if (url.startsWith("http://") || url.startsWith("https://")) {
                break;
            }
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.url.error"), MessageType.ERROR);
        }

        config.setUrl(url);

        HugeCloud.getConfigManager().saveConfig();

        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.success"), MessageType.INFO);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.success.error", e.getMessage()), MessageType.ERROR);
            e.printStackTrace();
        }
        //create folder
        File localFolder = new File(System.getProperty("user.dir") + "/local");
        if (!localFolder.exists() && !localFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.local.error"), MessageType.ERROR);
            return;
        }
        File groupsFolder = new File(System.getProperty("user.dir") + "/local/groups");
        if (!groupsFolder.exists() && !groupsFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.groups.error"), MessageType.ERROR);
            return;
        }

        File templateFolder = new File(System.getProperty("user.dir") + "/templates");
        if (!templateFolder.exists() && !templateFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("hugecloud.init.templates.error"), MessageType.ERROR);
            return;
        }

        String downloadUrl = "https://hugecloud.arolg.dev/HugeCloud-BungeeMaster-1.0.jar";
        File jarFile = new File(templateFolder, "HugeCloud-Master.jar");

        try (var in = new BufferedInputStream(new URL(downloadUrl).openStream());
             var fileOutputStream = new FileOutputStream(jarFile)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }

        onstart();
    }
}
