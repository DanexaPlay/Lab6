package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class CheckUpdatePermissionCommand implements ServerCommand {
    private final DatabaseManager databaseManager;

    public CheckUpdatePermissionCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public CommandType getType() {
        return CommandType.CHECK_UPDATE_PERMISSION;
    }

    @Override
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

        if (!databaseManager.isFlatOwner(id, username)) {
            return List.of(CommandResponse.fail(
                    "Элемент не найден или вы не являетесь его автором!"
            ));
        }

        return List.of(CommandResponse.ok("Можно обновлять элемент"));
    }
}