package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

public class ReaderConstraints {

    @Deprecated
    public static Reader<String> email(final Reader<String> reads) {
        return matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?", reads);
    }

    @Deprecated
    public static Reader<String> url(final Reader<String> reads) {
        return matches("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-z" +
                "A-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$", reads);
    }

    @Deprecated
    public static Reader<String> phone(final Reader<String> reads) {
        return matches("^([\\+][0-9]{1,3}([ \\.\\-]))?([\\(]{1}[0-9]{2,6}[\\)])?([0-9 \\.\\-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$", reads);
    }

    public static Reader<String> email() {
        return matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?");
    }

    public static Reader<String> url() {
        return matches("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-z" +
                "A-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$");
    }

    public static Reader<String> phone() {
        return matches("^([\\+][0-9]{1,3}([ \\.\\-]))?([\\(]{1}[0-9]{2,6}[\\)])?([0-9 \\.\\-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$");
    }

    @Deprecated
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
                }, new ValidationError("Value is to big."));
            }
        };
    }

    @Deprecated
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
                }, new ValidationError("Value is to small."));
            }
        };
    }

    @Deprecated
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
                }, new ValidationError("Value does not match."));
            }
        };
    }

    @Deprecated
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
                }, new ValidationError("Value does not verify."));
            }
        };
    }

    public static Reader<String> matches(final String pattern) {
        return new Reader<String>() {
            @Override
            public JsResult<String> read(JsValue value) {
                try {
                    String str = value.as(String.class);
                    if (str.matches(pattern)) {
                        return new JsSuccess<String>(str);
                    } else {
                        return new JsError<String>(new ValidationError("'" + str + "' does not match pattern '" + pattern + "'"));
                    }
                } catch (Exception e) {
                    return new JsError<String>(new ValidationError(e.getMessage()));
                }
            }
        };
    }

    public static Reader<Integer> min(final Integer min) {
        return new Reader<Integer>() {
            @Override
            public JsResult<Integer> read(JsValue value) {
                try {
                    Integer str = value.as(Integer.class);
                    if (str < min) {
                        return new JsError<Integer>(new ValidationError("'" + str + "' is below limit '" + min + "'"));
                    } else {
                        return new JsSuccess<Integer>(str);
                    }
                } catch (Exception e) {
                    return new JsError<Integer>(new ValidationError(e.getMessage()));
                }
            }
        };
    }

    public static Reader<Integer> max(final Integer max) {
        return new Reader<Integer>() {
            @Override
            public JsResult<Integer> read(JsValue value) {
                try {
                    Integer str = value.as(Integer.class);
                    if (str > max) {
                        return new JsError<Integer>(new ValidationError("'" + str + "' is over limit '" + max + "'"));
                    } else {
                        return new JsSuccess<Integer>(str);
                    }
                } catch (Exception e) {
                    return new JsError<Integer>(new ValidationError(e.getMessage()));
                }
            }
        };
    }

    public static <A> Reader<A> verify(final Predicate<A> p, final Reader<A> reads) {
        return new Reader<A>() {
            @Override
            public JsResult<A> read(JsValue value) {
                try {
                    JsResult<A> str = value.read(reads);
                    for (JsError<A> err : str.asError()) {
                        return err;
                    }
                    for (JsSuccess<A> success : str.asSuccess()) {
                        if (p.apply(success.get())) {
                            return new JsSuccess<A>(success.get());
                        } else {
                            return new JsError<A>(new ValidationError("Doesn't validate the predicate"));
                        }
                    }
                    throw new RuntimeException("Can't happen");
                } catch (Exception e) {
                    return new JsError<A>(new ValidationError(e.getMessage()));
                }
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

        public JsValue asJson() {
            return DefaultWriters.throwableAsJson(this);
        }

        public JsValue asJson(boolean stack) {
            return DefaultWriters.throwableAsJson(this, stack);
        }
    }
}