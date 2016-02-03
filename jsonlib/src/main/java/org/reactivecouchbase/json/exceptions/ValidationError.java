package org.reactivecouchbase.json.exceptions;

public class ValidationError extends JsException {

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
}
