package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsArray;
import org.reactivecouchbase.json.JsObject;
import org.reactivecouchbase.json.JsValue;
import org.reactivecouchbase.json.Json;

import java.util.List;

public class DefaultWriters {

    private DefaultWriters() {
    }

    public static <T> Writer<List<T>> seq(final Format<T> writer) {
        return seq((Writer<T>) writer);
    }

    public static <T> Writer<List<T>> seq(final Writer<T> writer) {
        return value -> {
            JsArray array = Json.arr();
            for (T val : value) {
                array = array.addElement(writer.write(val));
            }
            return array;
        };
    }

    public static JsValue throwableAsJson(Throwable t, boolean printstacks) {
        return Json.toJson(t, new ThrowableWriter(printstacks));
    }

    public static JsObject throwableAsJsObject(Throwable t, boolean printstacks) {
        return Json.toJson(t, new ThrowableWriter(printstacks)).as(JsObject.class);
    }

    public static JsValue throwableAsJson(Throwable t) {
        return Json.toJson(t, new ThrowableWriter(true));
    }

    public static JsObject throwableAsJsObject(Throwable t) {
        return Json.toJson(t, new ThrowableWriter(true)).as(JsObject.class);
    }
}