package org.reactivecouchbase.json;

import org.reactivecouchbase.common.Throwables;
import org.reactivecouchbase.functional.Option;
import org.reactivecouchbase.functional.Tuple;
import org.reactivecouchbase.json.mapping.JsResult;
import org.reactivecouchbase.json.mapping.Reader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.reactivecouchbase.json.Syntax.$;
import static org.reactivecouchbase.json.Syntax.nill;

public class JsObject extends JsValue implements Iterable<Map.Entry<String, JsValue>> {
    public final Map<String, JsValue> values;

    public JsObject(Map<String, JsValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null !");
        }
        this.values = Collections.unmodifiableMap(values);
    }

    public JsObject() {
        this.values = Collections.unmodifiableMap(new HashMap<>());
    }

    public JsObject merge(JsObject with) {
        if (with == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        Map<String, JsValue> newValues = new HashMap<>();
        newValues.putAll(with.values);
        newValues.putAll(values);
        return new JsObject(newValues);
    }

    @Override
    public Iterator<Map.Entry<String, JsValue>> iterator() {
        return values.entrySet().iterator();
    }

    public JsObject deepMerge(JsObject with) {
        if (with == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        Map<String, JsValue> newValues = new HashMap<>();
        newValues.putAll(with.values);
        for (Map.Entry<String, JsValue> entry : values.entrySet()) {
            if (with.values.containsKey(entry.getKey()) && entry.getValue().is(JsObject.class)) {
                newValues.put(entry.getKey(), entry.getValue().as(JsObject.class).deepMerge(with.values.get(entry.getKey()).as(JsObject.class)));
            } else {
                newValues.put(entry.getKey(), entry.getValue());
            }
        }
        return new JsObject(newValues);
    }

    public Set<String> fieldsSet() {
        return values.keySet();
    }

    public Collection<JsValue> values() {
        return values.values();
    }

    public JsObject add(JsObject jsObject) {
        if (jsObject == null) {
            return new JsObject(values);
        }
        Map<String, JsValue> newValues = new HashMap<String, JsValue>();
        newValues.putAll(values);
        newValues.putAll(jsObject.values);
        return new JsObject(newValues);
    }

    public JsObject add(String key, Option<JsValue> optVal) {
        if (optVal.isDefined()) {
            return add($(key, optVal.get()));
        }
        return new JsObject(values);
    }

    public JsObject addOrNull(String key, Option<JsValue> optVal) {
        if (optVal.isDefined()) {
            return add($(key, optVal.get()));
        } else {
            return add($(key, nill()));
        }
    }

    // update only if key is present
    public JsObject update(String key, Function<JsValue, JsValue> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        for (JsValue val : field) {
            return this.add(key, Option.apply(value.apply(val)));
        }
        return this;
    }
    // update only if key is present and option is some
    public JsObject updateOpt(String key, Function<JsValue, Option<JsValue>> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        for (JsValue val : field) {
            return this.add(key, value.apply(val));
        }
        return this;
    }
    // update or insert at key
    public JsObject upsert(String key, Function<Option<JsValue>, JsValue> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        JsValue ret = value.apply(field);
        return this.add(key, Option.apply(ret));
    }
    // update or insert at key only if returned option is Some
    public JsObject upsertOpt(String key, Function<Option<JsValue>, Option<JsValue>> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        Option<JsValue> ret = value.apply(field);
        if (ret == null) {
            return this;
        }
        return this.add(key, ret);
    }

    public JsObject remove(String field) {
        if (field == null) {
            return new JsObject(values);
        }
        Map<String, JsValue> newValues = new HashMap<String, JsValue>();
        newValues.putAll(values);
        newValues.remove(field);
        return new JsObject(newValues);
    }

    @Override
    public JsValue field(String field) {
        if (field == null) {
            return JsUndefined.JSUNDEFINED_INSTANCE;
        }
        JsValue value = values.get(field);
        if (value == null) {
            return JsUndefined.JSUNDEFINED_INSTANCE;
        }
        return value;
    }

    @Override
    public Option<JsValue> fieldAsOpt(String field) {
        if (field == null) {
            return Option.none();
        }
        JsValue val = values.get(field);
        if (val == null) {
            return Option.none();
        }
        return Option.some(val);
    }

    @Override
    public List<JsValue> fields(String fieldName) {
        if (fieldName == null) {
            return Collections.emptyList();
        }
        List<JsValue> vals = new ArrayList<JsValue>();
        for (Map.Entry<String, JsValue> field : values.entrySet()) {
            if (field.getKey().equals(fieldName)) {
                vals.add(field.getValue());
            }
            for (JsObject obj : field.getValue().asOpt(JsObject.class)) {
                vals.addAll(obj.fields(fieldName));
            }
            for (JsObject obj : field.getValue().asOpt(JsPair.class)) {
                vals.addAll(obj.fields(fieldName));
            }
        }
        return vals;
    }

    @Override
    String toJsonString() {
        return "{" + toJsonPairString() + "}";
    }

    @Override
    public String toString() {
        return "JsObject(" + toJsonPairString() + ")";
    }

    private String toJsonPairString() {
        return values.entrySet().stream().map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue().toJsonString()).collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsObject)) {
            return false;
        }
        JsObject object = (JsObject) o;
        if (!values.equals(object.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deepEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsObject)) {
            return false;
        }
        JsObject object = (JsObject) o;
        for (Map.Entry<String, JsValue> value : values.entrySet()) {
            JsValue field = object.field(value.getKey());
            if (!field.deepEquals(value.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public Boolean exists(String field) {
        return values.containsKey(field);
    }

    public JsObject mapProperties(Function<Tuple<String, JsValue>, JsValue> block) {
        Map<String, JsValue> resulting = new HashMap<>();
        for (Map.Entry<String, JsValue> entry : values.entrySet()) {
            JsValue tuple = block.apply(new Tuple<>(entry.getKey(), entry.getValue()));
            resulting.put(entry.getKey(), tuple);
        }
        return new JsObject(resulting);
    }

    public <T> Map<String, T> mapPropertiesWith(Reader<T> reader) {
        Map<String, T> resultMap = new HashMap<>();
        for (Map.Entry<String, JsValue> entry : values.entrySet()) {
            JsResult<T> result = reader.read(entry.getValue());
            if (result.hasErrors()) {
                throw Throwables.propagate(result.asError().get().firstError());
            }
            resultMap.put(entry.getKey(), result.get());
        }
        return resultMap;
    }

    public <T> Map<String, T> mapPropertiesWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
        Map<String, T> resultMap = new HashMap<>();
        for (Map.Entry<String, JsValue> entry : values.entrySet()) {
            JsResult<T> result = reader.read(entry.getValue());
            if (result.hasErrors()) {
                resultMap.put(entry.getKey(), onError.apply(result));
            } else {
                resultMap.put(entry.getKey(), result.get());
            }
        }
        return resultMap;
    }

    public int nbrOfElements() {
        return size();
    }

    public int size() {
        return values == null ? 0 : values.size();
    }

    public boolean isEmpty() {
        return values == null || values.isEmpty();
    }

    public boolean notEmpty() {
        return !isEmpty();
    }

    @Override
    public JsObject cloneNode() {
        return new JsObject(new HashMap<>(values));
    }

    public JsObject with(String key) {
        return add(new JsPair(key, JsNull.JSNULL_INSTANCE));
    }

    public JsObject withNull(String key) {
        return add(new JsPair(key, JsNull.JSNULL_INSTANCE));
    }
    public JsObject withUndefined(String key) {
        return add(new JsPair(key, JsUndefined.JSUNDEFINED_INSTANCE));
    }
    public <T extends JsValue> JsObject with(String key, T value) {
        return add(new JsPair(key, value));
    }

    public <T extends JsValue> JsObject with(String key, Option<T> value) {
        return add(key, value.as(JsValue.class));
    }

    public JsObject with(String key, Integer value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Long value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Double value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, BigInteger value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, BigDecimal value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Boolean value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, String value) {
        return add(new JsPair(key, value));
    }

    public JsObject withInt(String key, Option<Integer> value) {
        return add(key, value.map(new Function<Integer, JsValue>() {
            @Override
            public JsValue apply(Integer input) {
                return new JsNumber(input);
            }
        }));
    }

    public JsObject withLong(String key, Option<Long> value) {
        return add(key, value.map(new Function<Long, JsValue>() {
            @Override
            public JsValue apply(Long input) {
                return new JsNumber(input);
            }
        }));
    }

    public JsObject withDouble(String key, Option<Double> value) {
        return add(key, value.map(new Function<Double, JsValue>() {
            @Override
            public JsValue apply(Double input) {
                return new JsNumber(input);
            }
        }));
    }

    public JsObject withBigInt(String key, Option<BigInteger> value) {
        return add(key, value.map(new Function<BigInteger, JsValue>() {
            @Override
            public JsValue apply(BigInteger input) {
                return new JsNumber(input);
            }
        }));
    }

    public JsObject withBigDec(String key, Option<BigDecimal> value) {
        return add(key, value.map(new Function<BigDecimal, JsValue>() {
            @Override
            public JsValue apply(BigDecimal input) {
                return new JsNumber(input);
            }
        }));
    }

    public JsObject withBoolean(String key, Option<Boolean> value) {
        return add(key, value.map(new Function<Boolean, JsValue>() {
            @Override
            public JsValue apply(Boolean input) {
                return new JsBoolean(input);
            }
        }));
    }

    public JsObject withString(String key, Option<String> value) {
        return add(key, value.map(new Function<String, JsValue>() {
            @Override
            public JsValue apply(String input) {
                return new JsString(input);
            }
        }));
    }
}