package main.Common;

import java.io.Serializable;

public enum CommandType implements Serializable {
    HELP,
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    EXIT,
    REMOVE_LAST,
    REORDER,
    AVERAGE_OF_NUMBER_OF_ROOMS,
    SAVE,
    REMOVE_LOWER,
    COUNT_GREATER_THAN_HOUSE,
    FILTER_BY_NEW,
    EXECUTE_SCRIPT,
    CONNECT,
    RECONNECT,
    STATUS,
    SET_HOST,
    SET_PORT,
    PING,
    LOGIN,
    REGISTER
}
