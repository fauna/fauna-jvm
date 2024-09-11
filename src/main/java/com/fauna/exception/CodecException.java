package com.fauna.exception;

import java.io.IOException;
import java.util.concurrent.Callable;

public class CodecException extends FaunaException {

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CodecException decodingIOException(IOException exc) {
        return new CodecException("IOException while decoding.", exc);
    }

    public static CodecException encodingIOException(IOException exc) {
        return new CodecException("IOException while encoding.", exc);
    }
}