package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class ReorderCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public ReorderCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.REORDER;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        collectionManager.reorder();
        return List.of(CommandResponse.ok("Коллекция перевёрнута!"));
    }
}
