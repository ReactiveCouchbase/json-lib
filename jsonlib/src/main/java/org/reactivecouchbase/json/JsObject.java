package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.reactivecouchbase.common.Functionnal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsObject extends JsValue implements Iterable<Map.Entry<String, JsValue>> {
    public final Map<String, JsValue> values;

    public JsObject(Map<String, JsValue> values) {
        if (values == null) throw new IllegalArgumentException("Values can't be null !");
        this.values = values;
    }

    public JsObject() {
        this.values = new HashMap<String, JsValue>();
    }

    public JsObject merge(JsObject with) {
        if (with == null) throw new IllegalArgumentException("Value can't be null !");
        Map<String, JsValue> newValues = new HashMap<String, JsValue>();
        newValues.putAll(with.values);
        newValues.putAll(values);
        return new JsObject(newValues);
    }

    @Override
    public Iterator<Map.Entry<String, JsValue>> iterator() {
        return values.entrySet().iterator();
    }

    public JsObject deepMerge(JsObject with) {
        if (with == null) throw new IllegalArgumentException("Value can't be null !");
        Map<String, JsValue> newValues = new HashMap<String, JsValue>();
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
        if (jsObject == null) return new JsObject(values);
        Map<String, JsValue> newValues = values;
        newValues.putAll(jsObject.values);
        return new JsObject(newValues);
    }

    public JsObject remove(String field) {
        if (field == null) return new JsObject(values);
        values.remove(field);
        return new JsObject(values);
    }

    public JsValue field(String field) {
        if (field == null) return Syntax.JSUNDEFINED_INSTANCE;
        JsValue value = values.get(field);
        if (value == null) return Syntax.JSUNDEFINED_INSTANCE;
        return value;
    }

    public Functionnal.Option<JsValue> fieldAsOpt(String field) {
        if (field == null) return Functionnal.Option.none();
        JsValue val = values.get(field);
        if (val == null) {
            return Functionnal.Option.none();
        }
        return Functionnal.Option.some(val);
    }

    public List<JsValue> fields(String fieldName) {
        if (fieldName == null) return Collections.emptyList();
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

    public String toString() {
        return "JsObject(" + toJsonPairString() + ")";
    }

    String toJsonPairString() {
        return Joiner.on(",").join(Iterables.transform(values.entrySet(), new Function<Map.Entry<String, JsValue>, String>() {
            public String apply(Map.Entry<String, JsValue> entry) {
                return "\"" + entry.getKey() + "\":" + entry.getValue().toJsonString();
            }
        }));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsObject)) return false;
        JsObject object = (JsObject) o;
        if (!values.equals(object.values)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public Boolean exists(String field) {
        return values.containsKey(field);
    }

    public JsObject mapProperties(Function<Functionnal.Tuple<String, JsValue>, JsValue> block) {
        Map<String, JsValue> resulting = new HashMap<String, JsValue>();
        for (Map.Entry<String, JsValue> entry : values.entrySet()) {
            JsValue tuple = block.apply(new Functionnal.Tuple<String, JsValue>(entry.getKey(), entry.getValue()));
            resulting.put(entry.getKey(), tuple);
        }
        return new JsObject(resulting);
    }

    public <T> Map<String, T> mapPropertiesWith(Reader<T> reader) {
        Map<String, T> resultMap = new HashMap<String, T>();
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
        Map<String, T> resultMap = new HashMap<String, T>();
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
}