package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class InfoCommand implements ClientCommand {
    public String getName() {
        return "info";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.INFO, null);
    }
}
