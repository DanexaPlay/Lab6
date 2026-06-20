package main.Common;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private CommandType type;
    private Object argument;
    private String username;
    private String hash_password;

    public CommandRequest(CommandType type, Object argument) {
        this.type = type;
        this.argument = argument;
        this.username = null;
        this.hash_password = null;
    }

    public CommandRequest(CommandType type, Object argument, String username, String hash_password) {
        this.type = type;
        this.argument = argument;
        this.username = username;
        this.hash_password = hash_password;
    }

    public String getUsername() {
        return username;
    }

    public String getHash_password() {
        return hash_password;
    }

    public boolean hasCredentials() {
        return username != null
                && !username.isBlank()
                && hash_password != null
                && !hash_password.isBlank();
    }

    public CommandRequest withCredentials(String username, String passwordHash) {
        return new CommandRequest(type, argument, username, passwordHash);
    }

    public CommandType getType() {
        return type;
    }

    public Object getArgument() {
        return argument;
    }
}
