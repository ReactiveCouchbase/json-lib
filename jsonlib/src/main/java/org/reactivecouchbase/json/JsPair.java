package org.reactivecouchbase.json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsPair extends JsObject {
    public JsPair(String name, JsValue value) {
        super(Syntax.asMap(name, value));
    }

    public JsPair(String name, Long value) {
        super(Syntax.asMap(name, new JsNumber(value)));
    }

    public JsPair(String name, Integer value) {
        super(Syntax.asMap(name, new JsNumber(value)));
    }

    public JsPair(String name, Double value) {
        super(Syntax.asMap(name, new JsNumber(value)));
    }

    public JsPair(String name, BigDecimal value) {
        super(Syntax.asMap(name, new JsNumber(value)));
    }

    public JsPair(String name, BigInteger value) {
        super(Syntax.asMap(name, new JsNumber(value)));
    }

    public JsPair(String name) {
        super(Syntax.asMap(name, JsNull.JSNULL_INSTANCE));
    }

    public JsPair(String name, String value) {
        super(Syntax.asMap(name, new JsString(value)));
    }

    public JsPair(String name, Boolean value) {
        super(Syntax.asMap(name, new JsBoolean(value)));
    }
}