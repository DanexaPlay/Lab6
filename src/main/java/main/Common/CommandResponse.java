package main.Common;

import java.io.Serializable;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;
    private boolean lastPart;

    public CommandResponse(boolean success, String message, Object data, boolean lastPart) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.lastPart = lastPart;
    }

    public static CommandResponse ok(String message) {
        return new CommandResponse(true, message, null, true);
    }

    public static CommandResponse ok(String message, Object data) {
        return new CommandResponse(true, message, data, true);
    }

    public static CommandResponse part(String message, Object data, boolean lastPart) {
        return new CommandResponse(true, message, data, lastPart);
    }

    public static CommandResponse fail(String message) {
        return new CommandResponse(false, message, null, true);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public boolean isLastPart() {
        return lastPart;
    }
}
