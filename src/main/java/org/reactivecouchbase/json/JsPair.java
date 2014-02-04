package org.reactivecouchbase.json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsPair extends JsObject {
    public JsPair(String name, JsValue value) {
        super(JsonLib.asMap(name, value));
    }
    public JsPair(String name, Long value) {
        super(JsonLib.asMap(name, new JsNumber(value)));
    }
    public JsPair(String name, Integer value) {
        super(JsonLib.asMap(name, new JsNumber(value)));
    }
    public JsPair(String name, Double value) {
        super(JsonLib.asMap(name, new JsNumber(value)));
    }
    public JsPair(String name, BigDecimal value) {
        super(JsonLib.asMap(name, new JsNumber(value)));
    }
    public JsPair(String name, BigInteger value) {
        super(JsonLib.asMap(name, new JsNumber(value)));
    }
    public JsPair(String name) {
        super(JsonLib.asMap(name, JsonLib.JSNULL_INSTANCE));
    }
    public JsPair(String name, String value) {
        super(JsonLib.asMap(name, new JsString(value)));
    }
    public JsPair(String name, Boolean value) {
        super(JsonLib.asMap(name, new JsBoolean(value)));
    }
    public String toString() {
        return super.toString();
    }
}
