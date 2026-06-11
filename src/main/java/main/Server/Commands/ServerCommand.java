package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;

import java.util.List;

public interface ServerCommand {
    CommandType getType();
    List<CommandResponse> execute(CommandRequest request);
}
