package dev.arolg.hugeCloudBMaster.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.arolg.hugeCloudBMaster.HugeCloudBMaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;

public class ServersLoader {

    private static final Gson gson = new Gson();
        public static void onLoad() {
            try {
                // Call the API to get all groups
                URL url = new URL("http://77.90.37.181:1000/groups");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String response = reader.readLine();
                        if (response == null || response.isEmpty()) {
                            System.err.println("Leere Antwort von API");
                            return;
                        }

                        response = response.trim();

                        // Prüfen, ob es wie ein JSON-Array aussieht
                        if (response.startsWith("[") && response.endsWith("]")) {
                            response = response.substring(1, response.length() - 1); // [ ... ] entfernen
                        }

                        // Kommas trennen, falls mehrere Server vorhanden
                        String[] entries = response.split(",");

                        for (String entry : entries) {
                            entry = entry.trim();
                            // Entferne ggf. Anführungszeichen
                            if (entry.startsWith("\"") && entry.endsWith("\"")) {
                                entry = entry.substring(1, entry.length() - 1);
                            }

                            String[] parts = entry.split(" - localhost:");
                            if (parts.length != 2) continue;

                            String name = parts[0].trim();
                            int port = Integer.parseInt(parts[1].trim());

                            ServerInfo info = new ServerInfo(name, new InetSocketAddress("0.0.0.0", port));

                            if (HugeCloudBMaster.proxyServer.getServer(name).isPresent()) {
                                ServerInfo existing = HugeCloudBMaster.proxyServer.getServer(name).get().getServerInfo();
                                HugeCloudBMaster.proxyServer.unregisterServer(existing);
                                HugeCloudBMaster.proxyServer.registerServer(info);
                            } else {
                                HugeCloudBMaster.proxyServer.registerServer(info);
                            }
                        }
                    }
                } else {
                    System.err.println("Failed to fetch groups from API. HTTP response code: " + connection.getResponseCode());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
}
