package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class RemoveByIdCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_BY_ID;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        collectionManager.remove_by_id((Long) request.getArgument());
        return List.of(CommandResponse.ok("Элемент удалён!"));
    }
}
