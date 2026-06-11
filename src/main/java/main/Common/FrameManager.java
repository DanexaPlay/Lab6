package main.Common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class FrameManager {
    public static ByteBuffer toBuffer(Serializable object) throws IOException {
        byte[] objectBytes = SerializationManager.serialize(object);
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + objectBytes.length);
        buffer.putInt(objectBytes.length);
        buffer.put(objectBytes);
        buffer.flip();
        return buffer;
    }
}
