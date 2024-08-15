package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.beans.PersonWithAttributes;
import com.fauna.codec.*;
import com.fauna.exception.NullDocumentException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    public void roundtrip_classWithParameterizedFields() throws IOException {
        var wire = "{\"first_name\":\"foo\",\"a_list\":[\"item1\"],\"a_map\":{\"key1\":{\"@int\":\"42\"}}}";
        var codec = cp.get(ClassWithParameterizedFields.class);
        ClassWithParameterizedFields decoded = Helpers.decode(codec, wire);
        var encoded = Helpers.encode(codec, decoded);
        assertEquals(wire, encoded);
    }

    @Test
    public void decode_docAsClass() throws IOException {
        var wire = "{\"@doc\":{\"id\":\"123\",\"coll\":\"Foo\",\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
        var codec = cp.get(PersonWithAttributes.class);
        PersonWithAttributes decoded = Helpers.decode(codec, wire);
        assertEquals("foo", decoded.getFirstName());
        assertEquals("bar", decoded.getLastName());
    }

    @Test
    public void decode_docAsClassThrowsIfNotExists() throws IOException {
        var wire = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
        var codec = cp.get(PersonWithAttributes.class);
        var ex = assertThrows(NullDocumentException.class, () -> Helpers.decode(codec, wire));
        assertEquals("Document 123 in collection Foo is null: not found", ex.getMessage());
    }
}
