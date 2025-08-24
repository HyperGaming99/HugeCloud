package dev.arolg.hugeCloudBMaster.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class OnServerSwitch {
    @Subscribe
    public void onServerSwitch(ServerPostConnectEvent event) {
        String serverName = event.getPlayer().getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("");

        String text;
        if (serverName.equalsIgnoreCase("test")) {
            text = "Bauserver | 1.21.4 | HugeCloud Bungee Master";
        } else if (serverName.equalsIgnoreCase("lol")) {
            text = "Minecraft Insel | 1.21.4 | HugeCloud Bungee Master";
        } else {
            text = "";
        }

        BossBar bossBar = BossBar.bossBar(
                Component.text(text),
                1.0f,
                BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS
        );

        event.getPlayer().showBossBar(bossBar);
    }
}
