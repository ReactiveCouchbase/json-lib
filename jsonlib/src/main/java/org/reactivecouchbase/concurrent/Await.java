package org.reactivecouchbase.concurrent;

import com.google.common.base.Throwables;

import java.util.concurrent.TimeUnit;

public class Await {
    public static <T> T result(Future<T> future, Long timeout, TimeUnit unit) {
        try {
            return future.toJucFuture().get(timeout, unit);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
