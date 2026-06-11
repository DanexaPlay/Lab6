package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class RemoveLastCommand implements ClientCommand {
    public String getName() {
        return "remove_last";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.REMOVE_LAST, null);
    }
}
