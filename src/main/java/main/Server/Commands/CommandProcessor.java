package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.LoginData;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;
import main.Server.FileManagment.FileManager;

import main.Server.Network.ActiveUserManager;
import main.Server.Network.ClientConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProcessor {
    private static final Logger logger = LogManager.getLogger(CommandProcessor.class);
    private final CollectionManager collectionManager;
    private final FileManager fileManager;
    private final Map<CommandType, ServerCommand> commands = new HashMap<>();
    private final DatabaseManager databaseManager;
    private final ActiveUserManager activeUserManager = ActiveUserManager.getInstance();

    public CommandProcessor(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
        databaseManager = null;
        add(new HelpCommand());
        add(new InfoCommand(collectionManager));
        add(new ShowCommand(collectionManager));
        add(new AddCommand(collectionManager, databaseManager));
        add(new UpdateCommand(collectionManager, databaseManager));
        add(new RemoveByIdCommand(collectionManager, databaseManager));
        add(new ClearCommand(collectionManager, databaseManager));
        add(new RemoveLastCommand(collectionManager, databaseManager));
        add(new ReorderCommand(collectionManager));
        add(new AverageOfNumberOfRoomsCommand(collectionManager));
        add(new RemoveLowerCommand(collectionManager, databaseManager));
        add(new CountGreaterThanHouseCommand(collectionManager));
        add(new FilterByNewCommand(collectionManager));
        add(new SaveCommand());
        add(new ExitCommand());
        add(new CheckUpdatePermissionCommand(databaseManager));
        add(new ExecuteScriptCommand(this));
        add(new PingCommand());
        add(new LoginCommand(this.databaseManager));
        add(new RegisterCommand(this.databaseManager));
    }

    public CommandProcessor(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
        add(new HelpCommand());
        add(new InfoCommand(collectionManager));
        add(new ShowCommand(collectionManager));
        add(new AddCommand(collectionManager, databaseManager));
        add(new UpdateCommand(collectionManager, databaseManager));
        add(new RemoveByIdCommand(collectionManager, databaseManager));
        add(new ClearCommand(collectionManager, databaseManager));
        add(new RemoveLastCommand(collectionManager, databaseManager));
        add(new ReorderCommand(collectionManager));
        add(new AverageOfNumberOfRoomsCommand(collectionManager));
        add(new RemoveLowerCommand(collectionManager, databaseManager));
        add(new CountGreaterThanHouseCommand(collectionManager));
        add(new FilterByNewCommand(collectionManager));
        add(new CheckUpdatePermissionCommand(databaseManager));
        add(new SaveCommand());
        add(new ExitCommand());
        add(new ExecuteScriptCommand(this));
        add(new PingCommand());
        add(new LoginCommand(this.databaseManager));
        add(new RegisterCommand(this.databaseManager));
        fileManager = null;
    }

    private void add(ServerCommand command) {
        commands.put(command.getType(), command);
    }

    private boolean isPublicCommand(CommandType type) {
        return type == CommandType.LOGIN
                || type == CommandType.REGISTER
                || type == CommandType.PING;
    }

    public List<CommandResponse> process(CommandRequest request) {
        return process(request, null);
    }

    public List<CommandResponse> process(CommandRequest request, ClientConnection connection) {
        if (request == null || request.getType() == null) {
            return List.of(CommandResponse.fail("Некорректный запрос!"));
        }
        CommandType type = request.getType();
        ServerCommand command = commands.get(type);
        if (command == null) {
            return List.of(CommandResponse.fail("Неизвестная команда!"));
        }
        if (type == CommandType.LOGIN) {
            return processLogin(request, connection, command);
        }
        if (!isPublicCommand(type)) {
            if (!request.hasCredentials()) {
                return List.of(CommandResponse.fail("Для выполнения команды нужно авторизоваться!"));
            }
            if (!databaseManager.checkPassword(
                    request.getUsername(),
                    request.getHash_password()
            )) {
                return List.of(CommandResponse.fail("Неверные данные авторизации!"));
            }
        }
        return command.execute(request);
    }

    private List<CommandResponse> processLogin(
            CommandRequest request,
            ClientConnection connection,
            ServerCommand command
    ) {
        if (connection == null) {
            return List.of(CommandResponse.fail("Команду login нельзя выполнять внутри серверного execute_script!"));
        }
        if (!(request.getArgument() instanceof LoginData loginData)) {
            return List.of(CommandResponse.fail("Неверные данные для login!"));
        }

        String username = loginData.getUsername();

        List<CommandResponse> responses = command.execute(request);

        if (responses.isEmpty() || !responses.get(0).isSuccess()) {
            return responses;
        }

        boolean loggedIn = activeUserManager.login(username);

        if (!loggedIn) {
            return List.of(CommandResponse.fail("Пользователь уже авторизован в другом клиенте!"));
        }

        connection.setUsername(username);

        return responses;
    }

    public void logout(ClientConnection connection) {
        if (connection == null) {
            return;
        }

        activeUserManager.logout(connection.getUsername());
    }

    public void save() {
        if (!databaseManager.isConnected()) {
            return;
        }
        databaseManager.saveCollection(collectionManager.getCollection());
    }
}
