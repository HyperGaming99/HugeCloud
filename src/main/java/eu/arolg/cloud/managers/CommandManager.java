package eu.arolg.cloud.managers;

import eu.arolg.cloud.HugeCloud;
import eu.arolg.cloud.command.Command;
import eu.arolg.cloud.utils.ANSICodes;
import eu.arolg.cloud.utils.CommandCompleter;
import eu.arolg.cloud.utils.MessageType;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandManager {


    private final HashMap<String, Command> commands = new HashMap<>();
    public void registerCommand(Command command) {
        commands.put(command.getCommand().toLowerCase(), command);
        if(command.getAlias() == null) return;
        for (String alias : command.getAlias()) {
            commands.put(alias.toLowerCase(), command);
        }
    }
    public Runnable reading() {
        return new Runnable() {
            @Override
            public void run() {
                LineReader reader = LineReaderBuilder.builder()
                        .completer(new CommandCompleter(CommandManager.this)) // Attach the completer
                        .build();

                while (true) {
                    try {
                        String s = reader.readLine(ANSICodes.BRIGHT_CYAN + "HugeCloud" + ANSICodes.RESET + " | ");
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
                        HugeCloud.getConsoleManager().sendMessage("Eingabe unterbrochen. Beende...", MessageType.INFO);
                        break;
                    } catch (Exception e) {
                        HugeCloud.getConsoleManager().sendMessage("Ein Fehler ist aufgetreten: " + e.getMessage(), MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public List<String> getTabCompletion(String input) {
        List<String> suggestions = new ArrayList<>();
        String[] parts = input.split(" ");
        String commandPart = parts[0].toLowerCase();

        if (commands.containsKey(commandPart)) {
            Command command = commands.get(commandPart);
            suggestions.addAll(command.getTabCompletion(parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0]));
        } else {
            for (String commandName : commands.keySet()) {
                if (commandName.startsWith(commandPart)) {
                    suggestions.add(commandName);
                }
            }
        }

        return suggestions;
    }
}
