package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.*;

public class RegisterCommand implements ClientCommand{

    public String getName() {
        return "register";
    }

    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 3) {
            throw new IllegalArgumentException("Неверный аргумент!");
        }
        String username = (String) (str[1]);
        String password = (String) (str[2]);
        password = Encryption.encryptString(password);
        return new CommandRequest(CommandType.REGISTER, new RegisterData(username, password));
    }
}
