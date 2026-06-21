package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public List execute(CommandRequest request) {
        if (!(request.getArgument() instanceof List<?> scriptCommands)) {
            return List.of(CommandResponse.fail("Неверный аргумент команды!"));
        }

        logger.info("Выполнение скрипта, команд: {}", scriptCommands.size());

        StringBuilder result = new StringBuilder();
        boolean allSuccess = true;

        for (Object object : scriptCommands) {
            if (!(object instanceof CommandRequest scriptRequest)) {
                allSuccess = false;
                result.append("----- НЕВЕРНАЯ КОМАНДА -----\n");
                result.append("В скрипте есть неверная команда.\n\n");
                continue;
            }

            CommandType type = scriptRequest.getType();

            result.append("----- ")
                    .append(type)
                    .append(" -----\n");

            if (type == CommandType.EXECUTE_SCRIPT) {
                allSuccess = false;
                result.append("Рекурсивный вызов скрипта запрещён!\n\n");
                continue;
            }

            if (type == CommandType.SAVE) {
                allSuccess = false;
                result.append("Команда save доступна только на сервере!\n\n");
                continue;
            }

            if (type == CommandType.EXIT) {
                allSuccess = false;
                result.append("Команда exit не выполняется из скрипта.\n\n");
                continue;
            }

            if (request.hasCredentials() && !scriptRequest.hasCredentials()) {
                scriptRequest.withCredentials(
                        request.getUsername(),
                        request.getHash_password()
                );
            }

            List responses = commandProcessor.process(scriptRequest);

            if (responses == null || responses.isEmpty()) {
                result.append("Команда не вернула ответ.\n\n");
                continue;
            }

            for (Object responseObject : responses) {
                if (!(responseObject instanceof CommandResponse response)) {
                    allSuccess = false;
                    result.append("Некорректный ответ от команды.\n");
                    continue;
                }

                if (!response.isSuccess()) {
                    allSuccess = false;
                }

                appendResponse(result, response);
            }

            result.append("\n");
        }

        String message = result.toString().trim();

        if (message.isBlank()) {
            message = "Скрипт выполнен.";
        }

        if (allSuccess) {
            return List.of(CommandResponse.ok(message));
        }

        return List.of(CommandResponse.fail(message));
    }

    private void appendResponse(StringBuilder result, CommandResponse response) {
        if (response.getMessage() != null && !response.getMessage().isBlank()) {
            result.append(response.getMessage()).append("\n");
        }

        Object data = response.getData();

        if (data == null) {
            return;
        }

        if (data instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                result.append(item).append("\n");
            }
            return;
        }

        result.append(data).append("\n");
    }
}