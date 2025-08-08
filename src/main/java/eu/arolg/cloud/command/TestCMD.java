package eu.arolg.cloud.command;

import eu.arolg.cloud.HugeCloud;

public class TestCMD extends Command{
    public TestCMD() {
        super("test");
    }

    @Override
    public void execute(String[] args) {
        HugeCloud.getConsoleManager().sendMessage("Test ;)", eu.arolg.cloud.utils.MessageType.INFO);
    }
}
