package com.github.fippls.dupfinder.util;

/**
 * Used to calculate how long calculations take.
 * @author github.com/fippls
 */
public class PerformanceTimer {
    private final long startTime;
    private long endTime;
    private long nanos;

    public PerformanceTimer() {
        this.startTime = System.nanoTime();
    }

    public void stop() {
        this.endTime = System.nanoTime();
    }

    public long millis() {
        return (endTime - startTime) / 1_000_000L;
    }

    public double seconds() {
        return (double) (endTime - startTime)/ 1.0E9D;
    }
}