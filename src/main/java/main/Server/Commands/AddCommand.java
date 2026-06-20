package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.FlatData;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class AddCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public AddCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    public CommandType getType() {
        return CommandType.ADD;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        if (!databaseManager.isConnected()) {
            return List.of(CommandResponse.fail("База данных недоступна"));
        }
        long id = databaseManager.addFlat((FlatData) request.getArgument(), request.getUsername());
        if (id != -1) {
            collectionManager.add((FlatData) request.getArgument(), id);
            return List.of(CommandResponse.ok("Объект успешно добавлен!"));
        }
        else {
            return List.of(CommandResponse.fail("Объект не был добавлен"));
        }
    }
}
