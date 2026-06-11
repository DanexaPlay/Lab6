package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.FlatData;
import main.Server.CollectionManager;

import java.util.List;

public class AddCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public AddCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.ADD;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        collectionManager.add((FlatData) request.getArgument());
        return List.of(CommandResponse.ok("Объект успешно добавлен!"));
    }
}
