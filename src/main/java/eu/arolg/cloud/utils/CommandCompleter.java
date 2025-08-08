package eu.arolg.cloud.utils;

import eu.arolg.cloud.managers.CommandManager;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements Completer {

    private final CommandManager commandManager;

    public CommandCompleter(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String input = line.line();
        List<String> suggestions = commandManager.getTabCompletion(input);
        for (String suggestion : suggestions) {
            candidates.add(new Candidate(suggestion));
        }
    }
}