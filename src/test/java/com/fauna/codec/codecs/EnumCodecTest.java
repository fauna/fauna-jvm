package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.beans.ClassWithClientGeneratedIdCollTsAnnotations;
import com.fauna.beans.ClassWithFaunaIgnore;
import com.fauna.beans.ClassWithIdCollTsAnnotations;
import com.fauna.beans.ClassWithInheritanceL2;
import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.beans.ClassWithRefTagCollision;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.ESCAPED_OBJECT_WIRE_WITH;


public class EnumCodecTest extends TestBase {

    enum TestEnum {
        Foo
    }

    @Test
    public void class_encodeEnum() throws IOException {
        var codec = DefaultCodecProvider.SINGLETON.get(TestEnum.class);
        var wire = "\"Foo\"";
        var obj = TestEnum.Foo;
        runCase(TestType.RoundTrip, codec, wire, obj, null);
    }
}
