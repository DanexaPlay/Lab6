package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class AverageOfNumberOfRoomsCommand implements ClientCommand {
    public String getName() {
        return "average_of_number_of_rooms";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        return new CommandRequest(CommandType.AVERAGE_OF_NUMBER_OF_ROOMS, null);
    }
}
