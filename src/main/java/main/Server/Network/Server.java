package main.Server.Network;

import main.Common.*;
import main.Server.Commands.CommandProcessor;
import main.Server.Commands.LoginCommand;
import main.Server.Commands.RegisterCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private final String host;
    private final int port;
    private final CommandProcessor commandProcessor;
    private boolean running = true;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public Server(String host, int port, CommandProcessor commandProcessor) {
        this.host = host;
        this.port = port;
        this.commandProcessor = commandProcessor;
    }

    public void start() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Сервер запущен: " + host + ":" + port);
            logger.info("Сервер запущен: {}:{}", host, port);

            while (running) {
                selector.select(500);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptClient(key);
                    }
                    if (key.isReadable()) {
                        readRequest(key);
                    }
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isWritable()) {
                        writeAnswer(key);
                    }
                }
            }
        } catch (BindException e) {
            logger.error("Порт занят: {}", port, e);
            System.out.println("Порт " + port + " уже занят. Укажите другой порт через --port.");
        } catch (IOException e) {
            logger.error("Ошибка сервера", e);
            System.out.println("Ошибка сервера. Сервер остановлен.");
        } finally {
            close();
        }
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }
        clientChannel.configureBlocking(false);
        SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
        clientKey.attach(new ClientConnection());
        System.out.println("Клиент подключился: " + clientChannel.getRemoteAddress());
        logger.info("Клиент подключился: {}", clientChannel.getRemoteAddress());
    }

    private void readRequest(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientConnection connection = (ClientConnection) key.attachment();

        try {
            if (connection.getDataBuffer() == null) {
                int read = channel.read(connection.getLengthBuffer());
                if (read == -1) {
                    disconnect(key);
                    return;
                }
                if (connection.getLengthBuffer().hasRemaining()) {
                    return;
                }
                connection.getLengthBuffer().flip();
                int length = connection.getLengthBuffer().getInt();
                if (length <= 0 || length > 100_000_000) {
                    disconnect(key);
                    return;
                }
                connection.setDataBuffer(ByteBuffer.allocate(length));
            }

            int read = channel.read(connection.getDataBuffer());
            if (read == -1) {
                disconnect(key);
                return;
            }
            if (connection.getDataBuffer().hasRemaining()) {
                return;
            }

            connection.getDataBuffer().flip();
            byte[] data = new byte[connection.getDataBuffer().remaining()];
            connection.getDataBuffer().get(data);
            connection.clearRead();

            Object object = SerializationManager.deserialize(data);
            if (!(object instanceof CommandRequest)) {
                connection.getAnswers().add(FrameManager.toBuffer(CommandResponse.fail("Неверный запрос!")));
            } else {
                if (((CommandRequest) object).hasCredentials() || ((CommandRequest) object).getType() == CommandType.LOGIN|| ((CommandRequest) object).getType() == CommandType.REGISTER || ((CommandRequest) object).getType() == CommandType.PING) {
                    List<CommandResponse> responses = commandProcessor.process((CommandRequest) object);
                    for (CommandResponse response : responses) {
                        connection.getAnswers().add(FrameManager.toBuffer(response));
                    }
                }
                else {
                    connection.getAnswers().add(FrameManager.toBuffer(CommandResponse.fail("Вы не авторизованы! Используйте login или register")));
                    }
                }
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (Exception e) {
            logger.error("Ошибка обработки запроса", e);
            try {
                connection.getAnswers().add(FrameManager.toBuffer(CommandResponse.fail("Не получилось обработать запрос.")));
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } catch (Exception ignored) {
                disconnect(key);
            }
        }
    }

    private void writeAnswer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientConnection connection = (ClientConnection) key.attachment();

        try {
            while (!connection.getAnswers().isEmpty()) {
                ByteBuffer buffer = connection.getAnswers().peek();
                channel.write(buffer);
                if (buffer.hasRemaining()) {
                    return;
                }
                connection.getAnswers().poll();
            }
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            disconnect(key);
        }
    }

    private void disconnect(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException ignored) {
        }
        key.cancel();
        System.out.println("Клиент отключился.");
        logger.info("Клиент отключился");
    }

    public void stop() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void close() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ignored) {
        }
    }
}
