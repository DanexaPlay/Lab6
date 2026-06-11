package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.UpdateData;
import main.Server.CollectionManager;

import java.util.List;

public class UpdateCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public UpdateCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.UPDATE;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        UpdateData data = (UpdateData) request.getArgument();
        collectionManager.update(data.getId(), data.getFlatData());
        return List.of(CommandResponse.ok("Элемент обновлён!"));
    }
}
