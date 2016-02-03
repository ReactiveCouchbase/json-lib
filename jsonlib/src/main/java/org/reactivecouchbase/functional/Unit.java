package org.reactivecouchbase.functional;

public class Unit {

    private static final Unit instance = new Unit();

    private Unit() {

    }

    public static Unit unit() {
        return instance;
    }
}
