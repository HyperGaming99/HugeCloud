package dev.arolg.hugeCloudBMaster.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.arolg.hugeCloudBMaster.HugeCloudBMaster;
import dev.arolg.hugeCloudBMaster.utils.ConfigManager;
import dev.arolg.hugeCloudBMaster.utils.ICommand;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.IOException;

public class hub implements ICommand {

    private final ProxyServer proxyServer;
    private final HugeCloudBMaster plugin;
    private final File configFile;


    public hub(ProxyServer proxyServer, HugeCloudBMaster plugin,File configFile) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.configFile = configFile;
    }
    @Override
    public BrigadierCommand createBrigadierCommand(ProxyServer proxy) {
        LiteralCommandNode<CommandSource> commandNode =
                LiteralArgumentBuilder.<CommandSource>literal("hub")
                        .requires(commandSource -> true)
                        .executes(context -> {
                            ConfigManager configManager = new ConfigManager(configFile.getParentFile());
                            try {
                                configManager.loadConfig();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            String hubServerName = configManager.getConfig().hub.hub;
                            CommandSource source = context.getSource();
                            if (source instanceof Player player) {
                                proxy.getServer(hubServerName).ifPresentOrElse(hubServer -> {
                                    player.createConnectionRequest(hubServer).fireAndForget();
                                    source.sendMessage(Component.text("You have been teleported to the hub!"));
                                }, () -> {
                                    source.sendMessage(Component.text("Hub server is not available."));
                                });
                            } else {
                                source.sendMessage(Component.text("This command can only be used by players."));
                            }
                            return 1;
                        })
                        .build();

        return new BrigadierCommand(commandNode);
    }
}
