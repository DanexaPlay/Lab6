package main.Server;

import main.BasicClasses.Flat;
import main.Server.Commands.CommandProcessor;
import main.Server.DatabaseManagment.DatabaseManager;
import main.Server.FileManagment.FileManager;
import main.Server.Network.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ServerMain {
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    public static void main(String[] args) {
        String host = readStringArg(args, "--host", "0.0.0.0");
        int port = readIntArg(args, "--port", 8731);
        DatabaseManager databaseManager = new DatabaseManager();

        Vector<Flat> initialCollection;

        if (databaseManager.isConnected()) {
            initialCollection = databaseManager.read_from_database();
        } else {
            logger.warn("База данных недоступна, коллекция будет создана пустой");
            initialCollection = new Vector<>();
        }

        CollectionManager collectionManager = new CollectionManager(initialCollection);
        CommandProcessor commandProcessor = new CommandProcessor(collectionManager, databaseManager);
        Server server = new Server(host, port, commandProcessor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Завершение JVM, сохранение коллекции");
            commandProcessor.save();
            server.shutdown();
        }));

        Thread consoleThread = new Thread(() -> serverConsole(server, commandProcessor), "server-console");
        consoleThread.setDaemon(true);
        consoleThread.start();
        server.start();
    }

    private static void serverConsole(Server server, CommandProcessor commandProcessor) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Runnable> commands = new HashMap<>();
        commands.put("save", () -> {
            commandProcessor.save();
            System.out.println("Коллекция сохранена!");
        });
        commands.put("exit", () -> {
            commandProcessor.save();
            System.out.println("Коллекция сохранена!");
            server.stop();
        });
        commands.put("", () -> { });

        while (server.isRunning()) {
            try {
                if (!scanner.hasNextLine()) {
                    commandProcessor.save();
                    server.stop();
                    break;
                }
                String command = scanner.nextLine().trim();
                Runnable action = commands.get(command);
                if (action == null) {
                    System.out.println("Серверная команда не найдена. Есть: save, exit");
                } else {
                    action.run();
                }
            } catch (NoSuchElementException e) {
                commandProcessor.save();
                server.stop();
                break;
            }
        }
    }

    private static String readStringArg(String[] args, String name, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(name)) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }

    private static int readIntArg(String[] args, String name, int defaultValue) {
        try {
            return Integer.parseInt(readStringArg(args, name, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
