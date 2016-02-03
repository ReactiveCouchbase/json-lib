package org.reactivecouchbase.json.mapping;

import org.reactivecouchbase.common.Throwables;
import org.reactivecouchbase.functional.Option;
import org.reactivecouchbase.json.JsArray;
import org.reactivecouchbase.json.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsError<T> extends JsResult<T> {

    public final List<Throwable> errors;

    @Override
    public T getValueOrElse(T result) {
        return result;
    }

    @Override
    public T getValueOrNull() {
        return null;
    }

    @Override
    public T getValueOrElse(Throwable result) {
        throw Throwables.propagate(result);
    }

    @Override
    public Option<JsError<T>> asError() {
        return Option.some(this);
    }

    @Override
    public Option<JsSuccess<T>> asSuccess() {
        return Option.none();
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean isErrors() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public int countErrors() {
        return errors.size();
    }

    @Override
    public T orError(Throwable t) {
        throw Throwables.propagate(t);
    }

    @Override
    public T get() {
        throw new IllegalStateException("No value");
    }

    @Override
    public Option<T> getOpt() {
        return Option.none();
    }

    @Override
    public JsResult<T> getOrElse(JsResult<T> result) {
        return result;
    }

    @Override
    public <B> JsResult<B> map(Function<T, B> map) {
        return new JsError<>(errors);
    }

    @Override
    public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, List<Throwable> errs) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && predicate.apply(val.get())) {
            thrs.addAll(errs);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, List<Throwable> errs) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && !predicate.apply(val.get())) {
            thrs.addAll(errs);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && predicate.apply(val.get())) {
            thrs.add(error);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && !predicate.apply(val.get())) {
            thrs.add(error);
        }
        return new JsError<T>(thrs);
    }

    public JsError(List<Throwable> errors) {
        this.errors = errors;
    }

    public JsError(Throwable errors) {
        this.errors = new ArrayList<>();
        this.errors.add(errors);
    }

    public Throwable firstError() {
        if (errors.isEmpty()) {
            return new IllegalAccessError("No error, that's weird !!!");
        }
        return errors.iterator().next();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.<T>emptyList().iterator();
    }

    public JsArray errors() {
        return Json.arr(errorsAsString());
    }

    public List<String> errorsAsString() {
        return errors.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "JsError(" + errors + ')';
    }
}