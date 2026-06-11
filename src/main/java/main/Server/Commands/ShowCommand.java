package main.Server.Commands;

import main.BasicClasses.Flat;
import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Server.CollectionManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShowCommand implements ServerCommand {
    private static final int CHUNK_SIZE = 1000;
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandType getType() {
        return CommandType.SHOW;
    }

    public List<CommandResponse> execute(CommandRequest request) {
        List<Flat> flats = collectionManager.show();
        if (flats.isEmpty()) {
            return List.of(CommandResponse.ok("Коллекция пуста!"));
        }
        int parts = (int) Math.ceil((double) flats.size() / CHUNK_SIZE);
        return IntStream.range(0, parts)
                .mapToObj(i -> {
                    List<Flat> chunk = flats.stream()
                            .skip((long) i * CHUNK_SIZE)
                            .limit(CHUNK_SIZE)
                            .collect(Collectors.toList());
                    return CommandResponse.part("", chunk, i == parts - 1);
                })
                .collect(Collectors.toList());
    }
}
