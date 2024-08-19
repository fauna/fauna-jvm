package com.fauna.codec.codecs;

import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;
import com.fauna.types.Module;

import java.io.IOException;

public class ModuleCodec extends BaseCodec<Module> {

    public static final ModuleCodec SINGLETON = new ModuleCodec();

    @Override
    public Module decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case MODULE:
                return parser.getValueAsModule();
            default:
                throw new ClientException(this.unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Module obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            gen.writeModuleValue(obj);
        }
    }

    @Override
    public Class<Module> getCodecClass() {
        return Module.class;
    }
}
