package dev.arolg.hugeCloudBMaster.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.arolg.hugeCloudBMaster.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class TabListUpdater {

    private final ProxyServer proxyServer;
    private final Object plugin;
    private ScheduledTask currentTask;
    private final File configFile;

    public TabListUpdater(ProxyServer proxyServer, Object plugin, File configFile) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.configFile = configFile;
    }

    public void startUpdating() {
        stopUpdating();

        currentTask = proxyServer.getScheduler().buildTask(plugin, () -> {
            try {
                ConfigManager configManager = new ConfigManager(configFile.getParentFile());
                configManager.loadConfig();

                String headerText = configManager.getConfig().tablist.header;
                String footerText = configManager.getConfig().tablist.footer;

                for (Player player : proxyServer.getAllPlayers()) {
                    Component header = convertLegacyToMiniMessage(headerText
                            .replace("%player_name%", player.getUsername())
                            .replace("%online_players%", String.valueOf(proxyServer.getPlayerCount()))
                            .replace("%server%", player.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse("Unknown"))
                            .replace("%max_players%", String.valueOf(proxyServer.getConfiguration().getShowMaxPlayers()))
                    );

                    Component footer = convertLegacyToMiniMessage(footerText
                            .replace("%player_name%", player.getUsername())
                            .replace("%online_players%", String.valueOf(proxyServer.getPlayerCount()))
                            .replace("%server%", player.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse("Unknown"))
                            .replace("%max_players%", String.valueOf(proxyServer.getConfiguration().getShowMaxPlayers()))
                    );

                    player.sendPlayerListHeaderAndFooter(header, footer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    public void stopUpdating() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    public static Component convertLegacyToMiniMessage(String text) {
        String miniMessageText = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
        return MiniMessage.miniMessage().deserialize(miniMessageText);
    }
}
