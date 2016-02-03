package org.reactivecouchbase.common;

public class Throwables {
    public static RuntimeException propagate(Throwable throwable) {
        Invariant.invariant(throwable != null, "Exception can not be null");
        throw new RuntimeException(throwable);
    }
}
