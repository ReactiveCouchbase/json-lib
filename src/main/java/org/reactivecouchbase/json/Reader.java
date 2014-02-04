package org.reactivecouchbase.json;

public interface Reader<T>  {
    public JsResult<T> read(JsValue value);
}