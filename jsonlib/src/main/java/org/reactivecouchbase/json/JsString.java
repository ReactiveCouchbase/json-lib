package org.reactivecouchbase.json;

import com.fasterxml.jackson.databind.node.TextNode;
import org.reactivecouchbase.functional.Option;

public class JsString extends JsValue implements java.lang.Comparable<JsString> {
    public final String value;

    public JsString(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }

    @Override
    public JsValue querySelector(String query) {
        return JsUndefined.JSUNDEFINED_INSTANCE;
    }

    @Override
    public Option<JsValue> querySelectorOpt(String query) {
        return Option.none();
    }

    @Override
    public int compareTo(JsString jsString) {
        return value.compareTo(jsString.value);
    }

    @Override
    String toJsonString() {
        return new TextNode(value).toString();
    }

    @Override
    public String toString() {
        return "JsString(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsString)) {
            return false;
        }

        JsString jsString = (JsString) o;

        if (!value.equals(jsString.value)) {
            return false;
        }

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

    @Override
    public JsString cloneNode() {
        return new JsString(value);
    }
}