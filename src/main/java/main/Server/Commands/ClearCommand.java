package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class ClearCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.CLEAR;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        collectionManager.clear();
        return List.of(CommandResponse.ok("Коллекция очищена!"));
    }
}
