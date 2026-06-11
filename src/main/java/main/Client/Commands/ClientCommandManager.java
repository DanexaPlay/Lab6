package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;

import java.util.HashMap;
import java.util.Map;

public class ClientCommandManager {
    private final Map<String, ClientCommand> commands = new HashMap<>();

    public ClientCommandManager() {
        add(new InfoCommand());
        add(new ShowCommand());
        add(new AddCommand());
        add(new ClearCommand());
        add(new RemoveLastCommand());
        add(new ReorderCommand());
        add(new AverageOfNumberOfRoomsCommand());
        add(new RemoveLowerCommand());
        add(new CountGreaterThanHouseCommand());
        add(new RemoveByIdCommand());
        add(new UpdateCommand());
        add(new FilterByNewCommand());
    }

    private void add(ClientCommand command) {
        commands.put(command.getName(), command);
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        ClientCommand command = commands.get(str[0]);
        if (command == null) {
            throw new IllegalArgumentException("Команда не существует или не заданы аргументы");
        }
        return command.build(str, reader);
    }
}
