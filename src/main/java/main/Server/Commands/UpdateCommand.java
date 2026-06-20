package main.Server.Commands;

import main.BasicClasses.Flat;
import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.UpdateData;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;

import java.util.List;

public class UpdateCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public UpdateCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public CommandType getType() {
        return CommandType.UPDATE;
    }

    @Override
    public List<CommandResponse> execute(CommandRequest request) {
        if (request == null || request.getArgument() == null) {
            return List.of(CommandResponse.fail("Не переданы данные для обновления!"));
        }

        if (!(request.getArgument() instanceof UpdateData data)) {
            return List.of(CommandResponse.fail("Неверный формат данных для update!"));
        }

        String username = request.getUsername();

        if (username == null || username.isBlank()) {
            return List.of(CommandResponse.fail("Пользователь не определён!"));
        }

        Flat updatedFlat = databaseManager.updateFlat(
                data.getId(),
                data.getFlatData(),
                username
        );

        if (updatedFlat == null) {
            return List.of(CommandResponse.fail(
                    "Элемент не найден или вы не являетесь его автором!"
            ));
        }

        boolean updatedInMemory = collectionManager.replaceById(updatedFlat.getId(), updatedFlat);

        if (!updatedInMemory) {
            return List.of(CommandResponse.fail(
                    "Элемент обновлён в БД, но не найден в коллекции в памяти!"
            ));
        }

        return List.of(CommandResponse.ok("Элемент обновлён!"));
    }
}