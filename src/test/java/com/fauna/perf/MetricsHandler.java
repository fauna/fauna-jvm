package com.fauna.perf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class MetricsHandler {
    private static final String unique = System.getenv("LOG_UNIQUE") != null ? System.getenv("LOG_UNIQUE") : "";
    private static final String rawStatsFilename = "rawstats_" + unique + ".csv";
    private static final String statsBlockFilename = "stats_" + unique + ".txt";
    private static final Map<String, List<TestTimings>> metricsCollector = new HashMap<>();

    public static void recordMetrics(String queryName, int roundTripMs, int queryTimeMs) {
        int overhead = roundTripMs - queryTimeMs;
        
        metricsCollector.computeIfAbsent(queryName, k -> new ArrayList<>())
                        .add(new TestTimings(roundTripMs, queryTimeMs, overhead));
    }

    public static void writeMetricsToFile() throws IOException {
        List<String> headers = List.of("ts,metric,roundTrip,queryTime,diff,tags");
        Files.write(Paths.get(rawStatsFilename), headers, StandardOpenOption.CREATE);

        List<String> blockHeaders = List.of(
            String.format("%-35s%9s%9s%9s%9s", "TEST", "P50", "P95", "P99", "STDDEV"),
            new String(new char[71]).replace("\0", "-")
        );
        Files.write(Paths.get(statsBlockFilename), blockHeaders, StandardOpenOption.CREATE);

        for (Map.Entry<String, List<TestTimings>> entry : new TreeMap<>(metricsCollector).entrySet()) {
            DescriptiveStatistics stats = new DescriptiveStatistics();

            List<String> lines = entry.getValue().stream()
                .map(testRun -> {
                    stats.addValue(testRun.getOverheadMs());

                    return String.join(",",
                        testRun.getCreatedAt().toString(),
                        entry.getKey(),
                        Integer.toString(testRun.getRoundTripMs()),
                        Integer.toString(testRun.getQueryTimeMs()),
                        Integer.toString(testRun.getOverheadMs()),
                        String.join(";", getMetricsTags())
                    );
            })
                .collect(Collectors.toList());
            Files.write(Paths.get(rawStatsFilename), lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            // *100, round, /100 trick to get two decimal places
            double p50 = Math.round(stats.getPercentile(50) * 100) / 100;
            double p95 = Math.round(stats.getPercentile(95) * 100) / 100;
            double p99 = Math.round(stats.getPercentile(99) * 100) / 100;
            double stddev = Math.round(stats.getStandardDeviation() * 100) / 100;

            var line = String.format("%-35s%9s%9s%9s%9s", entry.getKey(), p50, p95, p99, stddev);
            Files.write(Paths.get(statsBlockFilename), List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    public static String[] getMetricsTags() {
        String env = System.getenv("FAUNA_ENVIRONMENT") != null ? System.getenv("FAUNA_ENVIRONMENT") : "test";
        return new String[] {"env:" + env, "driver_lang:java", "version:0.0.1"};
    }
}
