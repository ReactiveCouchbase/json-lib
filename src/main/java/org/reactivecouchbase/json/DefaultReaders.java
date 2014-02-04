package org.reactivecouchbase.json;

import org.joda.time.DateTime;
import org.reactivecouchbase.common.Functionnal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class DefaultReaders {

    public static <A> Reader<A> pure(final A a) {
        return new Reader<A>() {
            @Override
            public JsResult<A> read(JsValue value) {
                return new JsSuccess<A>(a);
            }
        };
    }

    public static final <T> Functionnal.Option<Reader<T>> getReader(Class<T> clazz) {
        Reader<T> val = (Reader<T>) readers.get(clazz);
        if (val == null) return Functionnal.Option.none();
        return Functionnal.Option.some(val);
    }
    public static final Reader<JsObject> JS_OBJECT_READER = new Reader<JsObject>() {
        @Override
        public JsResult<JsObject> read(JsValue value) {
            if (value.is(JsObject.class)) {
                return new JsSuccess<JsObject>((JsObject) value);
            }
            return new JsError<JsObject>(new IllegalAccessError("Not a JsObject"));
        }
    };
    public static final Reader<JsArray> JS_ARRAY_READER = new Reader<JsArray>() {
        @Override
        public JsResult<JsArray> read(JsValue value) {
            if (value.is(JsArray.class)) {
                return new JsSuccess<JsArray>((JsArray) value);
            }
            return new JsError<JsArray>(new IllegalAccessError("Not a JsArray"));
        }
    };
    public static final Reader<JsBoolean> JS_BOOLEAN_READER = new Reader<JsBoolean>() {
        @Override
        public JsResult<JsBoolean> read(JsValue value) {
            if (value.is(JsBoolean.class)) {
                return new JsSuccess<JsBoolean>((JsBoolean) value);
            }
            return new JsError<JsBoolean>(new IllegalAccessError("Not a JsBoolean"));
        }
    };
    public static final Reader<JsPair> JS_PAIR_READER = new Reader<JsPair>() {
        @Override
        public JsResult<JsPair> read(JsValue value) {
            if (value.is(JsPair.class)) {
                return new JsSuccess<JsPair>((JsPair) value);
            }
            return new JsError<JsPair>(new IllegalAccessError("Not a JsPair"));
        }
    };
    public static final Reader<JsNull> JS_NULL_READER = new Reader<JsNull>() {
        @Override
        public JsResult<JsNull> read(JsValue value) {
            if (value.is(JsNull.class)) {
                return new JsSuccess<JsNull>((JsNull) value);
            }
            return new JsError<JsNull>(new IllegalAccessError("Not a JsNull"));
        }
    };
    public static final Reader<JsUndefined> JS_UNDEFINED_READER = new Reader<JsUndefined>() {
        @Override
        public JsResult<JsUndefined> read(JsValue value) {
            if (value.is(JsUndefined.class)) {
                return new JsSuccess<JsUndefined>((JsUndefined) value);
            }
            return new JsError<JsUndefined>(new IllegalAccessError("Not a JsUndefined"));
        }
    };
    public static final Reader<JsonLib.JsonFormattedValue> JS_FORMATTED_READER = new Reader<JsonLib.JsonFormattedValue>() {
        @Override
        public JsResult<JsonLib.JsonFormattedValue> read(JsValue value) {
            if (value.is(JsonLib.JsonFormattedValue.class)) {
                return new JsSuccess<JsonLib.JsonFormattedValue>((JsonLib.JsonFormattedValue) value);
            }
            return new JsError<JsonLib.JsonFormattedValue>(new IllegalAccessError("Not a JsonFormattedValue"));
        }
    };
    public static final Reader<JsNumber> JS_NUMBER_READER = new Reader<JsNumber>() {
        @Override
        public JsResult<JsNumber> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<JsNumber>((JsNumber) value);
            }
            return new JsError<JsNumber>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<JsString> JS_STRING_READER = new Reader<JsString>() {
        @Override
        public JsResult<JsString> read(JsValue value) {
            if (value.is(JsString.class)) {
                return new JsSuccess<JsString>((JsString) value);
            }
            return new JsError<JsString>(new IllegalAccessError("Not a JsString"));
        }
    };
    public static final Reader<Boolean> BOOLEAN_READER = new Reader<Boolean>() {
        @Override
        public JsResult<Boolean> read(JsValue value) {
            if (value.is(JsBoolean.class)) {
                return new JsSuccess<Boolean>(((JsBoolean) value).value);
            }
            return new JsError<Boolean>(new IllegalAccessError("Not a JsBoolean"));
        }
    };
    public static final Reader<String> STRING_READER = new Reader<String>() {
        @Override
        public JsResult<String> read(JsValue value) {
            if (value.is(JsString.class)) {
                return new JsSuccess<String>(((JsString) value).value);
            }
            return new JsError<String>(new IllegalAccessError("Not a JsString"));
        }
    };
    public static final Reader<Double> DOUBLE_READER = new Reader<Double>() {
        @Override
        public JsResult<Double> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<Double>(((JsNumber) value).value.doubleValue());
            }
            return new JsError<Double>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<Long> LONG_READER = new Reader<Long>() {
        @Override
        public JsResult<Long> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<Long>(((JsNumber) value).value.longValue());
            }
            return new JsError<Long>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<Integer> INTEGER_READER = new Reader<Integer>() {
        @Override
        public JsResult<Integer> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<Integer>(((JsNumber) value).value.intValue());
            }
            return new JsError<Integer>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<BigDecimal> BIGDEC_READER = new Reader<BigDecimal>() {
        @Override
        public JsResult<BigDecimal> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<BigDecimal>(((JsNumber) value).value);
            }
            return new JsError<BigDecimal>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<BigInteger> BIGINT_READER = new Reader<BigInteger>() {
        @Override
        public JsResult<BigInteger> read(JsValue value) {
            if (value.is(JsNumber.class)) {
                return new JsSuccess<BigInteger>(((JsNumber) value).value.toBigInteger());
            }
            return new JsError<BigInteger>(new IllegalAccessError("Not a JsNumber"));
        }
    };
    public static final Reader<DateTime> DATETIME_READER = new Reader<DateTime>() {
        @Override
        public JsResult<DateTime> read(JsValue value) {
            if (value.is(JsString.class)) {
                try {
                    return new JsSuccess<DateTime>(DateTime.parse(value.as(String.class)));
                } catch (Exception e) {
                    return new JsError<DateTime>(e);
                }
            }
            return new JsError<DateTime>(new IllegalAccessError("Not a JsString"));
        }
    };
    public static final Reader<JsValue> JSVALUE_READER = new Reader<JsValue>() {
        @Override
        public JsResult<JsValue> read(JsValue value) {
            return new JsSuccess<JsValue>(value);
        }
    };
    static final Map<Class<?>, Reader<?>> readers = new HashMap<Class<?>, Reader<?>>() {{
        put(JsObject.class, JS_OBJECT_READER);
        put(JsArray.class, JS_ARRAY_READER);
        put(JsBoolean.class, JS_BOOLEAN_READER);
        put(JsPair.class, JS_PAIR_READER);
        put(JsNull.class, JS_NULL_READER);
        put(JsUndefined.class, JS_UNDEFINED_READER);
        put(JsonLib.JsonFormattedValue.class, JS_FORMATTED_READER);
        put(JsNumber.class , JS_NUMBER_READER);
        put(JsString.class, JS_STRING_READER);
        put(Boolean.class, BOOLEAN_READER);
        put(String.class, STRING_READER);
        put(Double.class, DOUBLE_READER);
        put(Long.class, LONG_READER);
        put(Integer.class, INTEGER_READER);
        put(BigDecimal.class, BIGDEC_READER);
        put(BigInteger.class, BIGINT_READER);
        put(JsValue.class, JSVALUE_READER);
        put(DateTime.class, DATETIME_READER);
    }};
}