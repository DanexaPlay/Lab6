package main.Server.Network;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientConnection {
    private ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
    private ByteBuffer dataBuffer;
    private Queue<ByteBuffer> answers = new ConcurrentLinkedQueue<>();;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ByteBuffer getLengthBuffer() {
        return lengthBuffer;
    }

    public ByteBuffer getDataBuffer() {
        return dataBuffer;
    }

    public void setDataBuffer(ByteBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    public Queue<ByteBuffer> getAnswers() {
        return answers;
    }

    public void clearRead() {
        lengthBuffer.clear();
        dataBuffer = null;
    }
}
