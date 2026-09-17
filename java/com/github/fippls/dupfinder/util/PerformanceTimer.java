package com.github.fippls.dupfinder.util;

import java.util.concurrent.Callable;

/**
 * Used to calculate how long time it takes to perform certain operations.
 * @author github.com/fippls
 */
public class PerformanceTimer {
    private final long startTime;
    private long endTime;

    public PerformanceTimer() {
        this.startTime = System.nanoTime();
    }

    public void stop() {
        this.endTime = System.nanoTime();
    }

    public double seconds() {
        return (double)nanos() / 1.0E9D;
    }

    public long nanos() {
        return endTime - startTime;
    }

    /**
     * Time a single operation.
     * @param task Task to perform.
     * @return Nanoseconds task took to run.
     * @throws Exception In case something goes wrong.
     */
    public static long time(Runnable task) throws Exception {
        final long start = System.nanoTime();
        task.run();
        return System.nanoTime() - start;
    }
}
