package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface Reader<T> {
    JsResult<T> read(JsValue value);

    default <B> Reader<B> map(final Function<T, B> f) {
        final Reader<T> self = this;
        return value -> self.read(value).map(f);
    }

    default <B> Reader<B> flatMap(final Function<T, Reader<B>> f) {
        final Reader<T> self = this;
        return value -> self.read(value).flatMap(t -> f.apply(t).read(value));
    }

    default Reader<T> filter(final Function<T, Boolean> f) {
        final Reader<T> self = this;
        return value -> self.read(value).filter(f);
    }

    default Reader<T> filterNot(final Function<T, Boolean> f) {
        final Reader<T> self = this;
        return value -> self.read(value).filterNot(f);
    }

    default Reader<T> orElse(final Reader<T> v) {
        final Reader<T> self = this;
        return value -> self.read(value).getOrElse(v.read(value));
    }

    default <B extends JsValue> Reader<T> compose(final Reader<B> v) {
        final Reader<T> self = this;
        return value -> {
            JsResult<B> res = v.read(value);
            for (JsError<B> t : res.asError()) {
                return new JsError<>(t.errors);
            }
            for (JsSuccess<B> t : res.asSuccess()) {
                return self.read(t.get());
            }
            throw new IllegalStateException("Should not happen");
        };
    }

    default Reader<T> and(final Reader<T> v) {
        final Reader<T> self = this;
        return value -> {
            JsResult<T> res1 = self.read(value);
            JsResult<T> res2 = v.read(value);
            if (res1.isSuccess() && res2.isSuccess()) {
                return res1;
            }
            List<Throwable> errors = new ArrayList<>();
            if (res1.isErrors()) {
                errors.addAll(res1.asError().get().errors);
            }
            if (res2.isErrors()) {
                errors.addAll(res2.asError().get().errors);
            }
            return new JsError<>(errors);
        };
    }
}