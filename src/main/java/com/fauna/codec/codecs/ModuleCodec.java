package com.fauna.codec.codecs;

import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.types.Module;

public class ModuleCodec extends BaseCodec<Module> {

    public static final ModuleCodec SINGLETON = new ModuleCodec();

    @Override
    public Module decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case MODULE:
                return parser.getValueAsModule();
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Module obj) throws CodecException {
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

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Module, FaunaType.Null};
    }
}
