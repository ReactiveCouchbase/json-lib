package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsArray extends JsValue implements Iterable<JsValue> {

    public final List<JsValue> values;

    public JsArray(List<JsValue> values) {
        if (values == null) throw new IllegalArgumentException("Values can't be null !");
        this.values = values;
    }

    public JsArray() {
        this.values = new ArrayList<JsValue>();
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

    public boolean contains(JsValue value) {
        return values.contains(value);
    }

    @Override
    public Iterator<JsValue> iterator() {
        return values.iterator();
    }

    public JsValue get(int idx) {
        try {
            return values.get(idx);
        } catch (Exception e) {
            return Syntax.JSUNDEFINED_INSTANCE;
        }
    }

    public JsArray append(JsArray arr) {
        if (arr == null) return new JsArray(values);
        List<JsValue> vals = values;
        vals.addAll(arr.values);
        return new JsArray(vals);
    }

    public JsArray preprend(JsArray arr) {
        if (arr == null) return new JsArray(values);
        List<JsValue> vals = values;
        vals.addAll(0, arr.values);
        return new JsArray(vals);
    }

    public JsArray addElement(JsValue arr) {
        if (arr == null) return new JsArray(values);
        List<JsValue> vals = values;
        vals.add(arr);
        return new JsArray(vals);
    }

    public JsArray preprendElement(JsValue arr) {
        if (arr == null) return new JsArray(values);
        List<JsValue> vals = values;
        vals.add(0, arr);
        return new JsArray(vals);
    }

    public JsArray map(Function<JsValue, JsValue> map) {
        return new JsArray(Lists.newArrayList(Lists.transform(values, map)));
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
        return new JsArray(Lists.newArrayList(Iterables.filter(values, predicate)));
    }

    public JsArray filterNot(final Predicate<JsValue> predicate) {
        Predicate<JsValue> p = new Predicate<JsValue>() {
            public boolean apply(JsValue jsValue) {
                return !predicate.apply(jsValue);
            }
        };
        return new JsArray(Lists.newArrayList(Iterables.filter(values, p)));
    }

    String toJsonString() {
        return "[" + Joiner.on(",").join(Lists.transform(values, new Function<JsValue, String>() {
            public String apply(JsValue jsValue) {
                return jsValue.toJsonString();
            }
        })) + "]";
    }

    public String toString() {
        return "JsArray[" + Joiner.on(", ").join(values) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsArray)) return false;
        JsArray jsArray = (JsArray) o;
        if (!values.equals(jsArray.values)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean deepEquals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsArray)) return false;
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
                if (!v1.deepEquals(v2)) return false;
            }
        }
        return true;
    }
}