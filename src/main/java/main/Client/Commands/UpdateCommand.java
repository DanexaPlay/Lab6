package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;
import main.Common.FlatData;
import main.Common.UpdateData;

public class UpdateCommand implements ClientCommand {
    public String getName() {
        return "update";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 2) {
            throw new IllegalArgumentException("Неверный аргумент!");
        }
        long id = Long.parseLong(str[1]);
        FlatData flatData = reader.readFlat();
        return new CommandRequest(CommandType.UPDATE, new UpdateData(id, flatData));
    }
}
