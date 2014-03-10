package org.reactivecouchbase.json;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JsSuccess<T> extends JsResult<T> {

    private final T value;

    public JsSuccess(T value) {
        this.value = value;
    }

    public Functionnal.Option<JsError<T>> asError() {
        return Functionnal.Option.none();
    }

    public Functionnal.Option<JsSuccess<T>> asSuccess() {
        return Functionnal.Option.some(this);
    }

    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean isErrors() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public int countErrors() {
        return 0;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T orError(Throwable t) {
        return get();
    }

    @Override
    public Functionnal.Option<T> getOpt() {
        return Functionnal.Option.some(value);
    }

    @Override
    public T getValueOrElse(T result) {
        return value;
    }

    @Override
    public T getValueOrElse(Throwable result) {
        return value;
    }

    @Override
    public JsResult<T> getOrElse(JsResult<T> result) {
        return new JsSuccess<T>(value);
    }

    @Override
    public <B> JsResult<B> map(Function<T, B> map) {
        return new JsSuccess<B>(map.apply(value));
    }

    @Override
    public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
        return map.apply(value);
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> p) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (p.apply(a)) {
                    return new JsSuccess<T>(a);
                }
                return new JsError<T>(new ArrayList<Throwable>());
            }
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> p) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (p.apply(a)) {
                    return new JsError<T>(new ArrayList<Throwable>());
                }
                return new JsSuccess<T>(a);
            }
        });
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> predicate, final List<Throwable> errors) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (predicate.apply(a)) {
                    return new JsSuccess<T>(a);
                }
                List<Throwable> ts = new ArrayList<Throwable>();
                ts.addAll(errors);
                return new JsError<T>(ts);
            }
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> predicate, final List<Throwable> errors) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (predicate.apply(a)) {
                    List<Throwable> ts = new ArrayList<Throwable>();
                    ts.addAll(errors);
                    return new JsError<T>(ts);
                }
                return new JsSuccess<T>(a);
            }
        });
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> predicate, final Throwable error) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (predicate.apply(a)) {
                    return new JsSuccess<T>(a);
                }
                List<Throwable> ts = new ArrayList<Throwable>();
                ts.add(error);
                return new JsError<T>(ts);
            }
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> predicate, final Throwable error) {
        return this.flatMap(new Function<T, JsResult<T>>() {
            public JsResult<T> apply(T a) {
                if (predicate.apply(a)) {
                    List<Throwable> ts = new ArrayList<Throwable>();
                    ts.add(error);
                    return new JsError<T>(ts);
                }
                return new JsSuccess<T>(a);
            }
        });
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.singletonList(value).iterator();
    }
}