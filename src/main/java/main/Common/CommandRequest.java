package main.Common;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private CommandType type;
    private Object argument;

    public CommandRequest(CommandType type, Object argument) {
        this.type = type;
        this.argument = argument;
    }

    public CommandType getType() {
        return type;
    }

    public Object getArgument() {
        return argument;
    }
}
