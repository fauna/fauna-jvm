package com.fauna.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;

class FaunaGeneratorTest {

    private FaunaGenerator writer;
    private ByteArrayOutputStream stream;

    @Before
    public void setUp() throws IOException {
        stream = new ByteArrayOutputStream();
        writer = new FaunaGenerator(stream);
    }

    @After
    public void tearDown() throws IOException {
        writer.close();
        stream.close();
    }

}