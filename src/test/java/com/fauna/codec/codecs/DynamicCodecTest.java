package com.fauna.codec.codecs;

import com.fauna.codec.*;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.*;
import com.fauna.types.Module;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    public void decode_doc() throws IOException {
        var wire = "{\"@doc\":{\"id\":\"123\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
        var codec = cp.get(Object.class);
        Document decoded = (Document) Helpers.decode(codec, wire);
        assertEquals("123", decoded.getId());
        assertEquals(new Module("Foo"), decoded.getCollection());
        assertEquals("foo", decoded.get("first_name"));
        assertEquals("bar", decoded.get("last_name"));
    }

    @Test
    public void decode_docRef() throws IOException {
        var wire = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";
        var codec = cp.get(Object.class);
        DocumentRef decoded = (DocumentRef) Helpers.decode(codec, wire);
        assertEquals("123", decoded.getId());
        assertEquals(new Module("Foo"), decoded.getCollection());
    }

    @Test
    public void decode_namedDoc() throws IOException {
        var wire = "{\"@doc\":{\"name\":\"Boogles\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
        var codec = cp.get(Object.class);
        NamedDocument decoded = (NamedDocument) Helpers.decode(codec, wire);
        assertEquals("Boogles", decoded.getName());
        assertEquals(new Module("Foo"), decoded.getCollection());
        assertEquals("foo", decoded.get("first_name"));
        assertEquals("bar", decoded.get("last_name"));
    }

    @Test
    public void decode_namedDocRef() throws IOException {
        var wire = "{\"@ref\":{\"name\":\"Boogles\",\"coll\":{\"@mod\":\"Foo\"}}}";
        var codec = cp.get(Object.class);
        NamedDocumentRef decoded = (NamedDocumentRef) Helpers.decode(codec, wire);
        assertEquals("Boogles", decoded.getName());
        assertEquals(new Module("Foo"), decoded.getCollection());
    }

    @Test
    public void decode_docThrowsIfNotExists() throws IOException {
        var wire = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
        var codec = cp.get(Object.class);
        var ex = assertThrows(NullDocumentException.class, () -> Helpers.decode(codec, wire));
        assertEquals("Document 123 in collection Foo is null: not found", ex.getMessage());
    }
}
