package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultWriters {

    public static <T> Writer<List<T>> seq(final Writer<T> writer) {
        return new Writer<List<T>>() {
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
