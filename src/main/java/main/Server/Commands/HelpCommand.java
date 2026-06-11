package main.Server.Commands;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;

import java.util.List;

public class HelpCommand implements ServerCommand {
    public CommandType getType() {
        return CommandType.HELP;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        String text = String.join("\n",
                "help : вывести справку по доступным командам",
                "info : вывести информацию о коллекции",
                "show : вывести все элементы коллекции",
                "add {element} : добавить новый элемент в коллекцию",
                "update id {element} : обновить значение элемента коллекции, id которого равен заданному",
                "remove_by_id id : удалить элемент из коллекции по его id",
                "clear : очистить коллекцию",
                "save : сохранить коллекцию в файл",
                "execute_script file_name : считать и исполнить скрипт из указанного файла",
                "exit : завершить программу",
                "remove_last : удалить последний элемент из коллекции",
                "reorder : отсортировать коллекцию в порядке, обратном нынешнему",
                "remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный",
                "average_of_number_of_rooms : вывести среднее значение поля numberOfRooms для всех элементов коллекции",
                "count_greater_than_house house : вывести количество элементов, значение поля house которых больше заданного",
                "filter_by_new isNew : вывести элементы, значение поля isNew которых равно заданному",
                "connect : подключиться к серверу",
                "reconnect : переподключиться к серверу",
                "status : показать адрес, порт и состояние соединения",
                "set_host host : изменить адрес сервера",
                "set_port port : изменить порт сервера",
                "helios : поставить host helios.cs.ifmo.ru");
        return List.of(CommandResponse.ok(text));
    }
}
