package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ExecuteScriptCommand implements ServerCommand {
    private static final Logger logger = LogManager.getLogger(ExecuteScriptCommand.class);
    private final CommandProcessor commandProcessor;

    public ExecuteScriptCommand(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public CommandType getType() {
        return CommandType.EXECUTE_SCRIPT;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        if (!(request.getArgument() instanceof List<?>)) {
            return List.of(CommandResponse.fail("Неверный аргумент команды!"));
        }

        List<?> list = (List<?>) request.getArgument();
        List<CommandResponse> responses = new ArrayList<>();
        logger.info("Выполнение скрипта, команд: {}", list.size());

        for (Object object : list) {
            if (!(object instanceof CommandRequest)) {
                responses.add(CommandResponse.fail("В скрипте есть неверная команда."));
                continue;
            }

            CommandRequest commandRequest = (CommandRequest) object;
            if (commandRequest.getType() == CommandType.EXECUTE_SCRIPT) {
                responses.add(CommandResponse.fail("Рекурсивный вызов скрипта запрещён!"));
                continue;
            }
            if (commandRequest.getType() == CommandType.SAVE) {
                responses.add(CommandResponse.fail("Команда save доступна только на сервере!"));
                continue;
            }
            if (commandRequest.getType() == CommandType.EXIT) {
                responses.add(CommandResponse.fail("Команда exit не выполняется из скрипта."));
                continue;
            }

            responses.addAll(commandProcessor.process(commandRequest));
        }

        if (responses.isEmpty()) {
            responses.add(CommandResponse.ok("Скрипт выполнен."));
        } else {
            CommandResponse last = responses.get(responses.size() - 1);
            responses.set(responses.size() - 1, new CommandResponse(
                    last.isSuccess(),
                    last.getMessage(),
                    last.getData(),
                    true
            ));
        }
        return responses;
    }
}
