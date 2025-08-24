package dev.arolg.hugeCloudBMaster.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PingEvent {
    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing ping = event.getPing();
        ServerPing.Builder pingBuilder = ping.asBuilder();

        String miniMessageText = "<aqua><bold>HugeCloud</bold></aqua> <dark_gray>-</dark_gray> <gray>Simple and lightweight</gray> <dark_gray>-</dark_gray> <white>0.0.1</white><reset>";
        Component motd = MiniMessage.miniMessage().deserialize(miniMessageText);

        pingBuilder.description(motd);

        event.setPing(pingBuilder.build());
    }
}
