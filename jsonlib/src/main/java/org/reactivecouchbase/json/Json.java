package org.reactivecouchbase.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.reactivecouchbase.json.mapping.*;
import org.reactivecouchbase.validation.Rule;
import org.reactivecouchbase.validation.Validation;
import org.reactivecouchbase.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

import static org.reactivecouchbase.json.Syntax.$;

public class Json {

    public static JsObject obj(Map<String, ?> objects) {
        JsObject obj = Json.obj();
        for (Map.Entry<String, ?> entry : objects.entrySet()) {
            obj = obj.add($(entry.getKey(), wrap(entry.getValue())));
        }
        return obj;
    }

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

    public static <T> Reader<T> reads(final Class<T> clazz) {
        if (DefaultReaders.readers.containsKey(clazz)) {
            return (Reader<T>) DefaultReaders.readers.get(clazz);
        }
        return value -> {
            try {
                return new JsSuccess<T>(Jackson.fromJson(Jackson.jsValueToJsonNode(value), clazz));
            } catch (Exception e) {
                return new JsError<T>(Collections.<Throwable>singletonList(e));
            }
        };
    }

    public static <T> Writer<T> writes(final Class<T> clazz) {
        return value -> Jackson.jsonNodeToJsValue(Jackson.toJson(value));
    }

    public static JsValue toJson(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static JsValue parse(String json) {
        return Jackson.parseJsValue(json);
    }

    public static <T> Reader<T> safeReader(final Reader<T> reader) {
        return value -> {
            try {
                return reader.read(value);
            } catch (Exception e) {
                return JsResult.error(e);
            }
        };
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

    public static JsObject obj() {
        return obj(new ArrayList<JsObject>());
    }

    public static <T extends Object> JsArray array(List<T> objects) {
        return new JsArray(objects.stream().map(Json::wrap).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public static JsArray arr(Object... objects) {
        if (objects != null && objects.length == 1 && List.class.isAssignableFrom(objects[0].getClass())) {
            return array((List<Object>) objects[0]);
        }
        List<Object> objs = Arrays.asList(objects);
        return array(objs);
    }

    public static <T> JsArray arr(List<T> collection, final Writer<T> writer) {
        return Json.arr(collection.stream().map(writer::write).collect(Collectors.toList()));
    }

    public static String stringify(JsValue value) {
        return value.toJsonString();
    }

    public static String stringify(JsValue value, boolean pretty) {
        if (pretty) {
            return prettyPrint(value);
        }
        return stringify(value);
    }

    @SuppressWarnings("unchecked")
    public static JsValue wrap(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static <T> JsResult<T> fromJson(JsValue value, Reader<T> reader) {
        return reader.read(value);
    }

    public static <T> JsResult<T> fromJson(String value, Reader<T> reader) {
        return reader.read(Json.parse(value));
    }

    public static <T> Validation<T, ValidationError> fromJson(JsValue value, Rule<JsValue, T> reader) {
        return reader.validate(value);
    }

    public static <T> Validation<T, ValidationError> fromJson(String value, Rule<JsValue, T> reader) {
        return reader.validate(Json.parse(value));
    }

    public static <T, V extends T> JsValue toJson(V o, Writer<T> writer) {
        return writer.write(o);
    }

    public static <T> JsonNode toJackson(JsValue value) {
        return Jackson.toJson(value);
    }

    public static String prettyPrint(JsValue value) {
        return Jackson.prettify(value);
    }

    public static JsValue fromJsonNode(JsonNode node) {
        return Jackson.jsonNodeToJsValue(node);
    }
}