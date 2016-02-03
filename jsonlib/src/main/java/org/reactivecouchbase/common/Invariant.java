package org.reactivecouchbase.common;

import java.util.function.BooleanSupplier;

public class Invariant {

    public static class InvariantException extends RuntimeException {
        public InvariantException(String message) {
            super(message);
        }
    }

    public static void invariant(BooleanSupplier supplier, String message) {
        if (!supplier.getAsBoolean()) {
            throw new InvariantException(message);
        }
    }

    public static void invariant(boolean supplier, String message) {
        if (!supplier) {
            throw new InvariantException(message);
        }
    }

    public static void checkNotNull(Object what) {
        invariant(what != null, "Value should not be null");
    }
}
