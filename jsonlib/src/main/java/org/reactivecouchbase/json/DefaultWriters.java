package org.reactivecouchbase.json;

import java.util.List;

public class DefaultWriters {

    public static <T> Writer<List<T>> seq(final Writer<T> writer) {
        return new Writer<List<T>>() {
            @Override
            public JsValue write(List<T> value) {
                JsArray array = Json.arr();
                for (T val : value) {
                    array = array.addElement(writer.write(val));
                }
                return array;
            }
        };
    }
}
