package org.reactivecouchbase.json;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class JsValidator<T> implements Reader<T> {

    private final Reader<T> baseReader;
    private final List<Reader<T>> validators;
    private final boolean traverse;

    public static <T> JsValidator<T> validateWith(final Class<T> clazz, Reader<T>... reads) {
        Preconditions.checkNotNull(clazz);
        return new JsValidator<T>(DefaultReaders.getReader(clazz).getOrElse(new Callable<Reader<T>>() {
            @Override
            public Reader<T> call() throws Exception {
                throw new IllegalStateException("No reader found for class " + clazz.getName());
            }
        }), Arrays.asList(reads), true);
    }

    private static <T> JsValidator<T> validateWith(Reader<T> base, Reader<T>... reads) {
        Preconditions.checkNotNull(base);
        return new JsValidator<T>(base, Arrays.asList(reads), true);
    }

    public JsValidator<T> traversable() {
        return new JsValidator<T>(baseReader, validators, true);
    }

    public JsValidator<T> failFast() {
        return new JsValidator<T>(baseReader, validators, false);
    }

    public JsValidator(Reader<T> base, List<Reader<T>> validators, boolean traverse) {
        this.baseReader = base;
        this.validators = validators;
        this.traverse = traverse;
    }

    public JsValidator<T> and(Reader<T> reader) {
        Preconditions.checkNotNull(reader);
        List<Reader<T>> newReaders = new ArrayList<Reader<T>>();
        newReaders.addAll(validators);
        newReaders.add(reader);
        return new JsValidator<T>(baseReader, newReaders, traverse);
    }

    @Override
    public JsResult<T> read(JsValue value) {
        JsResult<T> firstRes = baseReader.read(value);
        JsResult<T> lastRes = firstRes;
        List<Throwable> throwables = new ArrayList<Throwable>();
        if (lastRes.isErrors()) {
            throwables.addAll(lastRes.asError().get().errors);
        }
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
            return firstRes;
        } else {
            return new JsError<T>(throwables);
        }
    }
}
