package org.reactivecouchbase.json;

import com.google.common.base.Function;

public abstract class CWriter<T> implements Writer<T> {

    public static <T> CWriter<T> cwriter(final Writer<T> writer) {
        return new CWriter<T>() {
            @Override
            public JsValue write(T value) {
                return writer.write(value);
            }
        };
    }

    public CWriter<T> transform(final Function<JsValue, JsValue> transformer) {
        final Writer<T> that = this;
        return new CWriter<T>() {
            @Override
            public JsValue write(T value) {
                return transformer.apply(that.write(value));
            }
        };
    }

    public CWriter<T> transform(final Writer<JsValue> transformer) {
        final Writer<T> that = this;
        return new CWriter<T>() {
            @Override
            public JsValue write(T value) {
                return transformer.write(that.write(value));
            }
        };
    }
}
