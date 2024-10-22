package com.fauna.perf.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public class TestDataParser {
    public static Stream<Arguments> getQueriesFromFile() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("./perf-test-setup/queries.json")));
        ObjectMapper mapper = new ObjectMapper();
        TestQueries testQueries = mapper.readValue(content, TestQueries.class);

        return testQueries.getQueries().stream()
                .map(q -> {
                    var response = q.getResponse();
                    var typed = response == null ? false : response.isTyped();
                    var paginate = response == null ? false : response.isPage();
                    return Arguments.of(q.getName(), q.getParts(), typed, paginate);
                });
    }
}
