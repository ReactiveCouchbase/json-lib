package org.reactivecouchbase.json;

import org.reactivecouchbase.functional.Option;
import org.reactivecouchbase.json.mapping.DefaultReaders;
import org.reactivecouchbase.json.mapping.Format;
import org.reactivecouchbase.json.mapping.JsError;
import org.reactivecouchbase.json.mapping.JsResult;
import org.reactivecouchbase.json.mapping.Reader;
import org.reactivecouchbase.validation.Rule;
import org.reactivecouchbase.validation.Validation;
import org.reactivecouchbase.validation.ValidationError;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public abstract class JsValue implements Serializable {
    public <T extends JsValue> Iterable<T> extractAs(Class<T> clazz) {
        if (is(clazz)) {
            return Collections.singletonList(clazz.cast(this));
        }
        return Collections.emptyList();
    }

    public <T extends JsValue> boolean is(Class<T> clazz) {
        return clazz.isAssignableFrom(this.getClass());
    }

    abstract String toJsonString();

    public <T> T as(Reader<T> reader) {
        return reader.read(this).getOpt().get();
    }

    public <T> T as(Format<T> reader) {
        return reader.read(this).getOpt().get();
    }

    public <T> T as(Class<T> clazz, Reader<T> reader) {
        return reader.read(this).getOpt().get();
    }

    public <T> T as(Class<T> clazz) {
        return asOpt(clazz).get();
    }

    public JsArray asArray() {
        return this.as(JsArray.class);
    }

    public JsObject asObject() {
        return this.as(JsObject.class);
    }

    public Boolean asBoolean() {
        return this.as(Boolean.class);
    }

    public Double asDouble() {
        return this.as(Double.class);
    }

    public Integer asInteger() {
        return this.as(Integer.class);
    }

    public Long asLong() {
        return this.as(Long.class);
    }

    public BigDecimal asBigDecimal() {
        return this.as(BigDecimal.class);
    }

    public String asString() {
        return this.as(String.class);
    }

    public Option<JsArray> asOptArray() {
        return this.asOpt(JsArray.class);
    }

    public Option<JsObject> asOptObject() {
        return this.asOpt(JsObject.class);
    }

    public Option<Boolean> asOptBoolean() {
        return this.asOpt(Boolean.class);
    }

    public Option<Double> asOptDouble() {
        return this.asOpt(Double.class);
    }

    public Option<Integer> asOptInteger() {
        return this.asOpt(Integer.class);
    }

    public Option<Long> asOptLong() {
        return this.asOpt(Long.class);
    }

    public Option<BigDecimal> asOptBigDecimal() {
        return this.asOpt(BigDecimal.class);
    }

    public Option<String> asOptString() {
        return this.asOpt(String.class);
    }

    public JsArray array(String field) {
        return this.field(field).as(JsArray.class);
    }

    public JsObject object(String field) {
        return this.field(field).as(JsObject.class);
    }

    public String string(String field) {
        return this.field(field).as(String.class);
    }

    public Integer integer(String field) {
        return this.field(field).as(Integer.class);
    }

    public Double dbl(String field) {
        return this.field(field).as(Double.class);
    }

    public BigDecimal bigDecimal(String field) {
        return this.field(field).as(BigDecimal.class);
    }

    public Long lng(String field) {
        return this.field(field).as(Long.class);
    }

    public Boolean bool(String field) {
        return this.field(field).as(Boolean.class);
    }

    public <T> Option<T> asOpt(Reader<T> reader) {
        return reader.read(this).getOpt();
    }

    public <T> Option<T> asOpt(Format<T> reader) {
        return reader.read(this).getOpt();
    }

    public <T> Option<T> asOpt(Class<T> clazz, Reader<T> reader) {
        return reader.read(this).getOpt();
    }

    public <T> Option<T> asOpt(Class<T> clazz) {
        for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
            return reader.read(this).getOpt();
        }
        return Option.none();
    }

    public <T> JsResult<T> read(Class<T> clazz) {
        for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
            return reader.read(this);
        }
        return new JsError<T>(new IllegalStateException("Cannot find reader for type " + clazz.getName()));
    }

    public <T> JsResult<T> read(Reader<T> reader) {
        return reader.read(this);
    }

    public <T> JsResult<T> validate(Reader<T> reader) {
        return reader.read(this);
    }

    public <T> Validation<T, ValidationError> validate(Rule<JsValue, T> rule) {
        return rule.validate(this);
    }

    public <T> Validation<T, ValidationError> read(Rule<JsValue, T> rule) {
        return rule.validate(this);
    }

    public <A extends JsValue> JsResult<A> transform(Reader<A> reader) {
        return reader.read(this);
    }

    public Boolean exists(String field) {
        return false;
    }

    public JsValue querySelector(String query) {
        return querySelectorOpt(query).getOrElse(JsUndefined.JSUNDEFINED_INSTANCE);
    }

    private static final Pattern jsonFieldArraySelector = Pattern.compile("(.+)\\[(\\d)+\\]");
    private static final Pattern jsonArraySelector = Pattern.compile("\\[(\\d)+\\]");
    private static final Pattern dotSplitter = Pattern.compile("\\.");
    private static final Pattern squareBracketSplitter = Pattern.compile("\\[");

    public Option<JsValue> querySelectorOpt(String query) {
        JsValue currentValue = this;
        try {
            String[] partsArray = dotSplitter.split(query);
            List<String> parts = partsArray == null ? new ArrayList<String>() : Arrays.asList(partsArray);
            for (String part : parts) {
                if (jsonFieldArraySelector.matcher(part).matches()) {
                    String[] subParts = squareBracketSplitter.split(part);
                    String field = subParts[0];
                    Integer index = Integer.valueOf(subParts[1].replace("]", ""));
                    currentValue = currentValue.field(field).asArray().get(index);
                } else if (part.startsWith("[") && jsonArraySelector.matcher(part).matches()) {
                    Integer index = Integer.valueOf(part.replace("[", "").replace("]", ""));
                    currentValue = currentValue.asArray().get(index);
                } else {
                    currentValue = currentValue.field(part);
                }
            }
        } catch (Exception e) {
            return Option.none();
        }
        return Option.apply(currentValue);
    }

    public JsValue field(String field) {
        return JsUndefined.JSUNDEFINED_INSTANCE;
    }

    public Option<JsValue> fieldAsOpt(String field) {
        return Option.none();
    }

    public List<JsValue> fields(String fieldName) {
        return new ArrayList<>();
    }

    public JsValue get(int idx) {
        return JsUndefined.JSUNDEFINED_INSTANCE;
    }

    public abstract boolean deepEquals(Object o);

    public abstract JsValue cloneNode();

    public String stringify() {
        return Json.stringify(this);
    }

    public String stringify(boolean pretty) {
        return pretty ? Json.prettyPrint(this) : Json.stringify(this);
    }

    public String pretty() {
        return Json.prettyPrint(this);
    }
}