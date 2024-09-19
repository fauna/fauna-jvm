package com.fauna.response;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Joins a list of byte buffers to make them appear as a single input stream.
 * <p>
 * The reset method is supported, and always resets to restart the stream at the beginning of the first buffer,
 * although markSupported() returns false for this class.
 */
public class MultiByteBufferInputStream extends InputStream {

    private final List<ByteBuffer> buffers;
    private int index = 0;
    private ByteBuffer currentBuffer;


    public MultiByteBufferInputStream(List<ByteBuffer> initialBuffers) {
        this.buffers = initialBuffers;
        this.currentBuffer = buffers.get(index);
    }

    public synchronized void add(List<ByteBuffer> additionalBuffers) {
        buffers.addAll(additionalBuffers);
    }

    @Override
    public synchronized int read() throws IOException {
        if (currentBuffer.hasRemaining()) {
            return currentBuffer.get() & 0xFF;
        } else if (buffers.size() > index+1) {
            index++;
            currentBuffer = buffers.get(index);
            return currentBuffer.get() & 0xFF;
        } else {
            throw new EOFException("End of Stream");
        }
    }

    @Override
    public synchronized void reset() {
        for (ByteBuffer buffer : buffers.subList(0, index)) {
            buffer.position(0);
        }
        index = 0;
        currentBuffer = buffers.get(index);
    }



}
