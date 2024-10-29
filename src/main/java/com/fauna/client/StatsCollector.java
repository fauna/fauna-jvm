package com.fauna.client;

import com.fauna.response.QueryStats;

public interface StatsCollector {

    /**
     * Add the QueryStats to the current counts.
     * @param stats QueryStats object
     */
    void add(QueryStats stats);

    /**
     * Return the collected Stats.
     * @return Stats object
     */
    QueryStatsSummary read();

    /**
     * Return the collected Stats and reset counts.
     * @return Stats object
     */
    QueryStatsSummary readAndReset();

    /**
     * Clone the stats collector instance.
     * @return A clone of the stats collector instance.
     */
    StatsCollector clone();
}
