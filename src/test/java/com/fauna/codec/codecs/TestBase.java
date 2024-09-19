package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaType;
import com.fauna.codec.Helpers;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public abstract class TestBase {

    public enum TestType {
        RoundTrip,
        Decode,
        Encode
    }

    public static Set<String> tags() {
        return BaseCodec.TAGS;
    }

    public static Stream<Arguments> unsupportedTypeCases(Codec codec) {
        return Arrays.stream(FaunaType.values())
                .filter(f -> Arrays.stream(codec.getSupportedTypes()).noneMatch(f::equals))
                .map(f -> Arguments.of(Helpers.getWire(f), f));
    }

    public <T,E extends Exception> void runCase(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        switch (testType) {
            case RoundTrip:
                var decodeRoundTrip = Helpers.decode(codec, wire);
                if (obj instanceof byte[]) {
                    assertArrayEquals((byte[]) obj, (byte[]) decodeRoundTrip);
                } else {
                    assertEquals(obj, decodeRoundTrip);
                }
                var encodeRoundTrip = Helpers.encode(codec, decodeRoundTrip);
                assertEquals(wire, encodeRoundTrip);
                break;
            case Decode:
                if (exception != null) {
                    var ex = assertThrows(exception.getClass(), () -> {
                        Helpers.decode(codec, wire);
                    });

                    assertEquals(exception.getMessage(), ex.getMessage());
                } else {
                    var decoded = Helpers.decode(codec, wire);
                    if (obj instanceof byte[]) {
                        assertArrayEquals((byte[]) obj, (byte[]) decoded);
                    } else {
                        assertEquals(obj, decoded);
                    }
                }
                break;
            case Encode:
                if (exception != null) {
                    var ex = assertThrows(exception.getClass(), () -> {
                        Helpers.encode(codec, (T) obj);
                    });

                    assertEquals(exception.getMessage(), ex.getMessage());
                } else {
                    var encoded = Helpers.encode(codec, (T) obj);
                    assertEquals(wire, encoded);
                }
        }
    }
}
