package main.Server.DatabaseManagment;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("Пользователь с именем '" + username + "' уже существует");
    }
}
