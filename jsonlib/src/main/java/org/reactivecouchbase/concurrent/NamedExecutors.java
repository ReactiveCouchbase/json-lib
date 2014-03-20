package org.reactivecouchbase.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedExecutors {

    public static ScheduledExecutorService newFixedThreadPool(final int size, final String prefix) {
        return Executors.newScheduledThreadPool(size, new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            }
        });
    }

    public static ExecutorService newCachedThreadPool(final String prefix) {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            }
        });
    }

    public static ScheduledExecutorService newSingleThreadPool(final String prefix) {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            }
        });
    }
}
