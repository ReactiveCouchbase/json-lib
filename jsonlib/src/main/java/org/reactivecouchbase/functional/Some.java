package org.reactivecouchbase.functional;

import java.util.Collections;
import java.util.Iterator;

public class Some<T> extends Option<T> {

    final T value;

    public Some(T value) {
        this.value = value;
    }

    /**
     * Always returns true
     *
     * @return
     */
    @Override
    public boolean isDefined() {
        return true;
    }

    /**
     * Returns the stored object
     *
     * @return
     */
    @Override
    public T get() {
        return value;
    }

    /**
     * Returns an iterator on a new list only containing the stored object
     *
     * @return
     */
    @Override
    public Iterator<T> iterator() {
        return Collections.singletonList(value).iterator();
    }

    @Override
    public String toString() {
        return "Some ( " + value + " )";
    }

    /**
     * Always returns false
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Some)) {
            return false;
        }

        Some some = (Some) o;

        if (!value.equals(some.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public Type type() {
        return Type.SOME;
    }

    @Override
    public Iterable<T> asSome() {
        return Collections.singleton(value);
    }

    @Override
    public Iterable<Unit> asNone() {
        return Collections.emptySet();
    }
}