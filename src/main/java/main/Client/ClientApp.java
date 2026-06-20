package main.Client;

import main.BasicClasses.Flat;
import main.Client.Console.ConsoleReader;
import main.Client.Console.InputCancelledException;
import main.Client.Commands.ClientCommandManager;
import main.Client.Network.NetworkClient;
import main.Common.CommandRequest;
import main.Common.CommandResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientApp {
    private static final Logger logger = LogManager.getLogger(ClientApp.class);
    private final NetworkClient networkClient;
    private final ClientCommandManager commandManager;
    private Scanner input = new Scanner(System.in);
    private volatile boolean running = true;
    private boolean scriptMode = false;
    private volatile boolean connectionStatus = false;
    private final Set<String> openedScripts = new HashSet<>();
    private final Map<String, Consumer<String[]>> localCommands = new HashMap<>();

    public ClientApp(NetworkClient networkClient) {
        this.networkClient = networkClient;
        this.commandManager = new ClientCommandManager(networkClient);
        fillLocalCommands();
    }

    private void fillLocalCommands() {
        localCommands.put("help", str -> printLocalHelp());
        localCommands.put("connect", this::connect);
        localCommands.put("reconnect", this::connect);
        localCommands.put("status", str -> status());
        localCommands.put("set_host", this::setHost);
        localCommands.put("set_port", this::setPort);
        localCommands.put("helios", str -> {
            connectionStatus = false;
            setHelios(str);
        });
        localCommands.put("exit", str -> {
            running = false;
            networkClient.close();
        });
        localCommands.put("save", str -> System.out.println("Команда save доступна только на сервере!"));
        localCommands.put("execute_script", this::executeScript);
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(networkClient::close));
        System.out.println("Клиент запущен. host=" + networkClient.getHost() + " port=" + networkClient.getPort());
        startConnectionThread();

        while (running) {
            try {
                if (!scriptMode) {
                    System.out.println("Пожалуйста, введите команду ");
                }
                if (!input.hasNextLine()) {
                    networkClient.close();
                    break;
                }
                String text = normalize(input.nextLine());
                if (scriptMode) {
                    System.out.println(text);
                }
                if (text.isEmpty()) {
                    continue;
                }
                handle(text);
            } catch (NoSuchElementException e) {
                networkClient.close();
                break;
            } catch (Exception e) {
                System.out.println("Команда не выполнена. Проверьте ввод.");
            }
        }
    }


    private void startConnectionThread() {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    if (networkClient.isConnected()) {
                        if (!networkClient.ping()) {
                            if (connectionStatus) {
                                System.out.println("Сервер недоступен.");
                            }
                            connectionStatus = false;
                        } else {
                            connectionStatus = true;
                        }
                    } else {
                        if (networkClient.connect()) {
                            if (!connectionStatus) {
                                System.out.println("Соединение установлено.");
                            }
                            connectionStatus = true;
                        }
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    if (connectionStatus) {
                        System.out.println("Сервер недоступен.");
                    }
                    connectionStatus = false;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String normalize(String text) {
        return text.trim().replaceAll("[\\s]{2,}", " ");
    }

    private void handle(String text) {
        String[] str = text.split(" ");
        Consumer<String[]> localCommand = localCommands.get(str[0]);
        if (localCommand != null) {
            localCommand.accept(str);
            return;
        }
        sendCommand(text, str);
    }

    private void sendCommand(String text, String[] str) {
        CommandRequest request = buildRequest(text, str);
        if (request == null) {
            return;
        }

        List<CommandResponse> responses = networkClient.send(request);
        if (responses.isEmpty()) {
            System.out.println("Сервер недоступен.");
            connectionStatus = false;
            return;
        }
        connectionStatus = true;
        responses.forEach(this::printResponse);
    }

    private CommandRequest buildRequest(String text, String[] str) {
        ConsoleReader reader = new ConsoleReader(input, this::checkServerBeforeInput);
        try {
            return commandManager.build(str, reader);
        } catch (NumberFormatException e) {
            System.out.println("Неверный аргумент!");
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (InputCancelledException e) {
            System.out.println(e.getMessage());
            connectionStatus = false;
            return null;
        } catch (NoSuchElementException e) {
            System.out.println("Ввод завершён.");
            running = false;
            return null;
        }
    }

    private boolean checkServerBeforeInput() {
        if (!networkClient.isConnected()) {
            return networkClient.connect();
        }
        return networkClient.ping();
    }

    private void printResponse(CommandResponse response) {
        if (response.getMessage() != null && !response.getMessage().isBlank()) {
            System.out.println(response.getMessage());
        }
        Object data = response.getData();
        if (data instanceof List<?>) {
            List<?> list = (List<?>) data;
            list.stream().forEach(item -> System.out.println(item.toString() + "\n"));
        } else if (data instanceof Flat || data != null) {
            System.out.println(data);
        }
    }

    private void connect(String[] str) {
        if (str.length == 2) {
            if (isPort(str[1])) {
                int port = Integer.parseInt(str[1]);
                if (!checkPort(port)) {
                    System.out.println("Неверный порт!");
                    return;
                }
                networkClient.setPort(port);
            } else {
                networkClient.setHost(str[1]);
            }
            connectionStatus = false;
        } else if (str.length == 3) {
            if (!isPort(str[2])) {
                System.out.println("Неверный порт!");
                return;
            }
            int port = Integer.parseInt(str[2]);
            if (!checkPort(port)) {
                System.out.println("Неверный порт!");
                return;
            }
            networkClient.setAddress(str[1], port);
            connectionStatus = false;
        } else if (str.length > 3) {
            System.out.println("Использование: connect host port");
            return;
        }

        if (networkClient.connect()) {
            connectionStatus = true;
            System.out.println("Соединение установлено.");
        } else {
            connectionStatus = false;
            System.out.println("Соединения нет.");
        }
    }

    private void status() {
        System.out.println("host=" + networkClient.getHost() + " port=" + networkClient.getPort());
        System.out.println(networkClient.isConnected() ? "Соединение есть." : "Соединения нет.");
    }

    private void setHost(String[] str) {
        if (str.length != 2) {
            System.out.println("Использование: set_host localhost");
            return;
        }
        connectionStatus = false;
        networkClient.setHost(str[1]);
        System.out.println("host изменён на " + str[1]);
    }

    private void setPort(String[] str) {
        if (str.length != 2) {
            System.out.println("Использование: set_port 2222");
            return;
        }
        try {
            int port = Integer.parseInt(str[1]);
            if (!checkPort(port)) {
                System.out.println("Неверный порт!");
                return;
            }
            connectionStatus = false;
            networkClient.setPort(port);
            System.out.println("port изменён на " + port);
        } catch (NumberFormatException e) {
            System.out.println("Неверный порт!");
        }
    }


    private void setHelios(String[] str) {
        connectionStatus = false;
        if (str.length == 1) {
            networkClient.setHost("se.ifmo.ru");
            System.out.println("host изменён на se.ifmo.ru");
            return;
        }
        if (str.length == 2 && isPort(str[1])) {
            int port = Integer.parseInt(str[1]);
            if (!checkPort(port)) {
                System.out.println("Неверный порт!");
                return;
            }
            networkClient.setAddress("se.ifmo.ru", port);
            System.out.println("host изменён на se.ifmo.ru");
            System.out.println("port изменён на " + port);
            return;
        }
        System.out.println("Использование: helios 8731");
    }

    private boolean isPort(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkPort(int port) {
        return port > 0 && port <= 65535;
    }

    private void executeScript(String[] str) {
        if (str.length != 2) {
            System.out.println("Не задано название файла!");
            return;
        }

        if (!checkServerBeforeInput()) {
            System.out.println("Сервер недоступен.");
            connectionStatus = false;
            return;
        }

        String fileName = str[1];
        if (openedScripts.contains(fileName)) {
            System.out.println("Рекурсивный вызов скрипта запрещён!");
            return;
        }

        Scanner oldInput = input;
        boolean oldScriptMode = scriptMode;
        List<CommandRequest> requests = new ArrayList<>();
        try {
            input = new Scanner(new File(fileName));
            scriptMode = true;
            openedScripts.add(fileName);
            ConsoleReader reader = new ConsoleReader(input, this::checkServerBeforeInput);

            while (running && input.hasNextLine()) {
                String text = normalize(input.nextLine());
                System.out.println(text);
                if (text.isEmpty()) {
                    continue;
                }

                String[] line = text.split(" ");
                if (line[0].equals("execute_script")) {
                    System.out.println("Рекурсивный вызов скрипта запрещён!");
                    continue;
                }
                if (line[0].equals("exit")) {
                    break;
                }
                if (line[0].equals("help")) {
                    requests.add(new CommandRequest(main.Common.CommandType.HELP, null));
                    continue;
                }
                if (localCommands.containsKey(line[0])) {
                    System.out.println("Команда " + line[0] + " не выполняется из скрипта.");
                    continue;
                }

                try {
                    requests.add(commandManager.build(line, reader));
                } catch (NumberFormatException e) {
                    System.out.println("Неверный аргумент!");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден!");
            return;
        } catch (InputCancelledException e) {
            System.out.println(e.getMessage());
            connectionStatus = false;
            return;
        } finally {
            openedScripts.remove(fileName);
            input = oldInput;
            scriptMode = oldScriptMode;
        }

        if (requests.isEmpty()) {
            return;
        }

        logger.info("Отправка скрипта на сервер, команд: {}", requests.size());
        List<CommandResponse> responses = networkClient.send(new CommandRequest(main.Common.CommandType.EXECUTE_SCRIPT, requests));
        if (responses.isEmpty()) {
            System.out.println("Сервер недоступен.");
            connectionStatus = false;
            return;
        }
        connectionStatus = true;
        responses.forEach(this::printResponse);
    }

    private void printLocalHelp() {
        System.out.println("help : вывести справку по доступным командам");
        System.out.println("info : вывести информацию о коллекции");
        System.out.println("show : вывести все элементы коллекции");
        System.out.println("add {element} : добавить новый элемент в коллекцию");
        System.out.println("update id {element} : обновить значение элемента коллекции, id которого равен заданному");
        System.out.println("remove_by_id id : удалить элемент из коллекции по его id");
        System.out.println("clear : очистить коллекцию");
        System.out.println("save : сохранить коллекцию в файл");
        System.out.println("execute_script file_name : считать и исполнить скрипт из указанного файла");
        System.out.println("exit : завершить программу");
        System.out.println("remove_last : удалить последний элемент из коллекции");
        System.out.println("reorder : отсортировать коллекцию в порядке, обратном нынешнему");
        System.out.println("remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный");
        System.out.println("average_of_number_of_rooms : вывести среднее значение поля numberOfRooms для всех элементов коллекции");
        System.out.println("count_greater_than_house house : вывести количество элементов, значение поля house которых больше заданного");
        System.out.println("filter_by_new isNew : вывести элементы, значение поля isNew которых равно заданному");
        System.out.println("connect : подключиться к серверу");
        System.out.println("connect host port : изменить адрес с портом и подключиться");
        System.out.println("reconnect : переподключиться к серверу");
        System.out.println("status : показать адрес, порт и состояние соединения");
        System.out.println("set_host host : изменить адрес сервера");
        System.out.println("set_port port : изменить порт сервера");
        System.out.println("helios : поставить host se.ifmo.ru");
        System.out.println("helios port : поставить host se.ifmo.ru и указанный порт");
        System.out.println("login username password : авторизоваться");
        System.out.println("register username password : зарегистрироваться");
    }
}
