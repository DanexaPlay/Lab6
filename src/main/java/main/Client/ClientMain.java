package main.Client;

import main.Client.Network.NetworkClient;

public class ClientMain {
    public static void main(String[] args) {
        String host = readStringArg(args, "--host", "se.ifmo.ru");
        int port = readIntArg(args, "--port", 8731);

        NetworkClient networkClient = new NetworkClient(host, port);
        ClientApp clientApp = new ClientApp(networkClient);
        clientApp.start();
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
