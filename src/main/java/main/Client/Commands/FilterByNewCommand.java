package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class FilterByNewCommand implements ClientCommand {
    public String getName() {
        return "filter_by_new";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 2 || !(str[1].equals("true") || str[1].equals("false"))) {
            throw new IllegalArgumentException("Неверный аргумент!");
        }
        return new CommandRequest(CommandType.FILTER_BY_NEW, Boolean.valueOf(str[1]));
    }
}
