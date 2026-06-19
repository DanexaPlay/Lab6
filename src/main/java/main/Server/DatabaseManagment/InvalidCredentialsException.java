package main.Server.DatabaseManagment;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Неверный логин или пароль!");
    }
}
