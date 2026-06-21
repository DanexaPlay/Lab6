package main.Client.Network;

import main.Common.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworkClient {
    private String host;
    private int port;
    private SocketChannel channel;
    private Selector selector;
    private boolean connected = false;
    private ByteBuffer outputBuffer;
    private String username;
    private String passwordHash;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setCredentials(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public void clearCredentials() {
        this.username = null;
        this.passwordHash = null;
    }

    public boolean isAuthorized() {
        return username != null
                && !username.isBlank()
                && passwordHash!= null
                && !passwordHash.isBlank();
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    private boolean isPublicCommand(CommandType type) {
        return type == CommandType.LOGIN
                || type == CommandType.REGISTER
                || type == CommandType.PING;
    }

    private boolean hasCredentials() {
        return username != null
                && !username.isBlank()
                && passwordHash != null
                && !passwordHash.isBlank();
    }

    private CommandRequest attachCredentials(CommandRequest request) {
        if (request == null) {
            return null;
        }

        if (isPublicCommand(request.getType())) {
            return request;
        }

        if (!hasCredentials()) {
            return request;
        }

        if (request.getType() == CommandType.EXECUTE_SCRIPT) {
            return attachCredentialsToScript(request);
        }

        return request.withCredentials(username, passwordHash);
    }

    private CommandRequest attachCredentialsToScript(CommandRequest request) {
        Object argument = request.getArgument();

        if (!(argument instanceof List<?> list)) {
            return request.withCredentials(username, passwordHash);
        }

        List<CommandRequest> updatedRequests = new ArrayList<>();

        for (Object item : list) {
            if (item instanceof CommandRequest commandRequest) {
                updatedRequests.add(attachCredentials(commandRequest));
            }
        }

        return new CommandRequest(
                request.getType(),
                updatedRequests,
                username,
                passwordHash
        );
    }

    private void saveCredentialsIfAuthSuccess(
            CommandRequest request,
            List<CommandResponse> responses
    ) {
        if (request == null || responses == null || responses.isEmpty()) {
            return;
        }

        if (request.getType() != CommandType.LOGIN
                && request.getType() != CommandType.REGISTER) {
            return;
        }

        boolean success = responses.stream().anyMatch(CommandResponse::isSuccess);

        if (!success) {
            return;
        }

        Object argument = request.getArgument();

        if (argument instanceof LoginData loginData) {
            setCredentials(loginData.getUsername(), loginData.getPassword());
            return;
        }

        if (argument instanceof RegisterData registerData) {
            setCredentials(registerData.getUsername(), registerData.getPassword());
        }
    }

    public synchronized boolean connect() {
        close();
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            channel.register(selector, SelectionKey.OP_CONNECT);

            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 700) {
                selector.select(100);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isConnectable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        if (socketChannel.finishConnect()) {
                            key.interestOps(SelectionKey.OP_READ);
                            connected = true;
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            connected = false;
        }
        close();
        return false;
    }

    private void prepareRequest(CommandRequest request) throws IOException {
        CommandRequest preparedRequest = addCredentialsIfNeeded(request);
        byte[] data = SerializationManager.serialize(preparedRequest);
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        outputBuffer = buffer;
    }

    private CommandRequest addCredentialsIfNeeded(CommandRequest request) {
        if (request == null || request.getType() == null) {
            return request;
        }

        CommandType type = request.getType();

        if (type == CommandType.LOGIN
                || type == CommandType.REGISTER
                || type == CommandType.PING) {
            return request;
        }

        if (!isAuthorized()) {
            return request;
        }

        return request.withCredentials(username, passwordHash);
    }

    private boolean writeRequest(SocketChannel channel) throws IOException {
        if (outputBuffer == null) {
            return true;
        }

        channel.write(outputBuffer);

        if (!outputBuffer.hasRemaining()) {
            outputBuffer = null;
            return true;
        }

        return false;
    }

    public synchronized List<CommandResponse> send(CommandRequest request) {
        if (!connected) {
            if (!connect()) {
                return List.of();
            }
        }

        try {
            SelectionKey key = channel.keyFor(selector);

            if (key == null || !key.isValid()) {
                close();
                return List.of();
            }

            CommandRequest requestToSend = attachCredentials(request);
            prepareRequest(requestToSend);
            key.interestOps(SelectionKey.OP_WRITE);

            long lastAction = System.currentTimeMillis();

            while (outputBuffer != null) {
                selector.select(2000);

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectedKey = iterator.next();
                    iterator.remove();

                    if (!selectedKey.isValid()) {
                        continue;
                    }

                    if (selectedKey.isWritable()) {
                        boolean sent = writeRequest((SocketChannel) selectedKey.channel());

                        if (sent) {
                            selectedKey.interestOps(SelectionKey.OP_READ);
                        } else {
                            selectedKey.interestOps(SelectionKey.OP_WRITE);
                        }

                        lastAction = System.currentTimeMillis();
                    }
                }

                if (System.currentTimeMillis() - lastAction > 5000) {
                    close();
                    return List.of();
                }
            }
            List<CommandResponse> responses = readResponses();

            saveCredentialsIfAuthSuccess(request, responses);

            return responses;
        } catch (IOException e) {
            close();
            return List.of();
        }
    }


    public synchronized boolean ping() {
        List<CommandResponse> responses = send(new CommandRequest(CommandType.PING, null));
        return !responses.isEmpty() && responses.get(0).isSuccess();
    }

    private List<CommandResponse> readResponses() {
        List<CommandResponse> responses = new ArrayList<>();
        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer dataBuffer = null;
        long lastAction = System.currentTimeMillis();

        try {
            while (true) {
                selector.select(200);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isReadable()) {
                        continue;
                    }

                    if (dataBuffer == null) {
                        int read = channel.read(lengthBuffer);
                        if (read == -1) {
                            close();
                            return List.of();
                        }
                        if (read > 0) {
                            lastAction = System.currentTimeMillis();
                        }
                        if (lengthBuffer.hasRemaining()) {
                            continue;
                        }
                        lengthBuffer.flip();
                        int length = lengthBuffer.getInt();
                        if (length <= 0 || length > 100_000_000) {
                            close();
                            return List.of();
                        }
                        dataBuffer = ByteBuffer.allocate(length);
                    }

                    int read = channel.read(dataBuffer);
                    if (read == -1) {
                        close();
                        return List.of();
                    }
                    if (read > 0) {
                        lastAction = System.currentTimeMillis();
                    }
                    if (dataBuffer.hasRemaining()) {
                        continue;
                    }

                    dataBuffer.flip();
                    byte[] data = new byte[dataBuffer.remaining()];
                    dataBuffer.get(data);
                    Object object = SerializationManager.deserialize(data);
                    if (object instanceof CommandResponse) {
                        CommandResponse response = (CommandResponse) object;
                        responses.add(response);
                        if (response.isLastPart()) {
                            return responses;
                        }
                    }
                    lengthBuffer.clear();
                    dataBuffer = null;
                }

                if (System.currentTimeMillis() - lastAction > 5000) {
                    close();
                    return responses;
                }
            }
        } catch (Exception e) {
            close();
            return List.of();
        }
    }

    public synchronized void close() {
        connected = false;
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ignored) {
        }
    }

    public synchronized boolean isConnected() {
        return connected && channel != null && channel.isOpen();
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized void setHost(String host) {
        this.host = host;
        close();
    }

    public synchronized void setPort(int port) {
        this.port = port;
        close();
    }

    public synchronized void setAddress(String host, int port) {
        this.host = host;
        this.port = port;
        close();
    }
}
