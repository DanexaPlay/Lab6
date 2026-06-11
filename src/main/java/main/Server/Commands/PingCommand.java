package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;

import java.util.List;

public class PingCommand implements ServerCommand {
    public CommandType getType() {
        return CommandType.PING;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        return List.of(CommandResponse.ok(""));
    }
}
