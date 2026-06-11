package main.Client.Commands;

import main.BasicClasses.House;
import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;
import main.Common.CommandType;

public class CountGreaterThanHouseCommand implements ClientCommand {
    public String getName() {
        return "count_greater_than_house";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        House house = reader.readHouse();
        return new CommandRequest(CommandType.COUNT_GREATER_THAN_HOUSE, house);
    }
}
