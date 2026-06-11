package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class RemoveLastCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveLastCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_LAST;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        collectionManager.remove_last();
        return List.of(CommandResponse.ok("Последний элемент удалён!"));
    }
}
