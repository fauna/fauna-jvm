package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.types.Module;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;

public class ModuleCodecTest extends TestBase {
    public static final Codec<Module> MODULE_CODEC = DefaultCodecProvider.SINGLETON.get(Module.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, MODULE_CODEC, "{\"@mod\":\"Foo\"}", new Module("Foo"), null),
                Arguments.of(TestType.RoundTrip, MODULE_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "ModuleCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void module_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(MODULE_CODEC);
    }

    @ParameterizedTest(name = "ModuleCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void module_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `ModuleCodec<Module>`. Supported types for codec are [Module, Null].", type);
        runCase(TestType.Decode, MODULE_CODEC, wire, null, new ClientException(exMsg));
    }
}
