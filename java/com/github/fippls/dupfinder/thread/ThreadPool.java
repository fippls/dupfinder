package com.github.fippls.dupfinder.thread;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread pool for hash calculations.
 * @author github.com/fippls
 */
public class ThreadPool {
    private static final AtomicInteger workerCount = new AtomicInteger(0);
    private static final ExecutorService service;

    private final List<Future<FileInfo>> futures = new LinkedList<>();
    private int numTasks = 0;

    static {
        service = Executors.newFixedThreadPool(
                Settings.threadPoolSize, getThreadFactory("WRK"));
    }

    public void addTask(Callable<FileInfo> task) {
        futures.add(service.submit(task));
        numTasks++;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public long numDone() {
        return futures.stream()
                .filter(Future::isDone)
                .count();
    }

    public List<FileInfo> fetchResult() {
        var result = futures.stream()
                .map(this::getWorkResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Remove for next run:
        futures.clear();
        numTasks = 0;

        return result;
    }

    private Optional<FileInfo> getWorkResult(Future<FileInfo> future) {
        try {
            return Optional.of(future.get());
        }
        catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof OutOfMemoryError) {
                long currentMemory = Runtime.getRuntime().totalMemory();
                System.err.println("Severe error: " + e.getMessage());
                System.err.println("Out of memory (used: " + (currentMemory / 1_000_000) +
                        " MiB), try increasing memory amount with the -Xmx VM option. Shutting down.");
                System.exit(-5);
            }

            // TODO: Check if the file was deleted, NoSuchFileException
            System.out.println("Interrupted or execution exception: " + e.getMessage());
        }

        return Optional.empty();
    }

    public static void shutDown() {
        service.shutdown();
    }

    private static ThreadFactory getThreadFactory(String name) {
        return runnable -> new Thread(name + workerCount.getAndIncrement()) {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
