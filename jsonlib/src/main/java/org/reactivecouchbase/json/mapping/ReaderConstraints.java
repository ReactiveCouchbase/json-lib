package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.exceptions.ValidationError;

import java.util.function.Predicate;

public class ReaderConstraints {

    public static final String EMAIL_PATTERN = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?";
    public static final String URL_PATTERN = "^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$";
    public static final String PHONE_PATTERN = "^([\\+][0-9]{1,3}([ \\.\\-]))?([\\(]{1}[0-9]{2,6}[\\)])?([0-9 \\.\\-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$";

    private ReaderConstraints() {
    }

    public static Reader<String> email() {
        return matches(EMAIL_PATTERN);
    }

    public static Reader<String> url() {
        return matches(URL_PATTERN);
    }

    public static Reader<String> phone() {
        return matches(PHONE_PATTERN);
    }

    public static Reader<String> matches(final String pattern) {
        return value -> {
            try {
                String str = value.as(String.class);
                if (str.matches(pattern)) {
                    return new JsSuccess<>(str);
                } else {
                    return new JsError<>(new ValidationError("'" + str + "' does not match pattern '" + pattern + "'"));
                }
            } catch (Exception e) {
                return new JsError<>(new ValidationError(e.getMessage()));
            }
        };
    }

    public static Reader<Integer> min(final Integer min) {
        return value -> {
            try {
                Integer str = value.as(Integer.class);
                if (str < min) {
                    return new JsError<>(new ValidationError("'" + str + "' is below limit '" + min + "'"));
                } else {
                    return new JsSuccess<>(str);
                }
            } catch (Exception e) {
                return new JsError<>(new ValidationError(e.getMessage()));
            }
        };
    }

    public static Reader<Integer> max(final Integer max) {
        return value -> {
            try {
                Integer str = value.as(Integer.class);
                if (str > max) {
                    return new JsError<>(new ValidationError("'" + str + "' is over limit '" + max + "'"));
                } else {
                    return new JsSuccess<>(str);
                }
            } catch (Exception e) {
                return new JsError<>(new ValidationError(e.getMessage()));
            }
        };
    }

    public static <A> Reader<A> verify(final Predicate<A> p, final Reader<A> reads) {
        return value -> {
            try {
                JsResult<A> str = value.read(reads);
                for (JsError<A> err : str.asError()) {
                    return err;
                }
                for (JsSuccess<A> success : str.asSuccess()) {
                    if (p.test(success.get())) {
                        return new JsSuccess<>(success.get());
                    } else {
                        return new JsError<>(new ValidationError("Doesn't validate the predicate"));
                    }
                }
                throw new RuntimeException("Can't happen");
            } catch (Exception e) {
                return new JsError<>(new ValidationError(e.getMessage()));
            }
        };
    }
}