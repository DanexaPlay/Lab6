package main.Server.DatabaseManagment;

public class UserNotExistsException extends RuntimeException {
    public UserNotExistsException(String username) {
        super("Пользователь с именем '" + username + "' не существует");;
    }
}
