package org.reactivecouchbase.json;

import org.reactivecouchbase.common.Throwables;
import org.reactivecouchbase.json.mapping.JsResult;
import org.reactivecouchbase.json.mapping.Reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JsArray extends JsValue implements Iterable<JsValue> {
    public final List<JsValue> values;

    public JsArray(List<JsValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null !");
        }
        this.values = Collections.unmodifiableList(values);
    }

    public JsArray() {
        this.values = Collections.unmodifiableList(new ArrayList<>());
    }

    public boolean contains(JsValue value) {
        return values.contains(value);
    }

    @Override
    public Iterator<JsValue> iterator() {
        return values.iterator();
    }

    @Override
    public JsValue get(int idx) {
        try {
            return values.get(idx);
        } catch (Exception e) {
            return JsUndefined.JSUNDEFINED_INSTANCE;
        }
    }

    public JsArray append(JsArray arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        List<JsValue> vals = new ArrayList<>();
        vals.addAll(values);
        vals.addAll(arr.values);
        return new JsArray(vals);
    }

    public JsArray preprend(JsArray arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        List<JsValue> vals = new ArrayList<>();
        vals.addAll(values);
        vals.addAll(0, arr.values);
        return new JsArray(vals);
    }

    public JsArray addElement(JsValue arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        List<JsValue> vals = new ArrayList<>();
        vals.addAll(values);
        vals.add(arr);
        return new JsArray(vals);
    }

    public JsArray preprendElement(JsValue arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        List<JsValue> vals = new ArrayList<>();
        vals.addAll(values);
        vals.add(0, arr);
        return new JsArray(vals);
    }

    public JsArray map(Function<JsValue, JsValue> map) {
        return new JsArray(values.stream().map(map).collect(Collectors.toList()));
    }

    public <T> List<T> mapWith(Reader<T> reader) {
        List<T> resultList = new ArrayList<T>();
        for (JsValue value : this.values) {
            JsResult<T> result = value.read(reader);
            if (result.hasErrors()) {
                throw Throwables.propagate(result.asError().get().firstError());
            }
            resultList.add(result.get());
        }
        return resultList;
    }

    public <T> List<T> mapWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
        List<T> resultList = new ArrayList<T>();
        for (JsValue value : this.values) {
            T v = null;
            JsResult<T> result = value.read(reader);
            if (result.hasErrors()) {
                v = onError.apply(result);
            } else {
                v = result.get();
            }
            resultList.add(v);
        }
        return resultList;
    }

    public JsArray filter(Predicate<JsValue> predicate) {
        return new JsArray(values.stream().filter(predicate).collect(Collectors.toList()));
    }

    public JsArray filterNot(final Predicate<JsValue> predicate) {
        return new JsArray(values.stream().filter(predicate.negate()).collect(Collectors.toList()));
    }

    @Override
    String toJsonString() {
        return "[" + values.stream().map(JsValue::toJsonString).collect(Collectors.joining(",")) + "]";
    }

    @Override
    public String toString() {
        return "JsArray[" + values.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsArray)) {
            return false;
        }
        JsArray jsArray = (JsArray) o;
        if (!values.equals(jsArray.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deepEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsArray)) {
            return false;
        }
        JsArray jsArray = (JsArray) o;
        for (int i = 0; i < size(); i++) {
            JsValue v1 = get(i);
            JsValue v2 = jsArray.get(i);
            if (v1 == null && v2 == null) {
                // we're good
            } else if (v1 != null && v2 == null) {
                return false;
            } else if (v1 == null && v2 != null) {
                return false;
            } else {
                if (!v1.deepEquals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public JsArray cloneNode() {
        return new JsArray(new ArrayList<>(values));
    }
}