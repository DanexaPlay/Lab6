package main.Server.Commands;

import main.BasicClasses.House;
import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class CountGreaterThanHouseCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public CountGreaterThanHouseCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.COUNT_GREATER_THAN_HOUSE;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        int count = collectionManager.count_greater_than_house((House) request.getArgument());
        return List.of(CommandResponse.ok("Количество элементов: " + count));
    }
}
