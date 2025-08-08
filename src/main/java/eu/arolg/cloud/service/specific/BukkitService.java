package eu.arolg.cloud.service.specific;

import eu.arolg.cloud.service.Service;

import java.util.UUID;

public class BukkitService extends Service {

    private final int maxPlayers;

    public BukkitService(UUID id, int port, int ram, String group, boolean dynamic, int maxPlayers) {
        super(id, port, ram, group, dynamic);
        this.maxPlayers = maxPlayers;
    }
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void create() {

    }
}
