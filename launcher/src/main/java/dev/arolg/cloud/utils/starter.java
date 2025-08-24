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
        groupsloader.printServiceTable(HugeCloud.bukkitServices, HugeCloud.bungeeTasks);
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
            for (BukkitTask service : HugeCloud.bukkitServices) {
                if (service.getStatus() == TaskState.ONLINE) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Fehler beim Stoppen des Dienstes " + service.getName() + ": " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
            for (BungeeTask service : HugeCloud.bungeeTasks) {
                if (service.getStatus() == TaskState.ONLINE) {
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

            for (BungeeTask service : HugeCloud.bungeeTasks) {
                if (service.getStatus() == TaskState.OFFLINE) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Fehler beim Starten des Dienstes " + service.getName() + ": " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
            for (BukkitTask service : HugeCloud.bukkitServices) {
                if (service.getStatus() == TaskState.OFFLINE) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Fehler beim Starten des Dienstes " + service.getName() + ": " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    public static void onInit() throws IOException {
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

        File templateFolder = new File(System.getProperty("user.dir") + "/templates");
        if (!templateFolder.exists() && !templateFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Fehler beim Erstellen des templates Ordners.", MessageType.ERROR);
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
