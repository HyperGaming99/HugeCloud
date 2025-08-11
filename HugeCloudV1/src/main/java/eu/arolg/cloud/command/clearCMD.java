package eu.arolg.cloud.command;

import eu.arolg.cloud.HugeCloud;

public class clearCMD extends Command {

    public clearCMD() {
        super("clear");
    }

    @Override
    public void execute(String[] args) {
        HugeCloud.getConsoleManager().clearConsole(HugeCloud.getConsoleManager().createLineReader().getTerminal());
    }
}
