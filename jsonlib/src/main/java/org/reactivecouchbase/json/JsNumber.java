package org.reactivecouchbase.json;

import org.reactivecouchbase.functional.Option;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class JsNumber extends JsValue implements java.lang.Comparable<JsNumber> {
    public final BigDecimal value;

    public JsNumber(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = value;
    }

    public JsNumber(BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = new BigDecimal(value);
    }

    public JsNumber(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = BigDecimal.valueOf(value).setScale(0);
    }

    public JsNumber(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = BigDecimal.valueOf(value).setScale(0);
    }

    public JsNumber(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = BigDecimal.valueOf(value);
    }

    public JsNumber(Float value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = BigDecimal.valueOf(value);
    }

    public JsNumber(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        this.value = BigDecimal.valueOf(value).setScale(0);
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
    public int compareTo(JsNumber jsNumber) {
        return value.compareTo(jsNumber.value);
    }

    @Override
    String toJsonString() {
        return value.toPlainString();
    }

    @Override
    public String toString() {
        return "JsNumber(" + value.toPlainString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsNumber)) {
            return false;
        }
        JsNumber jsNumber = (JsNumber) o;
        if (!value.equals(jsNumber.value)) {
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
    public JsNumber cloneNode() {
        return new JsNumber(value);
    }
}