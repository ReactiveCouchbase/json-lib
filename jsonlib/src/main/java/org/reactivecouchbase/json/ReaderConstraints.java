package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

public class ReaderConstraints {

    public static Reader<String> email(final Reader<String> reads) {
        return matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?", reads);
    }

    public static Reader<String> url(final Reader<String> reads) {
        return matches("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-z" +
                "A-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$", reads);
    }

    public static Reader<String> phone(final Reader<String> reads) {
        return matches("^([\\+][0-9]{1,3}([ \\.\\-]))?([\\(]{1}[0-9]{2,6}[\\)])?([0-9 \\.\\-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$", reads);
    }

    public static Reader<Integer> max(final int max, final Reader<Integer> reads) {
        return new Reader<Integer>() {
            @Override
            public JsResult<Integer> read(JsValue value) {
                JsResult<Integer> jsr = reads.read(value);
                return jsr.filterNot(new Function<Integer, Boolean>() {
                    @Override
                    public Boolean apply(Integer val) {
                        return val > max;
                    }
                }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value is to big.")));
            }
        };
    }

    public static Reader<Integer> min(final int min, final Reader<Integer> reads) {
        return new Reader<Integer>() {
            @Override
            public JsResult<Integer> read(JsValue value) {
                JsResult<Integer> jsr = reads.read(value);
                return jsr.filterNot(new Function<Integer, Boolean>() {
                    @Override
                    public Boolean apply(Integer val) {
                        return val < min;
                    }
                }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value is to small.")));
            }
        };
    }

    public static Reader<String> matches(final String pattern, final Reader<String> reads) {
        return new Reader<String>() {
            @Override
            public JsResult<String> read(JsValue value) {
                JsResult<String> jsr = reads.read(value);
                return jsr.filter(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String val) {
                        return val.matches(pattern);
                    }
                }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value does not match.")));
            }
        };
    }

    public static <A> Reader<A> verifying(final Predicate<A> p, final Reader<A> reads) {
        return new Reader<A>() {
            @Override
            public JsResult<A> read(JsValue value) {
                JsResult<A> jsr = reads.read(value);
                return jsr.filter(new Function<A, Boolean>() {
                    @Override
                    public Boolean apply(A val) {
                        return p.apply(val);
                    }
                }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value does not verify.")));
            }
        };
    }

    public static class JsWrappedException extends RuntimeException {
        private final List<Exception> exceptions;

        public JsWrappedException(List<Exception> exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        public void setStackTrace(StackTraceElement[] stackTraceElements) {
            throw new IllegalAccessError("Can set stackstrace");
        }

        @Override
        public String getMessage() {
            return Joiner.on(" | ").join(Iterables.transform(exceptions, new Function<Exception, String>() {
                @Override
                public String apply(java.lang.Exception e) {
                    return e.getMessage();
                }
            }));
        }

        @Override
        public String getLocalizedMessage() {
            return Joiner.on(" | ").join(Iterables.transform(exceptions, new Function<Exception, String>() {
                @Override
                public String apply(java.lang.Exception e) {
                    return e.getLocalizedMessage();
                }
            }));
        }

        @Override
        public String toString() {
            return getMessage();
        }

        @Override
        public void printStackTrace() {
            for (Exception e : exceptions) {
                e.printStackTrace();
            }
        }
    }
}