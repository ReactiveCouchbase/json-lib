package org.reactivecouchbase.json;

public class JsNull extends JsValue {

    String toJsonString() {
        return "null";
    }

    public String toString() {
        return "JsNull()";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsNull)) return false;
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
        return Syntax.JSNULL_INSTANCE;
    }
}