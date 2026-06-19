package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;
import main.Server.DatabaseManagment.DatabaseManager;
import main.Server.FileManagment.FileManager;

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

    public CommandProcessor(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
        databaseManager = null;
        add(new HelpCommand());
        add(new InfoCommand(collectionManager));
        add(new ShowCommand(collectionManager));
        add(new AddCommand(collectionManager));
        add(new UpdateCommand(collectionManager));
        add(new RemoveByIdCommand(collectionManager));
        add(new ClearCommand(collectionManager));
        add(new RemoveLastCommand(collectionManager));
        add(new ReorderCommand(collectionManager));
        add(new AverageOfNumberOfRoomsCommand(collectionManager));
        add(new RemoveLowerCommand(collectionManager));
        add(new CountGreaterThanHouseCommand(collectionManager));
        add(new FilterByNewCommand(collectionManager));
        add(new SaveCommand());
        add(new ExitCommand());
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
        add(new AddCommand(collectionManager));
        add(new UpdateCommand(collectionManager));
        add(new RemoveByIdCommand(collectionManager));
        add(new ClearCommand(collectionManager));
        add(new RemoveLastCommand(collectionManager));
        add(new ReorderCommand(collectionManager));
        add(new AverageOfNumberOfRoomsCommand(collectionManager));
        add(new RemoveLowerCommand(collectionManager));
        add(new CountGreaterThanHouseCommand(collectionManager));
        add(new FilterByNewCommand(collectionManager));
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

    public List<CommandResponse> process(CommandRequest request) {
        try {
            if (request == null || request.getType() == null) {
                return List.of(CommandResponse.fail("Команда не передана!"));
            }
            ServerCommand command = commands.get(request.getType());
            logger.info("Получена команда: {}", request.getType());
            if (command == null) {
                return List.of(CommandResponse.fail("Команда не существует или не заданы аргументы"));
            }
            return command.execute(request);
        } catch (ClassCastException e) {
            logger.warn("Неверный аргумент команды", e);
            return List.of(CommandResponse.fail("Неверный аргумент команды!"));
        } catch (IllegalArgumentException e) {
            logger.warn("Команда не выполнена", e);
            return List.of(CommandResponse.fail(e.getMessage() == null ? "Команда не выполнена." : e.getMessage()));
        }
        catch (Exception e) {
            logger.error("Ошибка выполнения команды", e);
            return List.of(CommandResponse.fail("Команда не выполнена. Проверьте данные."));
        }
    }

    public void save() {
        if (this.fileManager != null) {
            logger.info("Сохранение коллекции");
            fileManager.write_to_file(collectionManager.getCollection());
        }
        else {
            logger.info("Коллекция не сохранена, поскольку режим записи в файл отключён");
        }
    }
}
