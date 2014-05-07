package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import java.util.Date;

public class Syntax {

    /**
     * To wrap already JSON formatted data. Try to avoid it !!!
     */
    public static class JsonFormattedValue extends JsValue {
        public final String value;

        public JsonFormattedValue(String value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = value;
        }

        String toJsonString() {
            return value;
        }

        public String toString() {
            return "Json(" + value + ")";
        }

        @Override
        public boolean deepEquals(Object o) {
            return equals(o);
        }

        @Override
        public JsValue cloneNode() {
            throw new RuntimeException("Unable to clone JsonFormattedValue");
        }
    }

    /* Helper to create JsValues on the fly */

    public static JsPair $(String name, JsValue value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Date value) {
        if (value == null) return _(name);
        return new JsPair(name, new DateTime(value).toString());
    }
    public static JsPair $(String name, DateTime value) {
        if (value == null) return _(name);
        return new JsPair(name, value.toString());
    }
    public static JsPair $(String name, Date value, String format) {
        if (value == null) return _(name);
        return new JsPair(name, new DateTime(value).toString(format));
    }
    public static JsPair $(String name, DateTime value, String format) {
        if (value == null) return _(name);
        return new JsPair(name, value.toString(format));
    }
    public static JsPair $(String name, DateTime value, org.joda.time.format.DateTimeFormatter format) {
        if (value == null) return _(name);
        return new JsPair(name, value.toString(format));
    }

    public static JsPair $(String name, Long value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Short value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Float value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Integer value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Double value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, BigDecimal value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, BigInteger value) {
        return new JsPair(name, value);
    }

    public static JsPair _(String name) {
        return new JsPair(name);
    }

    public static JsPair __(String name) {
        return new JsPair(name, JSUNDEFINED_INSTANCE);
    }

    public static JsPair $(String name, String value) {
        return new JsPair(name, value);
    }

    public static JsPair $$(String name, String value) {
        return new JsPair(name, new JsonFormattedValue(value));
    }

    public static JsPair $$$(String name, Object value) {
        return new JsPair(name, Json.wrap(value));
    }

    public static JsPair $(String name, Boolean value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Map<String, ?> value) {
        return new JsPair(name, mapAsObj(value));
    }

    public static JsObject mapAsObj(Map<String, ?> value) {
        return new JsObject(Maps.transformValues(value, new Function<Object, JsValue>() {
            @Override
            public JsValue apply(java.lang.Object o) {
                return Json.wrap(o);
            }
        }));
    }

    public static JsString string(String value) {
        return new JsString(value);
    }

    public static JsBoolean bool(Boolean value) {
        return new JsBoolean(value);
    }

    public static JsNumber number(Integer value) {
        return new JsNumber(value);
    }

    public static JsNumber number(Long value) {
        return new JsNumber(value);
    }

    public static JsNumber number(Double value) {
        return new JsNumber(value);
    }

    public static JsNumber number(BigDecimal value) {
        return new JsNumber(value);
    }

    public static JsNumber number(BigInteger value) {
        return new JsNumber(value);
    }

    public static JsValue json(String value) {
        return new JsonFormattedValue(value);
    }

    static final JsNull JSNULL_INSTANCE = new JsNull();

    public static JsNull nill() {
        return JSNULL_INSTANCE;
    }

    static final JsUndefined JSUNDEFINED_INSTANCE = new JsUndefined();

    public static JsUndefined undefined() {
        return JSUNDEFINED_INSTANCE;
    }

    static Map<String, JsValue> asMap(String name, JsValue value) {
        Map<String, JsValue> values = new HashMap<String, JsValue>();
        values.put(name, value);
        return values;
    }
}
