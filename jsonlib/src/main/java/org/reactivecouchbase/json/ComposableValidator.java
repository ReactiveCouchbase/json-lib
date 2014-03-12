package org.reactivecouchbase.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ComposableValidator<T> implements Reader<T> {

    private final Reader<T> baseReader;
    private final List<Reader<T>> validators;
    private final boolean traverse;

    public static <T> ComposableValidator<T> validateWith(final Class<T> clazz, Reader<T>... reads) {
        return new ComposableValidator<T>(DefaultReaders.getReader(clazz).getOrElse(new Callable<Reader<T>>() {
            @Override
            public Reader<T> call() throws Exception {
                throw new IllegalStateException("No reader found for class " + clazz.getName());
            }
        }), Arrays.asList(reads), true);
    }

    private static <T> ComposableValidator<T> validateWith(Reader<T> base, Reader<T>... reads) {
        return new ComposableValidator<T>(base, Arrays.asList(reads), true);
    }

    public ComposableValidator<T> traversable() {
        return new ComposableValidator<T>(baseReader, validators, true);
    }

    public ComposableValidator<T> firstError() {
        return new ComposableValidator<T>(baseReader, validators, false);
    }

    public ComposableValidator(Reader<T> base, List<Reader<T>> validators, boolean traverse) {
        this.baseReader = base;
        this.validators = validators;
        this.traverse = traverse;
    }

    public ComposableValidator<T> and(Reader<T> reader) {
        List<Reader<T>> newReaders = new ArrayList<Reader<T>>();
        newReaders.addAll(validators);
        newReaders.add(reader);
        return new ComposableValidator<T>(baseReader, newReaders, traverse);
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
