package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class AddCommand implements ClientCommand {
    public String getName() {
        return "add";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.ADD, reader.readFlat());
    }
}
