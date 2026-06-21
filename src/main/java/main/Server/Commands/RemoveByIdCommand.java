package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class RemoveByIdCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveByIdCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    public CommandType getType() {
        return CommandType.REMOVE_BY_ID;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        if (request == null || request.getArgument() == null) {
            return List.of(CommandResponse.fail("Не передан id элемента!"));
        }

        if (!(request.getArgument() instanceof Long id)) {
            return List.of(CommandResponse.fail("Неверный формат id!"));
        }

        String username = request.getUsername();

        if (username == null || username.isBlank()) {
            return List.of(CommandResponse.fail("Пользователь не авторизован!"));
        }

        boolean removedFromDatabase = databaseManager.removeFlatById(id, username);

        if (!removedFromDatabase) {
            return List.of(CommandResponse.fail(
                    "Элемент не найден или вы не являетесь его автором!"
            ));
        }

        try {
            collectionManager.remove_by_id(id);
        } catch (IllegalArgumentException e) {
            return List.of(CommandResponse.ok(
                    "Элемент удалён из БД. В памяти он уже отсутствовал."
            ));
        }

        return List.of(CommandResponse.ok("Элемент удалён!"));
    }
}
