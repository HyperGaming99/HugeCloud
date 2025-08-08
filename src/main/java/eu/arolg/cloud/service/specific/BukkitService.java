package eu.arolg.cloud.service.specific;

import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.service.Service;
import eu.arolg.cloud.service.ServiceState;
import eu.arolg.cloud.utils.MessageType;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BukkitService extends Service {

    private final int maxPlayers;
    private final String name;
    private final String group;
    private final boolean dynamic;
    private final int port;
    private final int ram;
    //Status from ServiceState
    private  ServiceState status;
    private Process process;
    private ProcessBuilder processBuilder;


    public BukkitService(UUID id, int port, int ram, String name, String group, boolean dynamic, int maxPlayers) {
        super(id, port, ram, name, group, dynamic);
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
        this.port = port;
        this.ram = ram;
        status = ServiceState.OFFLINE;
    }
    @Override
    public void start() {
        if (status == ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Service " + name + " is already running.", MessageType.WARN);
            return;
        }
        status = ServiceState.ONLINE;
        File serviceFolder = new File(System.getProperty("user.dir") + "/local/groups/" + name);
        File jarFile = new File(serviceFolder, "server.jar");

        if (!jarFile.exists()) {
            HugeCloud.getConsoleManager().sendMessage("Service JAR not found for: " + name, MessageType.ERROR);
            return;
        }

        try {
            int ram = getRam();
            int ramh = ram / 2;

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-Xmx" + ram + "M",
                    "-Xms" + ramh + "M",
                    "-jar",
                    jarFile.getAbsolutePath(),
                    "nogui"
            );
            processBuilder.directory(serviceFolder);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist gestartet.", MessageType.INFO);
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to start service: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }


    @Override
    public void stop() {
        if (status != ServiceState.ONLINE) {
            HugeCloud.getConsoleManager().sendMessage("Group " + name + " ist nicht online.", MessageType.WARN);
            return;
        }

        if (process != null && process.isAlive()) {
            process.destroy(); // sanfter Versuch
            try {
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly(); // brutal, wenn nötig
                }
            } catch (InterruptedException e) {
                HugeCloud.getConsoleManager().sendMessage("Fehler beim Stoppen von " + name, MessageType.ERROR);
                e.printStackTrace();
            }

            HugeCloud.getConsoleManager().sendMessage("Group " + name + " wurde gestoppt.", MessageType.INFO);
            status = ServiceState.OFFLINE;
        } else {
            HugeCloud.getConsoleManager().sendMessage("Kein laufender Prozess für " + name + ".", MessageType.ERROR);
        }
    }

    @Override
    public void create() {

    }

    public String getName() {
        return name;
    }

    public ServiceState getStatus() {
        return status;
    }

    public int getRam() {
        return ram;
    }
}
