package main.Server.Commands;

import main.BasicClasses.Flat;
import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;

public class FilterByNewCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public FilterByNewCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.FILTER_BY_NEW;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        List<Flat> flats = collectionManager.filter_by_new((Boolean) request.getArgument());
        if (flats.isEmpty()) {
            return List.of(CommandResponse.ok("Элементы не найдены!"));
        }
        return List.of(CommandResponse.ok("", flats));
    }
}
