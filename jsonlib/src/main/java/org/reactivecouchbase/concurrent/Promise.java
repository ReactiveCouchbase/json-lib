package org.reactivecouchbase.concurrent;

import org.reactivecouchbase.common.Functionnal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class Promise<T> {

    static final ExecutorService INTERNAL_EXECUTION_CONTEXT = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    final CountDownLatch promiseLock = new CountDownLatch(1);

    AtomicReference<Functionnal.Try<T>> internalResult = new AtomicReference<Functionnal.Try<T>>(null);

    private final Future<T> future;

    public Promise(ExecutorService ec) {
        this.future = new Future<T>(this, ec);
    }

    public Promise() {
        this.future = new Future<T>(this, INTERNAL_EXECUTION_CONTEXT);
    }

    public boolean isCompleted() {
        return promiseLock.getCount() <= 0;
    }

    public Future<T> future() {
        return future;
    }

    public Promise<T> complete(Functionnal.Try<T> result) {
        synchronized (this) {
            if (!isCompleted()) {
                this.internalResult.set(result);
                promiseLock.countDown();
            } else {
                throw new IllegalStateException("Promise already completed !");
            }
        }
        future.triggerCallbacks();
        return this;
    }

    public Boolean tryComplete(Functionnal.Try<T> result) {
        if (isCompleted()) return false;
        complete(result);
        return true;
    }


    public Promise<T> success(T result) {
        synchronized (this) {
            if (!isCompleted()) {
                this.internalResult.set(new Functionnal.Success<T>(result));
                promiseLock.countDown();
            } else {
                throw new IllegalStateException("Promise already completed !");
            }
        }
        future.triggerCallbacks();
        return this;
    }

    public Boolean trySuccess(T result) {
        if (isCompleted()) return false;
        success(result);
        return true;
    }

    public Promise<T> failure(Throwable result) {
        synchronized (this) {
            if (!isCompleted()) {
                this.internalResult.set(new Functionnal.Failure<T>(result));
                promiseLock.countDown();
            } else {
                throw new IllegalStateException("Promise already completed !");
            }
        }
        future.triggerCallbacks();
        return this;
    }

    public Boolean tryFailure(Throwable result) {
        if (isCompleted()) return false;
        failure(result);
        return true;
    }

    public static <T> Promise<T> failed(Throwable exception) {
        Promise<T> promise = new Promise<T>();
        promise.failure(exception);
        return promise;
    }

    public static <T> Promise<T> successful(T result) {
        Promise<T> promise = new Promise<T>();
        promise.success(result);
        return promise;
    }
}