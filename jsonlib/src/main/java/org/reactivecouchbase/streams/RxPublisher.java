package org.reactivecouchbase.streams;

import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.concurrent.Future;
import org.reactivestreams.api.Consumer;
import org.reactivestreams.api.Producer;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class RxPublisher<T> implements Publisher<T>, Producer<T> {

    public static enum State {
        INACTIVE, ACTIVE, COMPLETED, ERROR, SHUTDOWN
    }

    private static enum PublisherState {
        FULL, DRAINED
    }

    private final AtomicReference<State> CURRENT_STATE = new AtomicReference<State>(State.INACTIVE);
    private final AtomicReference<PublisherState> CURRENT_PUBLISHER_STATE = new AtomicReference<PublisherState>(PublisherState.FULL);
    private final ExecutorService ec;

    protected RxPublisher(ExecutorService ec) {
        this.ec = ec;
    }

    protected RxPublisher(State state, ExecutorService ec) {
        this.ec = ec;
        this.CURRENT_STATE.set(state);
    }

    public abstract Functionnal.Option<T> nextElement();

    @Override
    public String toString() {
        return "RxPublisher { " +
                "CURRENT_STATE: " + CURRENT_STATE.get() +
                " }";
    }

    @Override
    public Publisher<T> getPublisher() { return this; }

    @Override
    public void produceTo(Consumer<T> consumer) { subscribe(consumer.getSubscriber()); }

    public static <T> RxPublisher<T> pusher(ExecutorService ec) {
        return new RxPublisher<T>(ec) {
            @Override
            public Functionnal.Option<T> nextElement() {
                return null;
            }
            @Override
            public String toString() {
                return "RxPublisher pusher";
            }
        };
    }

    public static <T> RxPublisher<T> from(final Iterable<T> iterable, ExecutorService ec) {
        final Iterator<T> it = iterable.iterator();
        return new RxPublisher<T>(ec) {
            @Override
            public Functionnal.Option<T> nextElement() {
                if (it.hasNext()) {
                    return Functionnal.Option.some(it.next());
                }
                return Functionnal.Option.none();
            }
            @Override
            public String toString() {
                return "RxPublisher from iterable: " + iterable;
            }
        };
    }

    public Future<Functionnal.Unit> consumeWith(RxSubscriber<T> consumer) {
        produceTo(consumer);
        return consumer.run();
    }

    private final ConcurrentHashMap<Subscriber<T>, SubscriberHolder<T>> subscribers =
            new ConcurrentHashMap<Subscriber<T>, SubscriberHolder<T>>();

    private boolean allDrained() {
        for (Map.Entry<Subscriber<T>, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            if (!(entry.getValue().askedElements.get() == 0L)) return false;
            if (!entry.getValue().queue.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        if (CURRENT_STATE.get().equals(State.COMPLETED)) {
            subscriber.onComplete();
            return;
        }
        if (CURRENT_STATE.get().equals(State.ERROR)) {
            subscriber.onError(new IllegalStateException("The subscriber is in error state"));
            return;
        }
        if (CURRENT_STATE.get().equals(State.SHUTDOWN)) {
            subscriber.onError(new IllegalStateException("The subscriber is in error state"));
            return;
        }
        for (Map.Entry<Subscriber<T>, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            if (entry.getKey().equals(subscriber)) {
                subscriber.onError(new IllegalStateException("The subscriber already subscribed to this publisher"));
                return;
            }
        }
        subscribers.putIfAbsent(subscriber, new SubscriberHolder<T>(subscriber, new ConcurrentLinkedQueue<T>(), new AtomicLong(0L), ec));
        final Publisher<T> publisher = this;
        subscriber.onSubscribe(new Subscription() {

            @Override
            public void cancel() {
                if (CURRENT_STATE.get().equals(State.ACTIVE) || CURRENT_STATE.get().equals(State.INACTIVE)) {
                    CURRENT_STATE.set(State.SHUTDOWN);
                    subscribers.remove(subscriber);
                }
            }

            @Override
            public void requestMore(final int elements) {
                CURRENT_STATE.compareAndSet(State.INACTIVE, State.ACTIVE);
                if (CURRENT_STATE.get().equals(State.ACTIVE) || CURRENT_STATE.get().equals(State.INACTIVE)) {
                    if (CURRENT_PUBLISHER_STATE.get().equals(PublisherState.DRAINED) && allDrained()) {
                        CURRENT_STATE.set(State.COMPLETED);
                        stop();
                        return;
                    }
                    if (elements <= 0) throw new IllegalArgumentException("Nbr of elements should be greater than 0");
                    display("Asking (" + publisher + " => " + subscriber + ") for " + elements + " elements");
                    final SubscriberHolder<T> holder = subscribers.get(subscriber);
                    Future.async(new Runnable() {
                        @Override
                        public void run() {
                            if (!CURRENT_PUBLISHER_STATE.get().equals(PublisherState.DRAINED)) {
                                for (int i = 0; i < elements; i++) {
                                    try {
                                        Functionnal.Option<T> elem = nextElement();
                                        if (elem != null) {
                                            if (elem.isDefined()) {
                                                push(elem.get());
                                            } else {
                                                CURRENT_PUBLISHER_STATE.set(PublisherState.DRAINED);
                                            }
                                        }
                                    } catch (Throwable t) {
                                        error(t);
                                    }
                                    /*if (CURRENT_STATE.get().equals(State.ACTIVE)) {
                                        if (!holder.isDrained(CURRENT_PUBLISHER_STATE.get())) {
                                            T element = holder.poll();
                                            if (element != null) {
                                                try {
                                                    subscriber.onNext(element);
                                                } catch (Throwable t) {
                                                    error(t);
                                                }
                                            } else {
                                                subscribers.get(subscriber).ask();
                                            }
                                        } else {
                                            holder.complete();
                                        }
                                    }*/
                                }
                            }
                            if (!holder.isDrained(CURRENT_PUBLISHER_STATE.get())) {
                                for (int i = 0; i < elements; i++) {
                                    T element = holder.poll();
                                    if (element != null) {
                                        try {
                                            subscriber.onNext(element);
                                        } catch (Throwable t) {
                                            error(t);
                                        }
                                    } else {
                                        subscribers.get(subscriber).ask();
                                    }
                                }
                            } else {
                                if (CURRENT_STATE.get().equals(State.ACTIVE)) holder.complete();
                            }
                        }
                    }, ec);
                }
            }
        });
    }

    public void push(T element) {
        for (Map.Entry<Subscriber<T>, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().push(element);
        }
    }

    public final void error(Throwable t) {
        CURRENT_STATE.set(State.ERROR);
        for (Map.Entry<Subscriber<T>, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().error(t);
        }
    }

    public final void stop() {
        CURRENT_STATE.set(State.SHUTDOWN);
        for (Map.Entry<Subscriber<T>, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().stop();
        }
    }

    static class SubscriberHolder<T> {
        public final Subscriber<T> subscriber;
        public final ConcurrentLinkedQueue<T> queue;
        public final AtomicLong askedElements;
        public final ExecutorService ec;
        public final CountDownLatch latch = new CountDownLatch(1);

        SubscriberHolder(Subscriber<T> subscriber, ConcurrentLinkedQueue<T> queue, AtomicLong askedElements, ExecutorService ec) {
            this.subscriber = subscriber;
            this.queue = queue;
            this.askedElements = askedElements;
            this.ec = ec;
        }

        public void offer(T element) {
            push(element);
        }

        public void error(Throwable t) {
            if (latch.getCount() > 0) {
                latch.countDown();
                subscriber.onError(t);
            }
        }

        public void stop() {
            if (latch.getCount() > 0) {
                latch.countDown();
                subscriber.onComplete();
            }
        }

        public boolean isDrained(PublisherState s) {
            return s.equals(PublisherState.DRAINED) && queue.isEmpty() && askedElements.get() == 0L;
        }

        public T poll() {
            return queue.poll();
        }

        public void ask() {
            askedElements.incrementAndGet();
        }

        public void complete() {
            if (latch.getCount() > 0) {
                latch.countDown();
                subscriber.onComplete();
            }
        }

        public void push(final T element) {
            if (askedElements.get() > 0L) {
                Future.async(new Runnable() {
                    @Override
                    public void run() {
                        askedElements.decrementAndGet();
                        subscriber.onNext(element);
                    }
                }, ec);
            } else {
                queue.offer(element);
            }
        }
    }

    public static void display(Object s) {
        //if (s != null) System.out.println(s);
    }
}
