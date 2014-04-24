package org.reactivecouchbase.streams;

import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.concurrent.Future;
import org.reactivecouchbase.concurrent.NamedExecutors;
import org.reactivecouchbase.concurrent.Promise;
import org.reactivestreams.api.Consumer;
import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import java.util.concurrent.ExecutorService;

public abstract class RxSubscriber<T> implements Subscriber<T>, Consumer<T> {

    private final Promise<Functionnal.Unit> promise = new Promise<Functionnal.Unit>();
    private Subscription subscription;

    @Override
    public final Subscriber<T> getSubscriber() { return this; }

    @Override
    public final void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscribe(subscription);
    }

    public void subscribe(Subscription subscription) {}

    @Override
    public final void onNext(T element) {
        try {
            element(element);
            subscription.requestMore(1);
        } catch (Throwable t) {
           subscription.cancel();
            onError(t);
        }
    }

    @Override
    public final void onComplete() {
        complete();
        promise.trySuccess(Functionnal.Unit.unit());
    }

    @Override
    public final void onError(Throwable cause) {
        error(cause);
        promise.tryFailure(cause);
    }

    public abstract void element(T elem);

    public void error(Throwable t) {}

    public void complete() {}

    private static final ExecutorService ec =
            NamedExecutors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), "Subscriber-Starter");

    public final Future<Functionnal.Unit> run() {
        Future.async(new Runnable() {
            @Override
            public void run() {
                if (subscription == null) {
                    throw new RuntimeException("Arfff");
                }
                subscription.requestMore(1);
            }
        }, ec);
        return promise.future();
    }
}
