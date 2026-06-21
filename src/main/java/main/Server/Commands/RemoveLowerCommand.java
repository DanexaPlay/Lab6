package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.FlatData;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class RemoveLowerCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveLowerCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_LOWER;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        if (!(request.getArgument() instanceof FlatData data)) {
            return List.of(CommandResponse.fail("Неверный аргумент для remove_lower!"));
        }

        String username = request.getUsername();

        List<Long> candidateIds = collectionManager.getLowerOwnedIds(data, username);

        if (candidateIds.isEmpty()) {
            return List.of(CommandResponse.ok("Нет ваших элементов меньше заданного."));
        }

        List<Long> removedIds = databaseManager.removeUserFlatsByIds(candidateIds, username);

        if (removedIds.isEmpty()) {
            return List.of(CommandResponse.fail("Не удалось удалить элементы из БД."));
        }

        collectionManager.removeByIds(removedIds);

        return List.of(CommandResponse.ok("Удалено ваших элементов: " + removedIds.size()));
    }
}
