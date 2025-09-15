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
                URL url = new URL("http://77.90.37.182:1000/groups");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        JsonArray groups = gson.fromJson(reader, JsonArray.class);

                        for (int i = 0; i < groups.size(); i++) {
                            String groupInfo = groups.get(i).getAsString();
                            String[] parts = groupInfo.split(" - localhost:");
                            if (parts.length != 2) continue;

                            String name = parts[0];
                            int port = Integer.parseInt(parts[1]);

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
                } else {
                    System.err.println("Failed to fetch groups from API. HTTP response code: " + connection.getResponseCode());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
}
