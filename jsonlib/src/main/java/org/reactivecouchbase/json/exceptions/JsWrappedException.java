package org.reactivecouchbase.json.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class JsWrappedException extends JsException {
    private final List<Exception> exceptions;

    public JsWrappedException(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTraceElements) {
        throw new IllegalAccessError("Can set stackstrace");
    }

    @Override
    public String getMessage() {
        return exceptions.stream().map(Throwable::getMessage).collect(Collectors.joining(" | "));
    }

    @Override
    public String getLocalizedMessage() {
        return exceptions.stream().map(Throwable::getLocalizedMessage).collect(Collectors.joining(" | "));
    }

    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public void printStackTrace() {
        exceptions.forEach(Throwable::printStackTrace);
    }
}