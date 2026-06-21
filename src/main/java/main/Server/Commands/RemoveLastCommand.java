package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class RemoveLastCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveLastCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_LAST;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        String username = request.getUsername();

        Long id = collectionManager.getLastOwnedId(username);

        if (id == null) {
            return List.of(CommandResponse.fail("У вас нет элементов для удаления!"));
        }

        List<Long> removedIds = databaseManager.removeUserFlatsByIds(List.of(id), username);

        if (removedIds.isEmpty()) {
            return List.of(CommandResponse.fail(
                    "Элемент не найден в БД или вы не являетесь его автором!"
            ));
        }

        collectionManager.removeByIds(removedIds);

        return List.of(CommandResponse.ok("Последний ваш элемент удалён!"));
    }
}
