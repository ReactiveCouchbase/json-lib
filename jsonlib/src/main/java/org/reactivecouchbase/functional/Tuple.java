package org.reactivecouchbase.functional;

import java.io.Serializable;

public class Tuple<A, B> implements Serializable {

    public final A _1;
    public final B _2;

    public Tuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static <A, B> Tuple<A, B> of(A _1, B _2) {
        return new Tuple<>(_1, _2);
    }

    public static <A, B> Tuple<A, B> from(Tuple<A, B> t) {
        return of(t._1, t._2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tuple that = (Tuple) o;

        return this._1.equals(that._1) &&
                this._2.equals(that._2);
    }

    @Override
    public int hashCode() {
        int result = _1.hashCode();
        result = 31 * result + _2.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Tuple { " +
                "_1: " + _1 +
                ", _2: " + _2 +
                " }";
    }
}