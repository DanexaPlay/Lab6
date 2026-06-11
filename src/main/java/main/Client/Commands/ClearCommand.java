package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class ClearCommand implements ClientCommand {
    public String getName() {
        return "clear";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.CLEAR, null);
    }
}
