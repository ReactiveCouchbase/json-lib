package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.json.JsObject;
import org.reactivecouchbase.json.JsValue;
import org.reactivecouchbase.json.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.reactivecouchbase.json.Syntax.$;

public class ThrowableWriter implements Writer<Throwable> {

    private final boolean printStacks;

    public ThrowableWriter(boolean printStacks) {
        this.printStacks = printStacks;
    }

    @Override
    public JsValue write(Throwable value) {
        StackTraceElement[] els = value.getStackTrace();
        List<StackTraceElement> elements = new ArrayList<>();
        if (els != null && els.length != 0) {
            elements.addAll(Arrays.asList(els));
        }
        List<String> elementsAsStr = elements.stream().map(StackTraceElement::toString).collect(Collectors.toList());
        JsObject base = Json.obj(
                $("message", value.getMessage()),
                $("type", value.getClass().getName())
        );
        if (printStacks) {
            base = base.add($("stack", Json.arr(elementsAsStr)));
        }
        if (value.getCause() != null) {
            base = base.add(Json.obj(
                    $("cause", Json.toJson(value.getCause(), new ThrowableWriter(printStacks)))
            ));
        }
        return base;
    }
}