package org.reactivecouchbase.json.exceptions;

import org.reactivecouchbase.json.JsObject;
import org.reactivecouchbase.json.JsValue;
import org.reactivecouchbase.json.mapping.DefaultWriters;

public class JsException extends RuntimeException {
    public JsException() {
    }

    public JsException(String s) {
        super(s);
    }

    public JsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JsException(Throwable throwable) {
        super(throwable);
    }

    public JsValue asJson() {
        return DefaultWriters.throwableAsJson(this, false);
    }

    public JsObject asJsObject() {
        return DefaultWriters.throwableAsJsObject(this, false);
    }

    public JsValue asJson(boolean stacks) {
        return DefaultWriters.throwableAsJson(this, stacks);
    }

    public JsObject asJsObject(boolean stacks) {
        return DefaultWriters.throwableAsJsObject(this, stacks);
    }
}