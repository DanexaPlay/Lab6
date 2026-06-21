package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class ClearCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public ClearCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    public CommandType getType() {
        return CommandType.CLEAR;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        String username = request.getUsername();

        List<Long> removedIds = databaseManager.clearUserFlats(username);

        if (removedIds.isEmpty()) {
            return List.of(CommandResponse.ok("Ваши элементы не найдены. Коллекция не изменилась."));
        }

        collectionManager.removeByIds(removedIds);

        return List.of(CommandResponse.ok("Удалено ваших элементов: " + removedIds.size()));
    }
}
