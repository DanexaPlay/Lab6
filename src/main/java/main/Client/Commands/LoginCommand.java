package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.*;

public class LoginCommand implements ClientCommand{

    public CommandRequest build(String[] str, ConsoleReader reader) {
        if (str.length != 3) {
            throw new IllegalArgumentException("Неверный аргумент!");
        }
        String username = (String) (str[1]);
        String password = (String) (str[2]);
        password = Encryption.encryptString(password);
        return new CommandRequest(CommandType.LOGIN, new LoginData(username, password));
    }

    public String getName() {
        return "login";
    }
}
