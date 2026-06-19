package main.Server.Commands;

import main.Common.*;
import main.Server.DatabaseManagment.DatabaseManager;
import main.Server.DatabaseManagment.UserAlreadyExistsException;
import main.Server.Network.ClientConnection;

import java.sql.SQLException;
import java.util.List;

public class RegisterCommand implements ServerCommand {
    private final DatabaseManager databaseManager;
    private ClientConnection clientConnection = null;

    public RegisterCommand(DatabaseManager databaseManager) {this.databaseManager=databaseManager;}

    public CommandType getType() {
        return CommandType.REGISTER;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        RegisterData data = (RegisterData) request.getArgument();
        try {
            databaseManager.register(data);
        }
        catch (UserAlreadyExistsException e) {
            return List.of(CommandResponse.fail("Данное имя уже занято!"));
        } catch (SQLException e) {
            return List.of(CommandResponse.fail("Проблема с базой данных!"));
        }
        return List.of(CommandResponse.ok("Успешная регистрация!"));
    }
}
