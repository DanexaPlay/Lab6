package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class InfoCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.INFO;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        return List.of(CommandResponse.ok(collectionManager.info()));
    }
}
