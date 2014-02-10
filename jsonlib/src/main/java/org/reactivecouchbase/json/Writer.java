package org.reactivecouchbase.json;

public interface Writer<T>  {
    public JsValue write(T value);
}