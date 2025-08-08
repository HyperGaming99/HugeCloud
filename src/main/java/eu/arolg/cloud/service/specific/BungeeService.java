package eu.arolg.cloud.service.specific;

import eu.arolg.cloud.service.Service;

import java.util.UUID;

public class BungeeService extends Service {


    public BungeeService(UUID id, int port, int ram, String name, String group, boolean dynamic) {
        super(id, port, ram, name, group, dynamic);
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
