package eu.arolg.cloud.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arolg.cloud.service.ServiceType;
import eu.arolg.cloud.utils.MessageType;
import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.utils.PortFinder;
import org.jline.reader.LineReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ServiceCMD extends Command {

    public ServiceCMD() {
        super("service", "srv");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            HugeCloud.getConsoleManager().sendMessage("Usage: service <create|list|start>", MessageType.INFO);
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
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());

        LineReader reader = HugeCloud.getConsoleManager().createLineReader();

        // Prompt for service details
        String serviceName = reader.readLine("Service Name: ");
        String ram = reader.readLine("RAM (in MB): ");
        String group = reader.readLine("Group: ");

        // Prompt for and validate port
        int port;
        while (true) {
            try {
                String portInput = reader.readLine("Port: ");
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
        HugeCloud.getConsoleManager().sendMessage("Service Name: " + serviceName, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("RAM: " + ram + " MB", MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Group: " + group, MessageType.INFO);
        HugeCloud.getConsoleManager().sendMessage("Port: " + port, MessageType.INFO);

        String confirmation = reader.readLine("Confirm creation? (yes/no): ");
        if (!confirmation.equalsIgnoreCase("yes") && !confirmation.equalsIgnoreCase("y")) {
            HugeCloud.getConsoleManager().sendMessage("Service creation canceled.", MessageType.INFO);
            return;
        }

        // Create service folder
        File serviceFolder = new File(System.getProperty("user.dir") + "/services/" + serviceName);
        if (!serviceFolder.exists() && !serviceFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create service folder.", MessageType.ERROR);
            return;
        }

        // Create configs folder
        File configsFolder = new File(System.getProperty("user.dir") + "/services/configs");
        if (!configsFolder.exists() && !configsFolder.mkdirs()) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create configs folder.", MessageType.ERROR);
            return;
        }

        // Generate UUID and write to name.json
        UUID serviceId = UUID.randomUUID();
        JsonObject serviceData = new JsonObject();
        serviceData.addProperty("id", serviceId.toString());
        serviceData.addProperty("name", serviceName);
        serviceData.addProperty("ram", ram);
        serviceData.addProperty("group", group);
        serviceData.addProperty("port", port);

        File configFile = new File(configsFolder, serviceName + ".json");
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(configFile.toPath(), gson.toJson(serviceData));
            HugeCloud.getConsoleManager().sendMessage("Config file created: " + configFile.getAbsolutePath(), MessageType.INFO);
        } catch (Exception e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to write config file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
            return;
        }

        // Create eula.txt
        File eulaFile = new File(serviceFolder, "eula.txt");
        try {
            Files.writeString(eulaFile.toPath(), "eula=true");
            HugeCloud.getConsoleManager().sendMessage("EULA file created: " + eulaFile.getAbsolutePath(), MessageType.INFO);
        } catch (IOException e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to create EULA file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
            return;
        }

        // Download JAR file
        String downloadUrl = "https://fill-data.papermc.io/v1/objects/5ee4f542f628a14c644410b08c94ea42e772ef4d29fe92973636b6813d4eaffc/paper-1.21.4-232.jar";
        File jarFile = new File(serviceFolder, "server.jar");

        try (var in = new BufferedInputStream(new URL(downloadUrl).openStream());
             var fileOutputStream = new FileOutputStream(jarFile)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            HugeCloud.getConsoleManager().sendMessage("Service created successfully at: " + serviceFolder.getAbsolutePath(), MessageType.INFO);
        } catch (Exception e) {
            HugeCloud.getConsoleManager().sendMessage("Failed to download JAR file: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    public void onList() {
        File configsFolder = new File(System.getProperty("user.dir") + "/services/configs");
        if (!configsFolder.exists() || !configsFolder.isDirectory()) {
            HugeCloud.getConsoleManager().sendMessage("No services found.", MessageType.INFO);
            return;
        }

        File[] configFiles = configsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null || configFiles.length == 0) {
            HugeCloud.getConsoleManager().sendMessage("No services found.", MessageType.INFO);
            return;
        }

        HugeCloud.getConsoleManager().sendMessage("Available Services:", MessageType.INFO);
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