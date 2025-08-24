package dev.arolg.hugeCloudBMaster.utils;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.proxy.ProxyServer;

public interface ICommand {
    BrigadierCommand createBrigadierCommand(final ProxyServer proxy);
}
