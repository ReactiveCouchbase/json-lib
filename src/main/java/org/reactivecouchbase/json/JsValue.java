package org.reactivecouchbase.json;

import com.google.common.collect.Lists;
import org.reactivecouchbase.common.Functionnal;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public abstract class JsValue implements Serializable {
    public <T extends JsValue> Iterable<T> extractAs(Class<T> clazz) {
        if (is(clazz)) {
            return Collections.singletonList(clazz.cast(this));
        }
        return Collections.emptyList();
    }
    public <T extends JsValue> boolean is(Class<T> clazz) {
        return clazz.isAssignableFrom(this.getClass());
    }
    public abstract String toJsonString();
    public <T> T as(Class<T> clazz, Reader<T> reader) {
        return reader.read(this).getOpt().get();
    }
    public <T> T as(Class<T> clazz) {
        return asOpt(clazz).get();
    }
    public <T> Functionnal.Option<T> asOpt(Class<T> clazz, Reader<T> reader) {
        return reader.read(this).getOpt();
    }
    public <T> Functionnal.Option<T> asOpt(Class<T> clazz) {
        for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
            return reader.read(this).getOpt();
        }
        return Functionnal.Option.none();
    }
    public <T> JsResult<T> read(Class<T> clazz) {
        for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
            return reader.read(this);
        }
        return new JsError<T>(new IllegalStateException("Cannot find reader for type " + clazz.getName()));
    }
    public <T> JsResult<T> read(Reader<T> reader) {
        return reader.read(this);
    }
    public <T> JsResult<T> validate(Reader<T> reader) {
        return reader.read(this);
    }
    public <A extends JsValue> JsResult<A> transform(Reader<A> reader) {
        return reader.read(this);
    }
    public Boolean exists(String field) {
        return false;
    }
    public JsValue field(String field) {
        return JsonLib.JSUNDEFINED_INSTANCE;
    }
    public Functionnal.Option<JsValue> fieldAsOpt(String field) {
        return Functionnal.Option.none();
    }
    public List<JsValue> fields(String fieldName) {
        return Lists.newArrayList();
    }
    public JsValue _(String field) {
        return this.field(field);
    }
    public List<JsValue> __(String fieldName) {
        return this.fields(fieldName);
    }
    public JsValue get(int idx) {
        return JsonLib.JSUNDEFINED_INSTANCE;
    }
}