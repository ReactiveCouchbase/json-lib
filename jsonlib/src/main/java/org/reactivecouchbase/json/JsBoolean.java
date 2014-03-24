package org.reactivecouchbase.json;

public class JsBoolean extends JsValue implements java.lang.Comparable<JsBoolean> {

    public final Boolean value;

    public JsBoolean(Boolean value) {
        if (value == null) throw new IllegalArgumentException("Value can't be null !");
        this.value = value;
    }

    @Override
    public int compareTo(JsBoolean aBoolean) {
        return value.compareTo(aBoolean.value);
    }

    String toJsonString() {
        if (value == null) {
            return Boolean.FALSE.toString();
        }
        return value.toString();
    }

    public String toString() {
        return "JsBoolean(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsBoolean)) return false;
        JsBoolean jsBoolean = (JsBoolean) o;
        if (!value.equals(jsBoolean.value)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean deepEquals(Object o) {
        return equals(o);
    }
}