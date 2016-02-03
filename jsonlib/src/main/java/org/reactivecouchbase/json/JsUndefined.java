package org.reactivecouchbase.json;

import org.reactivecouchbase.functional.Option;

public class JsUndefined extends JsValue {

    static final JsUndefined JSUNDEFINED_INSTANCE = new JsUndefined();

    @Override
    String toJsonString() {
        return "undefined";
    }

    @Override
    public JsValue querySelector(String query) {
        return JSUNDEFINED_INSTANCE;
    }

    @Override
    public Option<JsValue> querySelectorOpt(String query) {
        return Option.none();
    }

    @Override
    public String toString() {
        return "JsUndefined()";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsUndefined)) {
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
    public JsUndefined cloneNode() {
        return JSUNDEFINED_INSTANCE;
    }


}