package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsValue;

import java.util.function.Function;

public interface Writer<T> {
    JsValue write(T value);

    default Writer<T> transform(final Function<JsValue, JsValue> transformer) {
        final Writer<T> that = this;
        return value -> transformer.apply(that.write(value));
    }

    default Writer<T> transform(final Writer<JsValue> transformer) {
        final Writer<T> that = this;
        return value -> transformer.write(that.write(value));
    }
}