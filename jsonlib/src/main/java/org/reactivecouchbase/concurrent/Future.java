package org.reactivecouchbase.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.reactivecouchbase.common.Duration;
import org.reactivecouchbase.common.Functionnal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Future<T> {

    List<Functionnal.Action<Functionnal.Try<T>>> callbacks = new ArrayList<Functionnal.Action<Functionnal.Try<T>>>();
    final ExecutorService ec;
    final Promise<T> promise;
    final java.util.concurrent.Future<T> fuuuu;

    Future(final Promise<T> promise, ExecutorService ec) {
        this.ec = ec;
        this.promise = promise;
        this.fuuuu = new java.util.concurrent.Future<T>() {
            @Override
            public boolean cancel(boolean b) {
                throw new IllegalAccessError("You can't stop the future !!!");
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return promise.isCompleted();
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                promise.promiseLock.await();
                return promise.internalResult.get().get();
            }

            @Override
            public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                promise.promiseLock.await(l, timeUnit);
                if (!promise.isCompleted()) throw new RuntimeException("Underlying promise is not completed yet.");
                Functionnal.Try<T> tr = promise.internalResult.get();
                if (tr == null) {
                    // wut ???
                    return null;
                }
                return tr.get();
            }
        };
    }

    public Functionnal.Option<Functionnal.Try<T>> getValue() {
        return Functionnal.Option.some(promise.internalResult.get());
    }

    public boolean isSuccess() {
        return promise.internalResult.get().isSuccess();
    }

    public boolean isFailure() {
        return promise.internalResult.get().isFailure();
    }

    private boolean isDone() {
        return promise.isCompleted();
    }

    public java.util.concurrent.Future<T> toJucFuture() {
        return fuuuu;
    }

    public ListenableFuture<T> toListenableFuture() {
        return JdkFutureAdapters.listenInPoolThread(fuuuu);
    }

    public ListenableFuture<T> toListenableFuture(ExecutorService ec) {
        return JdkFutureAdapters.listenInPoolThread(fuuuu, ec);
    }

    void triggerCallbacks() {
        //SimpleLogger.trace("Triggering {} callbacks", callbacks.size());
        for (final Functionnal.Action<Functionnal.Try<T>> block : callbacks) {
            //SimpleLogger.trace("Submit complete block");
            ec.submit(new Runnable() {
                @Override
                public void run() {
                    //SimpleLogger.trace("Running complete block " + promise.internalResult.get());
                    block.apply(promise.internalResult.get());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* Resulting Future will use the  Executor from the current Future */
    public Future<T> andThen(final Functionnal.Action<Functionnal.Try<T>> callback) { return andThen(callback, ec); }

    public Future<T> andThen(final Functionnal.Action<Functionnal.Try<T>> callback, ExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> r) {
                callback.apply(r);
                promise.complete(r);
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public void onComplete(final Functionnal.Action<Functionnal.Try<T>> callback) { onComplete(callback, ec); }

    public void onComplete(final Functionnal.Action<Functionnal.Try<T>> callback, ExecutorService ec) {
        synchronized (this) {
            if (!isDone()) {
                callbacks.add(callback);
            }
        }
        if (isDone()) {
            ec.submit(new Runnable() {
                @Override
                public void run() {
                    callback.apply(promise.internalResult.get());
                }
            });
        }
    }

    /* Resulting Future will use the  Executor from the current Future */
    public void onSuccess(final Functionnal.Action<T> callback) { onSuccess(callback, ec); }

    public void onSuccess(final Functionnal.Action<T> callback, ExecutorService ec) {
        onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for (T t : result.asSuccess()) {
                    callback.apply(t);
                }
            }
        }, ec);
    }

    /* Resulting Future will use the  Executor from the current Future */
    public void onError(final Functionnal.Action<Throwable> callback) { onError(callback, ec); }

    public void onError(final Functionnal.Action<Throwable> callback, ExecutorService ec) {
        onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for (Throwable t : result.asFailure()) {
                    callback.apply(t);
                }
            }
        }, ec);
    }

    /* Resulting Future will use the  Executor from the current Future */
    public <B> Future<B> map(final Function<T, B> map) { return map(map, ec); }

    public <B> Future<B> map(final Function<T, B> map, ExecutorService ec) {
        final Promise<B> promise = new Promise<B>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for (Throwable t : result.asFailure()) {
                    promise.failure(t);
                }
                for (T value : result.asSuccess()) {
                    try {
                        promise.success(map.apply(value));
                    } catch (Exception ex) {
                        promise.failure(ex);
                    }
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public Future<T> filter(final Function<T, Boolean> predicate) { return filter(predicate, ec); }

    public Future<T> filter(final Function<T, Boolean> predicate, ExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for (Throwable t : result.asFailure()) {
                    promise.failure(t);
                }
                for (T value : result.asSuccess()) {
                    try {
                        if (predicate.apply(value)) {
                            promise.success(value);
                        }
                    } catch (Exception ex) {
                        promise.failure(ex);
                    }
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public Future<T> filterNot(final Function<T, Boolean> predicate) { return filterNot(predicate, ec); }

    public Future<T> filterNot(final Function<T, Boolean> predicate, ExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for (Throwable t : result.asFailure()) {
                    promise.failure(t);
                }
                for (T value : result.asSuccess()) {
                    try {
                        if (!predicate.apply(value)) {
                            promise.success(value);
                        }
                    } catch (Exception ex) {
                        promise.failure(ex);
                    }
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public <B> Future<B> flatMap(final Function<T, Future<B>> map) { return flatMap(map, ec); }

    public <B> Future<B> flatMap(final Function<T, Future<B>> map, final ExecutorService ec) {
        final Promise<B> promise = new Promise<B>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> result) {
                for(Throwable t : result.asFailure()) {
                    promise.failure(t);
                }
                for(T value : result.asSuccess()) {
                    try {
                        Future<B> fut = map.apply(value);
                        fut.onComplete(new Functionnal.Action<Functionnal.Try<B>>() {
                            @Override
                            public void call(Functionnal.Try<B> bTry) {
                                for (Throwable t : bTry.asFailure()) {
                                    promise.failure(t);
                                }
                                for (B valueB : bTry.asSuccess()) {
                                    promise.success(valueB);
                                }
                            }
                        }, ec);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public <S> Future<S> mapTo(final Class<S> clazz) { return mapTo(clazz, ec); }

    public <S> Future<S> mapTo(final Class<S> clazz, ExecutorService ec) {
        return map(new Function<T, S>() {
            @Override
            public S apply(T value) {
                return clazz.cast(value);
            }
        }, ec);
    }

    /* Resulting Future will use the  Executor from the current Future */
    public void foreach(final Function<T, ?> block) { foreach(block, ec); }

    public void foreach(final Function<T, ?> block, ExecutorService ec) {
        this.map(new Function<T, Object>() {
            @Override
            public Object apply(T t) {
                return block.apply(t);
            }
        }, ec);
    }

    /* Resulting Future will use the  Executor from the current Future */
    public <S> Future<S> transform(final Function<T, S> block, final Function<Throwable, Throwable> errorBlock) { return transform(block, errorBlock, ec); }

    public <S> Future<S> transform(final Function<T, S> block, final Function<Throwable, Throwable> errorBlock, ExecutorService ec) {
        final Promise<S> promise = new Promise<S>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> tTry) {
                for (final Throwable t : tTry.asFailure()) {
                    promise.complete(Functionnal.Try.apply(new Function<Functionnal.Unit, S>() {
                        @Override
                        public S apply(Functionnal.Unit unit) {
                            throw Throwables.propagate(errorBlock.apply(t));
                        }
                    }));
                }
                for (final T value : tTry.asSuccess()) {
                    promise.complete(Functionnal.Try.apply(new Function<Functionnal.Unit, S>() {
                        @Override
                        public S apply(Functionnal.Unit unit) {
                            return block.apply(value);
                        }
                    }));
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public <U> Future<U> recover(final Function<Throwable, U> block) { return recover(block, ec); }

    public <U> Future<U> recover(final Function<Throwable, U> block, ExecutorService ec) {
        final Promise<U> promise = new Promise<U>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> v) {
                promise.complete(v.recover(block));
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public Future<T> recoverWith(final Function<Throwable, Future<T>> block) { return recoverWith(block, ec); }

    public Future<T> recoverWith(final Function<Throwable, Future<T>> block, final ExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(Functionnal.Try<T> v) {
                for (final Throwable t : v.asFailure()) {
                    try {
                        block.apply(t).onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
                            @Override
                            public void call(Functionnal.Try<T> tTry) {
                                promise.complete(tTry);
                            }
                        }, ec);
                    } catch (Throwable tr) {
                        promise.failure(tr);
                    }
                }
                for (final T value : v.asSuccess()) {
                    promise.complete(v);
                }
            }
        }, ec);
        return promise.future();
    }

    /* Resulting Future will use the  Executor from the current Future */
    public Future<T> fallbackTo(final Future<T> that) { return fallbackTo(that, ec); }

    public Future<T> fallbackTo(final Future<T> that, final ExecutorService ec) {
        final Promise<T> p = new Promise<T>();
        this.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
            @Override
            public void call(final Functionnal.Try<T> tTry) {
                for (Throwable t : tTry.asFailure()) {
                   that.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
                       @Override
                       public void call(Functionnal.Try<T> uTry) {
                           for (Throwable tr : uTry.asFailure()) {
                                p.complete(tTry);
                           }
                           for (T value : uTry.asSuccess()) {
                                p.complete(uTry);
                           }
                       }
                   }, ec);
                }
                for (T value : tTry.asSuccess()) {
                    p.complete(tTry);
                }
            }
        }, ec);
        return p.future();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   Static API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <T> Future<T> firstCompletedOf(final List<Future<T>> futures, final ExecutorService ec) {
        final Promise<T> result = new Promise<T>();
        for (Future<T> future : futures) {
            future.onSuccess(new Functionnal.Action<T>() {
                @Override
                public void call(T t) {
                    result.trySuccess(t);
                }
            }, ec);
        }
        return result.future();
    }

    public static <T> Future<List<T>> sequence(final List<Future<T>> futures, final ExecutorService ec) {
        final Promise<List<T>> result = new Promise<List<T>>();
        final List<T> results = Collections.synchronizedList(Lists.<T>newArrayList());
        final CountDownLatch latch = new CountDownLatch(futures.size());
        for(Future<T> future : futures) {
            future.onComplete(new Functionnal.Action<Functionnal.Try<T>>() {
                @Override
                public void call(Functionnal.Try<T> tTry) {
                    latch.countDown();
                    for (Throwable t : tTry.asFailure()) {
                        result.failure(t);
                    }
                    for (T value : tTry.asSuccess()) {
                        results.add(value);
                    }
                    if (latch.getCount() == 0) {
                        result.success(results);
                    }
                }
            }, ec);
        }
        if (futures.isEmpty()) {
            result.success(results);
        }
        return result.future();
    }

    public static Future<Functionnal.Unit> in(Duration duration, final Runnable block, ScheduledExecutorService ec) {
        return in(duration.toMillis(), TimeUnit.MILLISECONDS, block, ec);
    }

    public static Future<Functionnal.Unit> in(Long in, TimeUnit unit, final Runnable block, ScheduledExecutorService ec) {
        return in(in, unit, new Function<Void, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(java.lang.Void aVoid) {
                block.run();
                return Functionnal.Unit.unit();
            }
        }, ec);
    }

    public static <T> Future<T> in(Duration duration, final Function<Void, T> block, ScheduledExecutorService ec) {
        return in(duration.toMillis(), TimeUnit.MILLISECONDS, block, ec);
    }

    public static <T> Future<T> in(Long in, TimeUnit unit, final Function<Void, T> block, ScheduledExecutorService ec) {
        final Promise<T> promise = new Promise<T>(ec);
        ec.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    promise.success(block.apply(null));
                } catch (Throwable e) {
                    promise.failure(e);
                }
            }
        }, in, unit);
        return promise.future();
    }

    public static <T> Future<T> async(final Function<Void, T> block, ExecutorService ec) {
        final Promise<T> promise = new Promise<T>(ec);
        ec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    promise.success(block.apply(null));
                } catch (Throwable e) {
                    promise.failure(e);
                }
            }
        });
        return promise.future();
    }

    public static Future<Functionnal.Unit> async(final Runnable block, ExecutorService ec) {
        final Promise<Functionnal.Unit> promise = new Promise<Functionnal.Unit>(ec);
        ec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    block.run();
                    promise.success(Functionnal.Unit.unit());
                } catch (Throwable e) {
                    promise.failure(e);
                }
            }
        });
        return promise.future();
    }

    public static <T> Future<T> failed(Throwable exception) {
        return new Promise<T>().failure(exception).future();
    }

    public static <T> Future<T> successful(T result) {
        return new Promise<T>().success(result).future();
    }

    public static <T> Future<T> from(final java.util.concurrent.Future<T> future, final ScheduledExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        Runnable wait = new Runnable() {
            @Override
            public void run() {
                if (future.isCancelled()) {
                    promise.tryFailure(new CancellationException("Future has been cancelled"));
                } else if (future.isDone()) {
                    try {
                        promise.trySuccess(future.get());
                    } catch (Exception e) {
                        promise.tryFailure(e);
                    }
                } else {
                    ec.schedule(this, 100, TimeUnit.MILLISECONDS);
                }
            }
        };
        ec.schedule(wait, 10, TimeUnit.MILLISECONDS);
        return promise.future();
    }

    public static <T> Future<T> fromListenable(final ListenableFuture<T> future, ExecutorService ec) {
        final Promise<T> promise = new Promise<T>();
        future.addListener(new Runnable() {
            @Override
            public void run() {
                if (future.isCancelled()) {
                    promise.tryFailure(new CancellationException("Future has been cancelled"));
                }
                if (future.isDone()) {
                    try {
                        promise.trySuccess(future.get());
                    } catch (Exception e) {
                        promise.tryFailure(e);
                    }
                }
            }
        }, ec);
        return promise.future();
    }

    public static <T> Future<T> timeout(final T some, Duration duration, ScheduledExecutorService ec) { return timeout(some, duration.toMillis(), TimeUnit.MILLISECONDS, ec); }

    public static <T> Future<T> timeout(final T some, Long in, TimeUnit unit, ScheduledExecutorService ec) {
        return in(in, unit, new Function<Void, T>() {
            @Override
            public T apply(java.lang.Void aVoid) {
                return some;
            }
        }, ec);
    }
}
