package com.fauna.exception;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.response.QueryFailure;

import java.io.IOException;

public class AbortException extends ServiceException {

    private Object abort = null;
    private final CodecProvider provider = DefaultCodecProvider.SINGLETON;

    public AbortException(QueryFailure response) {
        super(response);
    }

    public Object getAbort() throws IOException {
        return getAbort(Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAbort(Class<T> clazz) throws IOException {
        if (abort != null) return (T) abort;

        var abStr = this.getResponse().getAbortString();
        if (abStr.isPresent()) {
            var codec = provider.get(clazz);
            var parser = new UTF8FaunaParser(abStr.get());
            abort = codec.decode(parser);
            return (T) abort;
        } else {
            return null;
        }
    }
}
