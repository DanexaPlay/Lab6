package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;

import java.util.List;

public class SaveCommand implements ServerCommand {
    public CommandType getType() {
        return CommandType.SAVE;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        return List.of(CommandResponse.fail("Команда save доступна только на сервере!"));
    }
}
