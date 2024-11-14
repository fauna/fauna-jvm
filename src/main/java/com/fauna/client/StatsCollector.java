package com.fauna.client;

import com.fauna.response.QueryStats;

public interface StatsCollector {

    /**
     * Add the QueryStats to the current counts.
     *
     * @param stats QueryStats object
     */
    void add(QueryStats stats);

    /**
     * Return the collected Stats.
     *
     * @return Stats object
     */
    QueryStatsSummary read();

    /**
     * Return the collected Stats and reset counts.
     *
     * @return Stats object
     */
    QueryStatsSummary readAndReset();

    /**
     * Creates a new instance of a {@code StatsCollector}.
     *
     * @return A {@code StatsCollector} instance.
     */
    StatsCollector createNew();
}
