package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class RemoveByIdCommand implements ClientCommand {
    public String getName() {
        return "remove_by_id";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 2) {
            throw new IllegalArgumentException("Неверный аргумент!");
        }
        return new CommandRequest(CommandType.REMOVE_BY_ID, Long.parseLong(str[1]));
    }
}
