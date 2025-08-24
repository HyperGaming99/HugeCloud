package dev.arolg.cloud.tasks;

import java.util.UUID;

public abstract class Task {

    private final int port;
    private final UUID uniqueId;
    private final int ram;
    private final String name;
    private final String group;
    private final boolean dynamic;

    public Task(UUID id, int port, int ram, String name , String group, boolean dynamic) {
        this.uniqueId = id;
        this.port = port;
        this.ram = ram;
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
    }

    public abstract void start();
    public abstract void restart();
    public abstract void stop();
    public abstract void create(String version);
}
