package dev.arolg.hugeCloudBMaster;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.arolg.hugeCloudBMaster.listener.OnServerSwitch;
import dev.arolg.hugeCloudBMaster.listener.PingEvent;
import dev.arolg.hugeCloudBMaster.listener.TabListUpdater;
import dev.arolg.hugeCloudBMaster.utils.ConfigManager;
import dev.arolg.hugeCloudBMaster.utils.ICommand;
import dev.arolg.hugeCloudBMaster.utils.ServersLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "hugecloudbmaster", name = "HugeCloudBMaster", version = "1.0.1")
public class HugeCloudBMaster {

    public static ProxyServer proxyServer;
    public static Path dataDirectory;
    private final Gson gson = new Gson();

    //ProxyDirectory

    @Inject
    public HugeCloudBMaster(ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        HugeCloudBMaster.proxyServer = proxyServer;
        HugeCloudBMaster.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ServersLoader.onLoad();

        //   try {
           // ConfigManager configManager = new ConfigManager(ServersLoader.proxyDir);
            //  configManager.loadConfig();

            //  TabListUpdater updater = new TabListUpdater(proxyServer, this, new File(ServersLoader.proxyDir, "config.yml"));
            // updater.startUpdating();
            //   } catch (IOException e) {
            //      throw new RuntimeException(e);
            //   }

          proxyServer.getEventManager().register(this, new OnServerSwitch());
          proxyServer.getEventManager().register(this, new PingEvent());

        //    commandRegister("hub", new dev.arolg.hugeCloudBMaster.command.hub(proxyServer, this, new File(ServersLoader.proxyDir, "config.yml")), false);
    }

    public void commandRegister(String name, ICommand cmd, Boolean alias, String... aliases) {
        CommandManager commandManager = proxyServer.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(name).plugin(this).build();
        if(alias) {
            commandMeta = commandManager.metaBuilder(name).plugin(this).aliases(aliases).build();
        }

        BrigadierCommand setDefaultCommand = cmd.createBrigadierCommand(proxyServer);

        commandManager.register(commandMeta, setDefaultCommand);
    }

}
