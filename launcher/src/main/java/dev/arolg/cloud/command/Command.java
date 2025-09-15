package dev.arolg.cloud.command;

import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Command {
    private String command;
    private String[] alias;
    public Command(String command, String... alias) {
        this.command = command;
        this.alias = alias;
    }

    public Command(String command) {
        this.command = command;
        this.alias = null;
    }

    public List<String> getTabCompletion(String[] args) {
        return new ArrayList<>(); // Standardmäßig keine Vorschläge
    }
    public void execute(String[] args) throws Exception {

    }
}
