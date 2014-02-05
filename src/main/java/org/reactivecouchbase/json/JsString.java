package org.reactivecouchbase.json;

import com.fasterxml.jackson.databind.node.TextNode;

public class JsString extends JsValue implements java.lang.Comparable<JsString> {
    public final String value;
    public JsString(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }

    @Override
    public int compareTo(JsString jsString) {
        return value.compareTo(jsString.value);
    }

    String toJsonString() {
        return new TextNode(value).toString();
    }
    public String toString() {
        return "JsString(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsString)) return false;

        JsString jsString = (JsString) o;

        if (!value.equals(jsString.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}