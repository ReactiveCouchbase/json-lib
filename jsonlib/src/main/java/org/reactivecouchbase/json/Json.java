package org.reactivecouchbase.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Json {

    public static <T> Format<T> format(final Class<T> clazz) {
        final Writer<T> writer = Json.writes(clazz);
        final Reader<T> reader = Json.reads(clazz);
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

    public static <T> CReader<T> reads(final Class<T> clazz) {
        if (DefaultReaders.readers.containsKey(clazz)) {
            return (CReader<T>) DefaultReaders.readers.get(clazz);
        }
        return new CReader<T>() {
            @Override
            public JsResult<T> read(JsValue value) {
                try {
                    return new JsSuccess<T>(Jackson.fromJson(Jackson.jsValueToJsonNode(value), clazz));
                } catch (Exception e) {
                    return new JsError<T>(Collections.<Throwable>singletonList(e));
                }
            }
        };
    }

    public static <T> CWriter<T> writes(final Class<T> clazz) {
        return new CWriter<T>() {
            @Override
            public JsValue write(T value) {
                return Jackson.jsonNodeToJsValue(Jackson.toJson(value));
            }
        };
    }

    public static JsValue toJson(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static JsValue parse(String json) {
        return Jackson.parseJsValue(json);
    }

    public static JsObject obj(Iterable<? extends JsObject> objects) {
        JsObject root = new JsObject();
        for (JsObject object : objects) {
            root = root.add(object);
        }
        return root;
    }

    public static JsObject obj(JsObject... objects) {
        return obj(Arrays.asList(objects));
    }

    public static JsObject obj(Map<String, ?> objects) {
        JsObject obj = Json.obj();
        for (Map.Entry<String, ?> entry : objects.entrySet()) {
            obj = obj.add(Syntax.$(entry.getKey(), wrap(entry.getValue())));
        }
        return obj;
    }

    public static JsArray array(Iterable<? extends Object> objects) {
        return new JsArray(Lists.newArrayList(Lists.transform(Lists.newArrayList(objects), new Function<Object, JsValue>() {
            public JsValue apply(Object o) {
                return wrap(o);
            }
        })));
    }

    public static JsArray arr(Object... objects) {
        if (objects != null && objects.length == 1 && Iterable.class.isAssignableFrom(objects[0].getClass())) {
            return array((Iterable<Object>) objects[0]);
        }
        List<Object> objs = Arrays.asList(objects);
        return array(objs);
    }

    public static <T> JsArray arr(Iterable<T> collection, final Writer<T> writer) {
        return Json.arr(Iterables.transform(collection, new Function<T, JsValue>() {
            @Override
            public JsValue apply(T t) {
                return writer.write(t);
            }
        }));
    }

    public static String stringify(JsValue value) {
        return Jackson.generateFromJsValue(value);
    }

    public static String stringify(JsValue value, boolean pretty) {
        if (pretty) return prettyPrint(value);
        return stringify(value);
    }

    public static JsValue wrap(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static <T> JsResult<T> fromJson(JsValue value, Reader<T> reader) {
        return reader.read(value);
    }

    public static <T> JsResult<T> fromJson(String value, Reader<T> reader) {
        return reader.read(Json.parse(value));
    }

    public static <T> JsValue toJson(T o, Writer<T> writer) {
        return writer.write(o);
    }

    public static String prettyPrint(JsValue value) {
        return Jackson.prettify(value);
    }

    public static JsValue fromJsonNode(JsonNode node) {
        return Jackson.jsonNodeToJsValue(node);
    }
}