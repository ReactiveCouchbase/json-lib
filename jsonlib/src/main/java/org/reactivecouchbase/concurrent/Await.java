package org.reactivecouchbase.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.util.concurrent.TimeUnit;

public class Await {
    public static <T> T result(Future<T> future, Long timeout, TimeUnit unit) {
        Preconditions.checkNotNull(future);
        try {
            return future.toJucFuture().get(timeout, unit);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
