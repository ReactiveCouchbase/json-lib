package org.reactivecouchbase.json;

public class ValidationError extends RuntimeException {

    public ValidationError() {
    }

    public ValidationError(String s) {
        super(s);
    }

    public ValidationError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ValidationError(Throwable throwable) {
        super(throwable);
    }

    public JsValue asJson() {
        return DefaultWriters.throwableAsJson(this);
    }

    public JsValue asJson(boolean stack) {
        return DefaultWriters.throwableAsJson(this, stack);
    }
}
