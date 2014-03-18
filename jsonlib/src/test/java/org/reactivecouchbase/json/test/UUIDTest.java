package org.reactivecouchbase.json.test;

import org.junit.Test;
import org.reactivecouchbase.common.UUID;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class UUIDTest {

    @Test
    public void testUUID() throws Exception {
        int jobs = 100;
        ExecutorService ec = Executors.newFixedThreadPool(2);
        final AtomicLong generated = new AtomicLong(0);
        final AtomicLong collision = new AtomicLong(0);
        final Set<String> set = Collections.synchronizedSet(new HashSet<String>());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    String uuid = UUID.generate();
                    generated.incrementAndGet();
                    if (set.contains(uuid)) {
                        collision.incrementAndGet();
                    } else {
                        set.add(uuid);
                    }
                }
            }
        };
        List<Future<?>> futures = new ArrayList<Future<?>>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < jobs; i++) {
           futures.add(ec.submit(runnable));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        System.out.println("Total generated : " + generated.get() + " / collision : " + collision.get() + " in " + (System.currentTimeMillis() - start) + " ms.");
    }
}
