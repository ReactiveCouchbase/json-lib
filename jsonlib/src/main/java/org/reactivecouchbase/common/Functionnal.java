package org.reactivecouchbase.common;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class Functionnal {

    private Functionnal() {}

    public static class ComposableFunction<I, O> implements Function<I, O> {
        private final Function<I, O> function;

        private ComposableFunction(Function<I, O> function) {
            this.function = function;
        }

        public <OO> ComposableFunction<I, OO> andThen(final Function<O, OO> f) {
            return new ComposableFunction<I, OO>(new Function<I, OO>() {
                @Override
                public OO apply(I i) {
                    return f.apply(function.apply(i));
                }
            });
        }

        @Override
        public O apply(I i) {
            return function.apply(i);
        }

        public Function<I, O> asFunction() {
            return this;
        }
    }

    public static <I, O> ComposableFunction<I, O> chain(Function<I, O> function) {
        return new ComposableFunction<I, O>(function);
    }

    public static class Unit {
        private static final Unit instance = new Unit();
        private Unit() {}
        public static Unit unit() { return instance; }
    }

    public static abstract class Action<T> implements Function<T, Void> {

        @Override
        public final Void apply(T t) {
            call(t);
            return null;
        }

        public abstract void call(T t);
    }

    public static interface Monad<T> {

        <R> Option<R> map(Function<T, R> function);

        Option<T> map(Callable<T> function);

        <R> Option<R> flatMap(Callable<Option<R>> action);

        <R> Option<R> flatMap(Function<T, Option<R>> action);

        Option<T> filter(Function<T, Boolean> predicate);

        Option<T> filterNot(Function<T, Boolean> predicate);

    }

    public static abstract class Option<T> implements Iterable<T>, Monad<T>, Serializable {

        public static <T> Option<T> bind() {
            return Option.none();
        }

        public static <T> Option<T> bind(T some) {
            if (some == null) return Option.none();
            return Option.some(some);
        }

        public abstract boolean isDefined();

        public abstract boolean isEmpty();

        public abstract T get();

        public abstract Optional<T> toOptional();

        public Option<T> orElse(T value) {
            return isEmpty() ? Option.maybe(value) : this;
        }

        public T getOrElse(T value) {
            return isEmpty() ? value : get();
        }

        public T getOrElse(Function<Unit, T> function) {
            return isEmpty() ? function.apply(Unit.unit()) : get();
        }

        public T getOrElse(Callable<T> function) {
            T ret = null;
            try {
                ret = function.call();
            } catch(Exception e) {}
            return isEmpty() ? ret : get();
        }

        public T getOrNull() {
            return isEmpty() ? null : get();
        }

        public Option<T> filter(Function<T, Boolean> predicate) {
            if (isDefined()) {
                if (predicate.apply(get())) {
                    return this;
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public Option<T> filterNot(Function<T, Boolean> predicate) {
            if (isDefined()) {
                if (!predicate.apply(get())) {
                    return this;
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public <X> Either<X, T> toRight(X left) {
            if (isDefined()) {
                return Either.eitherRight(get());
            } else {
                return Either.eitherLeft(left);
            }
        }

        public <X> Either<T, X> toLeft(X right) {
            if (isDefined()) {
                return Either.eitherLeft(get());
            } else {
                return Either.eitherRight(right);
            }
        }

        @Override
        public <R> Option<R> map(Function<T, R> function) {
            if (isDefined()) {
                return Option.maybe(function.apply(get()));
            }
            return Option.none();
        }

        @Override
        public Option<T> map(Callable<T> function) {
            if (isDefined()) {
                T ret = null;
                try {
                    ret = function.call();
                } catch(Throwable e) {}
                return Option.maybe(ret);
                //return Option.maybe(get());
            }
            return Option.none();
        }

        @Override
        public <R> Option<R> flatMap(Callable<Option<R>> action) {
            if (isDefined()) {
                try {
                    return action.call();
                } catch(Throwable e) {
                    return Option.none();
                }
            }
            return Option.none();
        }

        @Override
        public <R> Option<R> flatMap(Function<T, Option<R>> action) {
            if (isDefined()) {
                return action.apply(get());
            }
            return Option.none();
        }

        public static <T> None<T> none() {
            return (None<T>) (Object) none;
        }

        public static <T> Some<T> some(T value) {
            return new Some<T>(value);
        }

        public static <T> Option<T> maybe(T value) {
            return apply(value);
        }

        public static <T> Option<T> apply(T value) {
            if (value == null) {
                return Option.none();
            } else {
                return Option.some(value);
            }
        }

        public static <T> Option<T> unit(T value) {
            return apply(value);
        }
    }

    public static class None<T> extends Option<T> {

        @Override
        public Optional<T> toOptional() {
            return Optional.absent();
        }

        @Override
        public boolean isDefined() {
            return false;
        }

        @Override
        public T get() {
            throw new IllegalStateException("No value");
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }

        @Override
        public String toString() {
            return "None";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    public static class Some<T> extends Option<T> {

        final T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        @Override
        public boolean isDefined() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.singletonList(value).iterator();
        }

        @Override
        public String toString() {
            return "Some ( " + value + " )";
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    final static None<Object> none = new None<Object>();

    public static class Tuple<A, B> extends T2<A, B> {
        public Tuple(A _1, B _2) {
            super(_1, _2);
        }
    }

    public static class T1<A> {
        public final A _1;
        public T1(A _1) {
            this._1 = _1;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + " )";
        }
    }

    public static class T2<A, B> {
        public final A _1;
        public final B _2;
        public T2(A _1, B _2) {
            this._1 = _1;
            this._2 = _2;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + " )";
        }
    }

    public static class T3<A, B, C> {
        public final A _1;
        public final B _2;
        public final C _3;
        public T3(A _1, B _2, C _3) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3 + " )";
        }
    }

    public static class T4<A, B, C, D> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public T4(A _1, B _2, C _3, D _4) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: "
                    + _3 + ", _4: " + _4 + " )";
        }
    }

    public static class T5<A, B, C, D, E> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public T5(A _1, B _2, C _3, D _4, E _5) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + " )";
        }
    }

    public static class T6<A, B, C, D, E, F> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public final F _6;
        public T6(A _1, B _2, C _3, D _4, E _5, F _6) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + ", _6: " + _6 + " )";
        }
    }

    public static class T7<A, B, C, D, E, F, G> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public final F _6;
        public final G _7;
        public T7(A _1, B _2, C _3, D _4, E _5, F _6, G _7) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + ", _6: " + _6 + " )";
        }
    }

    public static class T8<A, B, C, D, E, F, G, H> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public final F _6;
        public final G _7;
        public final H _8;
        public T8(A _1, B _2, C _3, D _4, E _5, F _6, G _7, H _8) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
            this._8 = _8;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + ", _6: " + _6
                    + ", _7: " + _7 + ", _8: " + _8 + " )";
        }
    }

    public static class T9<A, B, C, D, E, F, G, H, I> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public final F _6;
        public final G _7;
        public final H _8;
        public final I _9;
        public T9(A _1, B _2, C _3, D _4, E _5, F _6, G _7, H _8, I _9) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
            this._8 = _8;
            this._9 = _9;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + ", _6: " + _6
                    + ", _7: " + _7 + ", _8: " + _8 + ", _9: " + _9 + " )";
        }
    }

    public static class T10<A, B, C, D, E, F, G, H, I, J> {
        public final A _1;
        public final B _2;
        public final C _3;
        public final D _4;
        public final E _5;
        public final F _6;
        public final G _7;
        public final H _8;
        public final I _9;
        public final J _10;
        public T10(A _1, B _2, C _3, D _4, E _5, F _6, G _7, H _8, I _9, J _10) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
            this._8 = _8;
            this._9 = _9;
            this._10 = _10;
        }
        public String toString() {
            return "Tuple ( _1: " + _1 + ", _2: " + _2 + ", _3: " + _3
                    + ", _4: " + _4 + ", _5: " + _5 + ", _6: " + _6
                    + ", _7: " + _7 + ", _8: " + _8 + ", _9: " + _9
                    + ", _10: " + _10 + " )";
        }
    }

    public static abstract class Try<T> {

        protected final T value;

        protected final Throwable e;

        public Option<Throwable> error() {
            if (isFailure()) {
                return Option.some(e);
            } else {
                return Option.none();
            }
        }

        private Try(T value, Throwable e) {
            this.value = value;
            this.e = e;
        }

        public static <T> Try<T> apply(Function<Unit, T> call) {
            try {
                return new Success<T>(call.apply(Unit.unit()));
            } catch(Exception e) {
                return new Failure(e);
            }
        }
        public Try<T> filter(Function<T, Boolean> predicate) {
            if (isFailure()) {
                return new Failure<T>(e);
            } else {
                if (predicate.apply(value)) {
                    return new Success<T>(value);
                } else {
                    return new Failure<T>(e);
                }
            }
        }
        public <U> Try<U> flatMap(Function<T, Try<U>> call) {
            if (isFailure()) {
                return new Failure<U>(e);
            } else {
                return call.apply(value);
            }
        }
        public <U> void foreach(Function<T, U> call) {
            if (isFailure()) {
                call.apply(value);
            }
        }
        public T get() {
            if (isFailure()) {
                throw new RuntimeException(e);
            } else {
                return value;
            }
        }
        public T getOrElse(T defaultValue) {
            if (isFailure()) {
                return defaultValue;
            } else {
                return value;
            }
        }
        public abstract Boolean isFailure();
        public abstract Boolean isSuccess();
        public <U> Try<U> map(Function<T, U> call) {
            if (isFailure()) {
                return new Failure<U>(e);
            } else {
                return new Success<U>(call.apply(value));
            }
        }
        public Try<T> orElse(Try<T> t) {
            if (isFailure()) {
                return t;
            } else {
                return this;
            }
        }
        public <U> Try<U> recover(Function<Throwable, U> call) {
            if (isFailure()) {
                U u = call.apply(e);
                if (u == null) {
                    return new Failure<U>(new RuntimeException("Can't recover !!!"));
                } else {
                    return new Success<U>(u);
                }
            } else {
                return new Failure<U>(e);
            }
        }
        public <U> Try<U> recoverWith(Function<Throwable, Try<U>> call) {
            if (isFailure()) {
                return call.apply(e);
            } else {
                return new Failure<U>(e);
            }
        }
        public Option<T> toOption() {
            return Option.apply(value);
        }

        public Option<Throwable> asFailure() {
            if (isFailure()) {
                return Option.some(e);
            }
            return Option.none();
        }

        public Option<T> asSuccess() {
            if (isFailure()) {
                return Option.none();
            }
            return Option.some(value);
        }

        public <U> Try<U> transform(Function<T, Try<U>> s, Function<Throwable, Try<U>> f) {
            if (isSuccess()) {
                return s.apply(value);
            } else {
                return f.apply(e);
            }
        }
    }

    public static class Success<T> extends Try<T> {

        public Success(T value) {
            super(value, null);
        }

        @Override
        public Boolean isFailure() {
            return false;
        }

        @Override
        public Boolean isSuccess() {
            return true;
        }
    }

    public static class Failure<T> extends Try<T> {

        public Failure(Throwable value) {
            super(null, value);
        }

        public Throwable throwable() {
            return e;
        }

        @Override
        public Boolean isFailure() {
            return true;
        }

        @Override
        public Boolean isSuccess() {
            return false;
        }
    }

    public static class Either<A, B> {

        final public Left<A, B> left;
        final public Right<B, A> right;

        private Either(A left, B right) {
            this.left = new Left<A, B>(left, this);
            this.right = new Right<B, A>(right, this);
        }

        public static <A, B> Either<A, B> eitherLeft(A value) {
            return new Either<A, B>(value, null);
        }

        public static <A, B> Either<A, B> eitherRight(B value) {
            return new Either<A, B>(null, value);
        }

        public <A, B> Either<A, B> left(A value) {
            if (value != null) {
                return new Either<A, B>(value, null);
            }
            return new Either(left, right);
        }

        public <A, B> Either<A, B> right(B value) {
            if (value != null) {
                return new Either<A, B>(null, value);
            }
            return new Either(left, right);
        }

        public <A, B> Either<A, B> left(Option<A> value) {
            if (value.isDefined()) {
                return new Either<A, B>(value.get(), null);
            }
            return new Either(left, right);
        }

        public <A, B> Either<A, B> right(Option<B> value) {
            if (value.isDefined()) {
                return new Either<A, B>(null, value.get());
            }
            return new Either(left, right);
        }

        public <X> Option<X> fold(Function<A, X> fa, Function<B, X> fb) {
            if (isLeft()) {
                return Option.maybe(fa.apply(left.get()));
            } else if (isRight()) {
                return Option.maybe(fb.apply(right.get()));
            } else {
                return (Option<X>) Option.none();
            }
        }

        public boolean isLeft() {
            return left.isDefined();
        }

        public boolean isRight() {
            return right.isDefined();
        }

        public Either<B, A> swap() {
            A vLeft = null;
            B vRight = null;
            if (left.isDefined()) {
                vLeft = left.get();
            }
            if (right.isDefined()) {
                vRight = right.get();
            }
            return new Either<B, A>(vRight, vLeft);
        }

        @Override
        public String toString() {
            return "Either ( left: " + left + ", right: " + right + " )";
        }
    }

    public static class Left<A, B> implements Iterable<A> {

        private final A input;

        public final Either<A, B> e;

        Left(A value, Either<A, B> e) {
            this.e = e;
            this.input = value;
        }

        public boolean isDefined() {
            return !(input == null);
        }

        public A get() {
            return input;
        }

        @Override
        public Iterator<A> iterator() {
            if (input == null) {
                return Collections.<A>emptyList().iterator();
            } else {
                return Collections.singletonList(input).iterator();
            }
        }

        @Override
        public String toString() {
            return "Left ( " + input + " )";
        }

        public boolean isEmpty() {
            return !isDefined();
        }

        public A getOrElse(A value) {
            return isEmpty() ? value : get();
        }

        public A getOrElse(Function<Unit, A> function) {
            return isEmpty() ? function.apply(Unit.unit()) : get();
        }

        public A getOrNull() {
            return isEmpty() ? null : get();
        }

        public Option<Either<A, B>> filter(Function<A, Boolean> predicate) {
            if (isDefined()) {
                if (predicate.apply(get())) {
                    return Option.maybe(e);
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public Option<Either<A, B>> filterNot(Function<A, Boolean> predicate) {
            if (isDefined()) {
                if (!predicate.apply(get())) {
                    return Option.maybe(e);
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public <R> Either<R, B> map(Function<A, R> function) {
            if (isDefined()) {
                return new Either<R, B>(function.apply(get()), null);
            } else {
                return new Either<R, B>(null, e.right.get());
            }
        }

        public <R> Either<R, B> flatMap(Function<A, Either<R, B>> action) {
            if (isDefined()) {
                return action.apply(get());
            } else {
                return new Either<R, B>(null, e.right.get());
            }
        }
    }

    public static class Right<B, A> implements Iterable<B> {

        private final B input;

        public final Either<A, B> e;

        Right(B value, Either<A, B> e) {
            this.e = e;
            this.input = value;
        }

        public boolean isDefined() {
            return !(input == null);
        }

        public B get() {
            return input;
        }

        @Override
        public Iterator<B> iterator() {
            if (input == null) {
                return Collections.<B>emptyList().iterator();
            } else {
                return Collections.singletonList(input).iterator();
            }
        }

        @Override
        public String toString() {
            return "Left ( " + input + " )";
        }

        public boolean isEmpty() {
            return !isDefined();
        }

        public B getOrElse(B value) {
            return isEmpty() ? value : get();
        }

        public B getOrElse(Function<Unit, B> function) {
            return isEmpty() ? function.apply(Unit.unit()) : get();
        }

        public B getOrNull() {
            return isEmpty() ? null : get();
        }

        public Option<Either<A, B>> filter(Function<B, Boolean> predicate) {
            if (isDefined()) {
                if (predicate.apply(get())) {
                    return Option.maybe(e);
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public Option<Either<A, B>> filterNot(Function<B, Boolean> predicate) {
            if (isDefined()) {
                if (!predicate.apply(get())) {
                    return Option.maybe(e);
                } else {
                    return Option.none();
                }
            }
            return Option.none();
        }

        public <R> Either<A, R> map(Function<B, R> function) {
            if (isDefined()) {
                return new Either<A, R>(null, function.apply(get()));
            } else {
                return new Either<A, R>(e.left.get(), null);
            }
        }

        public <R> Either<A, R> flatMap(Function<B, Either<A, R>> action) {
            if (isDefined()) {
                return action.apply(get());
            } else {
                return new Either<A, R>(e.left.get(), null);
            }
        }
    }
}
