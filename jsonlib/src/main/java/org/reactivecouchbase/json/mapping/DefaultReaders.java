package org.reactivecouchbase.json.mapping;

import org.joda.time.DateTime;
import org.reactivecouchbase.functional.Option;
import org.reactivecouchbase.json.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultReaders {

    private DefaultReaders() {
    }

    public static <T> Reader<List<T>> seq(final Format<T> reader) {
        return seq((Reader<T>) reader);
    }

    public static <T> Reader<List<T>> seq(final Reader<T> reader) {
        return value -> {
            try {
                JsArray array = value.as(JsArray.class);
                return new JsSuccess<>(array.mapWith(reader));
            } catch (Exception e) {
                return new JsError<>(e);
            }
        };
    }

    public static <A> Reader<A> pure(final A a) {
        return value -> new JsSuccess<>(a);
    }

    @SuppressWarnings("unchecked")
    public static <T> Option<Reader<T>> getReader(Class<T> clazz) {
        Reader<T> val = (Reader<T>) readers.get(clazz);
        if (val == null) {
            return Option.none();
        }
        return Option.some(val);
    }

    public static final Reader<JsObject> JS_OBJECT_READER = value -> {
        if (value.is(JsObject.class)) {
            return new JsSuccess<>((JsObject) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsObject"));
    };
    public static final Reader<JsArray> JS_ARRAY_READER = value -> {
        if (value.is(JsArray.class)) {
            return new JsSuccess<>((JsArray) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsArray"));
    };
    public static final Reader<JsBoolean> JS_BOOLEAN_READER = value -> {
        if (value.is(JsBoolean.class)) {
            return new JsSuccess<>((JsBoolean) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsBoolean"));
    };
    public static final Reader<JsPair> JS_PAIR_READER = value -> {
        if (value.is(JsPair.class)) {
            return new JsSuccess<>((JsPair) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsPair"));
    };
    public static final Reader<JsNull> JS_NULL_READER = value -> {
        if (value.is(JsNull.class)) {
            return new JsSuccess<>((JsNull) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsNull"));
    };
    public static final Reader<JsUndefined> JS_UNDEFINED_READER = value -> {
        if (value.is(JsUndefined.class)) {
            return new JsSuccess<>((JsUndefined) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsUndefined"));
    };
    public static final Reader<JsNumber> JS_NUMBER_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>((JsNumber) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<JsString> JS_STRING_READER = value -> {
        if (value.is(JsString.class)) {
            return new JsSuccess<>((JsString) value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<Boolean> BOOLEAN_READER = value -> {
        if (value.is(JsBoolean.class)) {
            return new JsSuccess<>(((JsBoolean) value).value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsBoolean"));
    };
    public static final Reader<String> STRING_READER = value -> {
        if (value.is(JsString.class)) {
            return new JsSuccess<>(((JsString) value).value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<Double> DOUBLE_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>(((JsNumber) value).value.doubleValue());
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<Long> LONG_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>(((JsNumber) value).value.longValue());
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<Integer> INTEGER_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>(((JsNumber) value).value.intValue());
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<BigDecimal> BIGDEC_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>(((JsNumber) value).value);
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<BigInteger> BIGINT_READER = value -> {
        if (value.is(JsNumber.class)) {
            return new JsSuccess<>(((JsNumber) value).value.toBigInteger());
        }
        return new JsError<>(new IllegalAccessError("Not a JsNumber"));
    };
    public static final Reader<DateTime> DATETIME_READER = value -> {
        if (value.is(JsString.class)) {
            try {
                return new JsSuccess<>(DateTime.parse(value.as(String.class)));
            } catch (Exception e) {
                return new JsError<>(e);
            }
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<LocalTime> LOCAL_TIME_READER = value -> {
        if (value.is(JsString.class)) {
            try {
                return new JsSuccess<>(LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(value.as(String.class))));
            } catch (Exception e) {
                return new JsError<>(e);
            }
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<LocalDate> LOCAL_DATE_READER = value -> {
        if (value.is(JsString.class)) {
            try {
                return new JsSuccess<>(LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(value.as(String.class))));
            } catch (Exception e) {
                return new JsError<>(e);
            }
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<LocalDateTime> LOCAL_DATE_TIME_READER = value -> {
        if (value.is(JsString.class)) {
            try {
                return new JsSuccess<>(LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(value.as(String.class))));
            } catch (Exception e) {
                return new JsError<>(e);
            }
        }
        return new JsError<>(new IllegalAccessError("Not a JsString"));
    };
    public static final Reader<JsValue> JSVALUE_READER = JsSuccess::new;
    public static final Map<Class<?>, Reader<?>> readers = new HashMap<Class<?>, Reader<?>>() {{
        put(JsObject.class, JS_OBJECT_READER);
        put(JsArray.class, JS_ARRAY_READER);
        put(JsBoolean.class, JS_BOOLEAN_READER);
        put(JsPair.class, JS_PAIR_READER);
        put(JsNull.class, JS_NULL_READER);
        put(JsUndefined.class, JS_UNDEFINED_READER);
        put(JsNumber.class, JS_NUMBER_READER);
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
        put(LocalTime.class, LOCAL_TIME_READER);
        put(LocalDate.class, LOCAL_DATE_READER);
        put(LocalDateTime.class, LOCAL_DATE_TIME_READER);
    }};
}