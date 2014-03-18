package org.reactivecouchbase.json;

import com.google.common.base.Function;

public abstract class CReader<T> implements Reader<T> {

    public static <T> CReader<T> creader(final Reader<T> reader) {
        return new CReader<T>() {
            @Override
            public JsResult<T> read(JsValue value) {
                return reader.read(value);
            }
        };
    }

    public <B> CReader<B> map(final Function<T, B> f) {
        final Reader<T> self = this;
        return new CReader<B>() {
            @Override
            public JsResult<B> read(JsValue value) {
                return self.read(value).map(f);
            }
        };
    }

    public <B> CReader<B> flatMap(final Function<T, Reader<B>> f) {
        final Reader<T> self = this;
        return new CReader<B>() {
            @Override
            public JsResult<B> read(final JsValue value) {
                return self.read(value).flatMap(new Function<T, JsResult<B>>() {
                    @Override
                    public JsResult<B> apply(T t) {
                        return f.apply(t).read(value);
                    }
                });
            }
        };
    }

    public CReader<T> filter(final Function<T, Boolean> f) {
        final Reader<T> self = this;
        return new CReader<T>() {
            @Override
            public JsResult<T> read(final JsValue value) {
                return self.read(value).filter(f);
            }
        };
    }

    public CReader<T> filterNot(final Function<T, Boolean> f) {
        final Reader<T> self = this;
        return new CReader<T>() {
            @Override
            public JsResult<T> read(final JsValue value) {
                return self.read(value).filterNot(f);
            }
        };
    }

    public CReader<T> orElse(final Reader<T> v) {
        final Reader<T> self = this;
        return new CReader<T>() {
            @Override
            public JsResult<T> read(final JsValue value) {
                return self.read(value).getOrElse(v.read(value));
            }
        };
    }

    public <B extends JsValue> CReader<T> compose(final Reader<B> v) {
        final Reader<T> self = this;
        return new CReader<T>() {
            @Override
            public JsResult<T> read(final JsValue value) {
                JsResult<B> res =  v.read(value);
                for (JsError<B> t : res.asError()) {
                    return new JsError<T>(t.errors);
                }
                for (JsSuccess<B> t : res.asSuccess()) {
                    return self.read(t.get());
                }
                throw new IllegalStateException("Should not happen");
            }
        };
    }
}
