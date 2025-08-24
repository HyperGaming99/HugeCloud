package dev.arolg.cloud.managers;

import dev.arolg.cloud.utils.ANSICodes;
import dev.arolg.cloud.utils.MessageType;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConsoleManager {

    /**
     * Credits: Code2LearnYT
     * @return
     */

    public LineReader createLineReader() {
        while (true) {
            try {
                Terminal terminal = TerminalBuilder.builder()
                        .system(true)
                        .encoding(StandardCharsets.UTF_8)
                        .build();

                return LineReaderBuilder.builder().terminal(terminal)
                        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                        .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
                        .option(LineReader.Option.INSERT_TAB, false)
                        .build();

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(ANSICodes.BRIGHT_RED + "HugeCloud" + ANSICodes.RESET + "  ! ");
        }
    }


    public void sendMessage(String message, MessageType type) {
        final String prefix = ANSICodes.BRIGHT_CYAN + "hugecloud@v1 " + ANSICodes.RESET + " » ";
        final String info = "INFO: ";
        final String warn = ANSICodes.BRIGHT_YELLOW + "WARN: " + ANSICodes.RESET + ": ";
        final String error = ANSICodes.BRIGHT_RED + "ERROR: " + ANSICodes.RESET + ": ";

        switch (type) {
            case INFO -> System.out.println(prefix + info + message);
            case WARN -> System.out.println(prefix + warn + message);
            case ERROR -> System.out.println(prefix + error + message);
            default -> System.out.println(prefix + "UNKNOWN TYPE: " + message);
        }
    }

    public void sendMessageLeer() {
        System.out.println(ANSICodes.RESET + " ");
    }

    public static void clearConsole(Terminal terminal) {
        try {
            terminal.writer().print("\033[H\033[2J");
            terminal.writer().flush();
        } catch (Exception e) {
            System.err.println("Failed to clear console: " + e.getMessage());
        }
    }

}
