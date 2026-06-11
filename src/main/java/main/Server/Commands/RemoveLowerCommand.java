package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.FlatData;
import main.Server.CollectionManager;

import java.util.List;

public class RemoveLowerCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveLowerCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_LOWER;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        int removed = collectionManager.remove_lower((FlatData) request.getArgument());
        return List.of(CommandResponse.ok("Удалено элементов: " + removed));
    }
}
