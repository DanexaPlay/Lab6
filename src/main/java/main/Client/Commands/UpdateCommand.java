package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Client.Network.NetworkClient;
import main.Common.*;

import java.util.List;

public class UpdateCommand implements ClientCommand {
    private final NetworkClient networkClient;

    public UpdateCommand(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 2) {
            throw new IllegalArgumentException("Неверный аргумент! Использование: update <id>");
        }

        long id;

        try {
            id = Long.parseLong(str[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть числом!");
        }

        CommandRequest checkRequest = new CommandRequest(
                CommandType.CHECK_UPDATE_PERMISSION,
                id
        );

        List<CommandResponse> checkResponses = networkClient.send(checkRequest);

        if (checkResponses == null || checkResponses.isEmpty()) {
            throw new IllegalArgumentException("Сервер не вернул ответ на проверку прав!");
        }

        CommandResponse checkResponse = checkResponses.get(0);

        if (!checkResponse.isSuccess()) {
            throw new IllegalArgumentException(checkResponse.getMessage());
        }

        FlatData flatData = reader.readFlat();

        return new CommandRequest(
                CommandType.UPDATE,
                new UpdateData(id, flatData)
        );
    }
}
