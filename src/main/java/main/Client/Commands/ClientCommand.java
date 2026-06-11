package main.Client.Commands;

import main.Client.Console.ConsoleReader;
import main.Common.CommandRequest;

public interface ClientCommand {
    String getName();
    CommandRequest build(String[] str, ConsoleReader reader);
}
