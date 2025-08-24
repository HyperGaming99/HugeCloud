package dev.arolg.hugeCloudBMaster.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.arolg.hugeCloudBMaster.HugeCloudBMaster;

import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class ServersLoader {

    private static final Gson gson = new Gson();
    public static File proxyDir;
    public static void onLoad() {
        try {
            File hugeCloudDir = findMainDir(HugeCloudBMaster.dataDirectory);
            File groupsDir = new File(hugeCloudDir, "local/groups");
            if (!groupsDir.exists()) {
                System.err.println("Groups-Verzeichnis nicht gefunden: " + groupsDir.getAbsolutePath());
                return;
            }

            for (File file : groupsDir.listFiles((dir, name) -> name.endsWith(".json"))) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject obj = gson.fromJson(reader, JsonObject.class);
                    if (!"bukkit".equalsIgnoreCase(obj.get("group").getAsString())) continue;

                    String name = obj.get("name").getAsString();
                    int port = obj.get("port").getAsInt();

                    ServerInfo info = new ServerInfo(name, new InetSocketAddress("127.0.0.1", port));
                    if (HugeCloudBMaster.proxyServer.getServer(name).isPresent()) {
                        ServerInfo existing = HugeCloudBMaster.proxyServer.getServer(name).get().getServerInfo();
                        HugeCloudBMaster.proxyServer.unregisterServer(existing);
                        HugeCloudBMaster.proxyServer.registerServer(info);
                        continue;
                    }
                    HugeCloudBMaster.proxyServer.registerServer(info);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File hugeCloudDir = findMainDir(HugeCloudBMaster.dataDirectory);

            File modulesDir = new File(hugeCloudDir, "modules");
            if (!modulesDir.exists()) {
                if (!modulesDir.mkdirs()) {
                    System.err.println("Konnte modules-Verzeichnis nicht erstellen: " + modulesDir.getAbsolutePath());
                    return;
                }
            }

            File proxyDir = new File(modulesDir, "proxy");
            if (!proxyDir.exists()) {
                if (!proxyDir.mkdirs()) {
                    System.err.println("Konnte proxy-Verzeichnis nicht erstellen: " + proxyDir.getAbsolutePath());
                    return;
                }
            }

            ServersLoader.proxyDir = proxyDir;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static File findMainDir(Path start) {
        Path path = start.toAbsolutePath();

        for (int i = 0; i < 5; i++) {
            File potentialDir = path.toFile();
            File localGroups = new File(potentialDir, "local/groups");

            if (localGroups.exists() && localGroups.isDirectory()) {
                return potentialDir;
            }

            Path parent = path.getParent();
            if (parent == null) break;
            path = parent;
        }
        throw new IllegalStateException("Hauptordner mit local/groups nicht gefunden (Startpunkt: " + start + ")");
    }
}
