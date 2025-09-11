package dev.arolg.cloud.tasks;

import java.io.IOException;
import java.util.UUID;

public abstract class Task {

    private int port;
    private String uniqueId;
    private int ram;
    private String name;
    private String group;
    private boolean dynamic;

    public Task(String id, int port, int ram, String name , String group, boolean dynamic) {
        this.uniqueId = id;
        this.port = port;
        this.ram = ram;
        this.name = name;
        this.group = group;
        this.dynamic = dynamic;
    }

    public abstract void start() throws IOException, InterruptedException;
    public abstract void restart() throws IOException, InterruptedException;
    public abstract void stop() throws IOException, InterruptedException;
    public abstract void create(String version) throws Exception;
}
