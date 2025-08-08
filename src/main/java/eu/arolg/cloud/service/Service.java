package eu.arolg.cloud.service;

import java.util.UUID;

public abstract class Service {

    private final int port;
    private final UUID uniqueId;
    private final int ram;
    private final String group;
    private final boolean dynamic;


    public Service(UUID id, int port, int ram, String group, boolean dynamic) {
        this.uniqueId = id;
        this.port = port;
        this.ram = ram;
        this.group = group;
        this.dynamic = dynamic;
    }

    public abstract void start();
    public abstract void stop();
    public abstract void create();
}
