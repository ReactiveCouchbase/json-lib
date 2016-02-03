package org.reactivecouchbase.json;

import org.reactivecouchbase.functional.Option;

public class JsNull extends JsValue {
    static final JsNull JSNULL_INSTANCE = new JsNull();

    @Override
    public JsValue querySelector(String query) {
        return JsUndefined.JSUNDEFINED_INSTANCE;
    }

    @Override
    public Option<JsValue> querySelectorOpt(String query) {
        return Option.none();
    }

    @Override
    String toJsonString() {
        return "null";
    }

    @Override
    public String toString() {
        return "JsNull()";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsNull)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean deepEquals(Object o) {
        return equals(o);
    }

    @Override
    public JsNull cloneNode() {
        return JSNULL_INSTANCE;
    }
}