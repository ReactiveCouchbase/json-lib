package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.reactivecouchbase.common.Functionnal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DefaultWriters {

    public static <T> CWriter<List<T>> seq(final Writer<T> writer) {
        return new CWriter<List<T>>() {
            @Override
            public JsValue write(List<T> value) {
                JsArray array = Json.arr();
                for (T val : value) {
                    array = array.addElement(writer.write(val));
                }
                return array;
            }
        };
    }

    public static final CWriter<Integer> INT_WRITER = new CWriter<Integer>() {
        @Override
        public JsValue write(Integer value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<Short> SHORT_WRITER = new CWriter<Short>() {
        @Override
        public JsValue write(Short value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<Long> LONG_WRITER = new CWriter<Long>() {
        @Override
        public JsValue write(Long value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<Float> FLOAT_WRITER = new CWriter<Float>() {
        @Override
        public JsValue write(Float value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<Double> DOUBLE_WRITER = new CWriter<Double>() {
        @Override
        public JsValue write(Double value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<BigDecimal> BIGDEC_WRITER = new CWriter<BigDecimal>() {
        @Override
        public JsValue write(BigDecimal value) {
            return new JsNumber(value);
        }
    };

    public static final CWriter<Boolean> BOOLEAN_WRITER = new CWriter<Boolean>() {
        @Override
        public JsValue write(Boolean value) {
            return new JsBoolean(value);
        }
    };

    public static final CWriter<String> STRING_WRITER = new CWriter<String>() {
        @Override
        public JsValue write(String value) {
            return new JsString(value);
        }
    };

    public static <T> CWriter<Iterable<T>> iterable(final Writer<T> writer) {
        return new CWriter<Iterable<T>>() {
            @Override
            public JsValue write(Iterable<T> value) {
                JsArray array = Json.arr();
                for (T val : value) {
                    array = array.addElement(writer.write(val));
                }
                return array;
            }
        };
    }

    public static final CWriter<JsValue> JSVALUE_WRITER = new CWriter<JsValue>() {
        @Override
        public JsValue write(JsValue value) {
            return value;
        }
    };

    public static <T> CWriter<Functionnal.Option<T>> option(final Writer<T> writer) {
        return new CWriter<Functionnal.Option<T>>() {
            @Override
            public JsValue write(Functionnal.Option<T> value) {
                for (T t : value) {
                    return writer.write(t);
                }
                return Syntax.nill();
            }
        };
    }

    public static final CWriter<Date> DATE_WRITER = new CWriter<Date>() {
        @Override
        public JsValue write(Date value) {
            return new JsNumber(value.getTime());
        }
    };

    public static final CWriter<DateTime> DATETIME_WRITER = new CWriter<DateTime>() {
        @Override
        public JsValue write(DateTime value) {
            return new JsString(value.toString());
        }
    };

    public static CWriter<Date> date(final String pattern) {
        return new CWriter<Date>() {
            @Override
            public JsValue write(Date value) {
                return  new JsString(new java.text.SimpleDateFormat(pattern).format(value));
            }
        };
    }

    public static CWriter<DateTime> datetime(final String pattern) {
        return new CWriter<DateTime>() {
            @Override
            public JsValue write(DateTime value) {
                return  new JsString(value.toString(pattern));
            }
        };
    }

    public static JsValue throwableAsJson(Throwable t) {
        return Json.toJson(t, new ThrowableWriter(true));
    }

    public static JsObject throwableAsJsObject(Throwable t) {
        return Json.toJson(t, new ThrowableWriter(true)).as(JsObject.class);
    }

    public static JsValue throwableAsJson(Throwable t, boolean stack) {
        return Json.toJson(t, new ThrowableWriter(stack));
    }

    public static JsObject throwableAsJsObject(Throwable t, boolean stack) {
        return Json.toJson(t, new ThrowableWriter(stack)).as(JsObject.class);
    }

    public static class ThrowableWriter implements Writer<Throwable> {
        private final boolean printStacks;

        public ThrowableWriter(boolean printStacks) {
            this.printStacks = printStacks;
        }

        @Override
        public JsValue write(Throwable value) {
            StackTraceElement[] els = value.getStackTrace();
            List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
            if (els != null && els.length != 0) {
                elements.addAll(Arrays.asList(els));
            }
            List<String> elementsAsStr = Lists.transform(elements, new Function<StackTraceElement, String>() {
                @Override
                public String apply(StackTraceElement input) {
                    return input.toString();
                }
            });
            JsObject base = Json.obj(
                    Syntax.$("message", value.getMessage()),
                    Syntax.$("type", value.getClass().getName())
            );
            if (printStacks) {
                base = base.add(Syntax.$("stack", Json.arr(elementsAsStr)));
            }
            if (value.getCause() != null) {
                base = base.add(Json.obj(
                        Syntax.$("cause", Json.toJson(value.getCause(), new ThrowableWriter(printStacks)))
                ));
            }
            return base;
        }
    }
}
