package org.reactivecouchbase.functional;

import java.util.Collections;
import java.util.Iterator;

public class None<T> extends Option<T> {

    static final None<Object> NONE_INSTANCE = new None<Object>();

    /**
     * Always returns false
     *
     * @return
     */
    @Override
    public boolean isDefined() {
        return false;
    }

    /**
     * Always returns an exception
     *
     * @return
     */
    @Override
    public T get() {
        throw new IllegalStateException("No value");
    }

    /**
     * Returns an iterator on an empty list
     *
     * @return
     */
    @Override
    public Iterator<T> iterator() {
        return Collections.<T>emptyList().iterator();
    }

    @Override
    public String toString() {
        return "None";
    }

    /**
     * Always returns true
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof None)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 424242;
    }

    @Override
    public Type type() {
        return Type.NONE;
    }

    @Override
    public Iterable<T> asSome() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<Unit> asNone() {
        return Collections.singletonList(Unit.unit());
    }
}