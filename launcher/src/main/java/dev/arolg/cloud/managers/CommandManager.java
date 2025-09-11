package dev.arolg.cloud.managers;

import dev.arolg.cloud.HugeCloud;
import dev.arolg.cloud.command.Command;
import dev.arolg.cloud.utils.ANSICodes;
import dev.arolg.cloud.utils.LanguageManager;
import dev.arolg.cloud.utils.MessageType;
import org.jline.reader.LineReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandManager {

    private final HashMap<String, Command> commands = new HashMap<>();

    public void registerCommand(Command command) {
        commands.put(command.getCommand().toLowerCase(), command);
        if (command.getAlias() == null) return;
        for (String alias : command.getAlias()) {
            commands.put(alias.toLowerCase(), command);
        }
    }

    public Runnable reading() {
        return new Runnable() {
            @Override
            public void run() {
                LineReader reader = HugeCloud.getConsoleManager().createLineReader();

                while (true) {
                    try {
                        String s = reader.readLine(ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ");
                        String[] input = s.split(" ");
                        if (commands.containsKey(input[0].toLowerCase())) {
                            List<String> args = new ArrayList<>();
                            for (String i : input) {
                                if (!i.equalsIgnoreCase(input[0])) {
                                    args.add(i);
                                }
                            }
                            commands.get(input[0].toLowerCase()).execute(args.toArray(new String[0]));
                        }
                    } catch (org.jline.reader.UserInterruptException e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("console_exit"), MessageType.INFO);
                        break;
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage(LanguageManager.getMessage("error_executing_command"), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}