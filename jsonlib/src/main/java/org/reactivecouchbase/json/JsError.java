package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.reactivecouchbase.common.Functionnal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JsError<T> extends JsResult<T> {

    public final List<Throwable> errors;

    @Override
    public T getValueOrElse(T result) {
        return result;
    }

    @Override
    public T getValueOrElse(Throwable result) {
        throw Throwables.propagate(result);
    }

    public Functionnal.Option<JsError<T>> asError() {
        return Functionnal.Option.some(this);
    }

    public Functionnal.Option<JsSuccess<T>> asSuccess() {
        return Functionnal.Option.none();
    }

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
    public Functionnal.Option<T> getOpt() {
        return Functionnal.Option.none();
    }

    @Override
    public JsResult<T> getOrElse(JsResult<T> result) {
        return result;
    }

    @Override
    public <B> JsResult<B> map(Function<T, B> map) {
        return new JsError<B>(errors);
    }

    @Override
    public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
        return new JsError<B>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate) {
        return new JsError<T>(errors);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate) {
        return new JsError<T>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, List<Throwable> errs) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<Throwable>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && predicate.apply(val.get())) {
            thrs.addAll(errs);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, List<Throwable> errs) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<Throwable>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && !predicate.apply(val.get())) {
            thrs.addAll(errs);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<Throwable>();
        thrs.addAll(this.errors);
        if (val.isSuccess() && predicate.apply(val.get())) {
            thrs.add(error);
        }
        return new JsError<T>(thrs);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        List<Throwable> thrs = new ArrayList<Throwable>();
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
        this.errors = new ArrayList<Throwable>();
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
        return Lists.newArrayList(Lists.transform(errors, new Function<Throwable, String>() {
            public String apply(Throwable throwable) {
                return throwable.getMessage();
            }
        }));
    }

    @Override
    public String toString() {
        return "JsError(" + errors + ')';
    }
}