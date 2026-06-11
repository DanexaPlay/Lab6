package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class RemoveLowerCommand implements ClientCommand {
    public String getName() {
        return "remove_lower";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.REMOVE_LOWER, reader.readFlat());
    }
}
