package com.fauna.exception;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.response.QueryFailure;

import java.io.IOException;

public class AbortException extends ServiceException {

    private final String abortRaw;
    private Object abort = null;
    private final CodecProvider provider = DefaultCodecProvider.SINGLETON;

    public AbortException(QueryFailure response) {
        super(response);
        abortRaw = response.getAbortRaw().get();
    }

    public Object getAbort() throws IOException {
        return getAbort(Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAbort(Class<T> clazz) throws IOException {
        if (abort != null) return (T) abort;

        if (this.getResponse().getAbortRaw().isPresent()) {
            var codec = provider.get(clazz);
            var parser = new UTF8FaunaParser(abortRaw);
            abort = codec.decode(parser);
            return (T) abort;
        } else {
            throw new RuntimeException("Abort Exception missing abort data.");
        }
    }
}
