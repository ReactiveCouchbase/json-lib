package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsValue;

import java.util.List;

public class DefaultFormats {

    private DefaultFormats() {
    }

    public static <T> Format<List<T>> seq(Format<T> format) {
        return compose(DefaultReaders.seq(format), DefaultWriters.seq(format));
    }

    public static <T> Format<T> compose(final Reader<T> reader, final Writer<T> writer) {
        return new Format<T>() {
            @Override
            public JsResult<T> read(JsValue value) {
                return reader.read(value);
            }

            @Override
            public JsValue write(T value) {
                return writer.write(value);
            }
        };
    }
}
