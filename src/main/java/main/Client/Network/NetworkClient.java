package main.Client.Network;

import main.Common.CommandRequest;
import main.Common.CommandResponse;
import main.Common.CommandType;
import main.Common.FrameManager;
import main.Common.SerializationManager;

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

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
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
        byte[] data = SerializationManager.serialize(request);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();

        outputBuffer = buffer;
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

            prepareRequest(request);
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

            return readResponses();
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
