package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsValidator<T> implements Reader<T> {

    private final List<Reader<T>> validators;
    private final boolean traverse;

    public static <T> JsValidator<T> validateWith(final Class<T> clazz) {
        return new JsValidator<T>(Collections.singletonList(DefaultReaders.getReader(clazz).getOrElse(() -> {
            throw new IllegalStateException("No reader found for class " + clazz.getName());
        })), true);
    }

    public static <T> JsValidator<T> validateWith(Reader<T> base) {
        return new JsValidator<T>(Collections.singletonList(base), true);
    }

    public static <T> JsValidator<T> of(final Class<T> clazz) {
        return new JsValidator<>(new ArrayList<>(), true);
    }

    public JsValidator<T> traversable() {
        return new JsValidator<>(validators, true);
    }

    public JsValidator<T> failFast() {
        return new JsValidator<>(validators, false);
    }

    public JsValidator(List<Reader<T>> validators, boolean traverse) {
        this.validators = validators;
        this.traverse = traverse;
    }

    public JsValidator<T> and(Reader<T> reader) {
        List<Reader<T>> newReaders = new ArrayList<>();
        newReaders.addAll(validators);
        newReaders.add(reader);
        return new JsValidator<>(newReaders, traverse);
    }

    @Override
    public JsResult<T> read(JsValue value) {
        JsResult<T> lastRes = JsResult.error(new RuntimeException("No validators"));
        List<Throwable> throwables = new ArrayList<>();
        for (Reader<T> reader : validators) {
            lastRes = reader.read(value);
            if (lastRes.isErrors()) {
                if (!traverse) {
                    return lastRes;
                } else {
                    throwables.addAll(lastRes.asError().get().errors);
                }
            }
        }
        if (throwables.isEmpty()) {
            return lastRes;
        } else {
            return new JsError<>(throwables);
        }
    }
}