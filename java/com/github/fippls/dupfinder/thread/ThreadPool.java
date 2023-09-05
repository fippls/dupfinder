package com.github.fippls.dupfinder.thread;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.thread.task.AbstractHashCallable;
import com.github.fippls.dupfinder.util.Log;

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

    private final List<Task> enqueuedTasks = new LinkedList<>();

    static {
        service = Executors.newFixedThreadPool(
                Settings.threadPoolSize, getThreadFactory("WRK"));
    }

    public void addTask(AbstractHashCallable callable) {
        var task = new Task(callable, service.submit(callable));
        enqueuedTasks.add(task);
    }

    public int getNumTasks() {
        return enqueuedTasks.size();
    }

    public long numDone() {
        return enqueuedTasks.stream()
                .map(Task::future)
                .filter(Future::isDone)
                .count();
    }

    public List<FileInfo> fetchResult() {
        var result = enqueuedTasks.stream()
                .map(Task::future)
                .map(this::getWorkResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Remove for next run:
        enqueuedTasks.clear();

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
            Log.error("Interrupted or execution exception: ", e.getMessage());
        }

        return Optional.empty();
    }

    public static void shutDown() {
        service.shutdown();
    }

    @SuppressWarnings("SameParameterValue")
    private static ThreadFactory getThreadFactory(String name) {
        return runnable -> new Thread(name + workerCount.getAndIncrement()) {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    public long totalNumBytesProcessedSinceLastCall() {
        long totalBytesRead = 0;
        var callables = enqueuedTasks.stream()
                .map(Task::callable)
                .collect(Collectors.toList());

        for (AbstractHashCallable callable : callables) {
            totalBytesRead += callable.getAndClearBytesRead();
        }

        return totalBytesRead;
    }

    private static class Task {
        private final AbstractHashCallable callable;
        private final Future<FileInfo> future;

        Task(AbstractHashCallable callable, Future<FileInfo> future) {
            this.callable = callable;
            this.future = future;
        }

        Future<FileInfo> future() {
            return future;
        }

        AbstractHashCallable callable() {
            return callable;
        }
    }
}
