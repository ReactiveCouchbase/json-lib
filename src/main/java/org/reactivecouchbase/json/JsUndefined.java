package org.reactivecouchbase.json;

public class JsUndefined extends JsValue {

    String toJsonString() {
        return "undefined";
    }

    public String toString() {
        return "JsUndefined()";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsUndefined)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 42;
    }
}