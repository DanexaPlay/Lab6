package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class AverageOfNumberOfRoomsCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public AverageOfNumberOfRoomsCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.AVERAGE_OF_NUMBER_OF_ROOMS;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        return List.of(CommandResponse.ok(collectionManager.average_of_number_of_rooms()));
    }
}
