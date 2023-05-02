package com.github.fippls.dupfinder.util;

/**
 * Millisecond-based interval timer.
 * @author github.com/fippls
 */
public class IntervalTimer {
    private final long timeToWait;
    private long endTime;

    /**
     * Creates a new timer and starts it immediately.
     * @param timeToWait Number of milliseconds that we wait to wait.
     */
    public IntervalTimer(long timeToWait) {
        this.timeToWait = timeToWait;
        this.endTime = System.currentTimeMillis() + timeToWait;
    }

    /**
     * Checks if enough time has passed.
     */
    public boolean done() {
        return System.currentTimeMillis() >= endTime;
    }

    /**
     * Start over from here and wait the same time again.
     */
    public void reset() {
        endTime = System.currentTimeMillis() + timeToWait;
    }
}
