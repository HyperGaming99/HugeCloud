package dev.arolg.cloud.command;

public class ShutdownCMD extends Command {

    public ShutdownCMD() {
        super("shutdown", "exit", "stop");
    }

    @Override
    public void execute(String[] args) {
        System.exit(0);
    }
}
