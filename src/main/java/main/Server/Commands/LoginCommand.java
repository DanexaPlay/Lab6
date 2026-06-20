package main.Server.Commands;

import main.Common.*;
import main.Server.DatabaseManagment.DatabaseManager;
import main.Server.DatabaseManagment.InvalidCredentialsException;
import main.Server.DatabaseManagment.UserNotExistsException;
import main.Server.Network.ClientConnection;

import java.sql.SQLException;
import java.util.List;

public class LoginCommand implements ServerCommand {
    private final DatabaseManager databaseManager;

    public LoginCommand(DatabaseManager databaseManager) {this.databaseManager=databaseManager;}

    public CommandType getType() {
        return CommandType.LOGIN;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        if (!databaseManager.isConnected()) {
            return List.of(CommandResponse.fail("База данных недоступна"));
        }
        LoginData data = (LoginData) request.getArgument();
        try {
            databaseManager.login(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of(CommandResponse.fail("Проблема с базой данных!"));
        }
        catch (UserNotExistsException e) {
            return List.of(CommandResponse.fail("Пользователя не существует!"));
        }
        catch (InvalidCredentialsException e) {
            return List.of(CommandResponse.fail("Неверные логин или пароль!"));
        }
        return List.of(CommandResponse.ok("Успешная авторизация!"));
    }

}
