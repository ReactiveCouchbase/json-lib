package org.reactivecouchbase.streams;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.common.UUID;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class RxPublisher<T> implements Publisher<T>, Producer<T> {

    private final AtomicBoolean hot = new AtomicBoolean(false);
    private final ExecutorService ec;

    protected RxPublisher(ExecutorService ec) {
        this.ec = ec;
    }

    public abstract Functionnal.Option<T> nextElement();

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
        };
    }

    public static <T> RxPublisher<T> from(Iterable<T> iterable, ExecutorService ec) {
        final Iterator<T> it = iterable.iterator();
        return new RxPublisher<T>(ec) {
            @Override
            public Functionnal.Option<T> nextElement() {
                if (it.hasNext()) {
                    return Functionnal.Option.some(it.next());
                }
                return Functionnal.Option.none();
            }
        };
    }

    // map
    // filter
    // filterNot
    // collect
    // merge

    public <O> RxPublisher<O> composeWith(final Function<T, O> processor) {
        final RxPublisher<O> pub = new RxPublisher<O>(ec) {
            @Override
            public Functionnal.Option<O> nextElement() {
                return null;
            }
        };
        consumeWith(new RxSubscriber<T>() {
            @Override
            public void element(T elem) {
                pub.push(processor.apply(elem));
            }
        }).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                pub.stop();
            }
        });
        return pub;
    }

    public Future<Functionnal.Unit> consumeWith(RxSubscriber<T> consumer) {
        produceTo(consumer);
        return consumer.run();
    }

    private final ConcurrentHashMap<String, SubscriberHolder<T>> subscribers =
            new ConcurrentHashMap<String, SubscriberHolder<T>>();

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        hot.set(true);
        final String uuid = UUID.generate();
        subscribers.putIfAbsent(uuid, new SubscriberHolder<T>(subscriber, new ConcurrentLinkedQueue<T>(), new AtomicLong(0L), uuid, ec));
        subscriber.onSubscribe(new Subscription() {

            @Override
            public void cancel() {
                subscribers.get(uuid).endOfStream();
                subscribers.remove(uuid);
            }

            @Override
            public void requestMore(final int elements) {
                for (int i = 0; i < elements; i++) {
                    try {
                        Functionnal.Option<T> elem = nextElement();
                        if (elem != null) {
                            if (elem.isDefined()) {
                                push(elem.get());
                            } else {
                                stop();
                            }
                        }
                    } catch (Throwable t) {
                       error(t);
                    }
                }
                final SubscriberHolder<T> holder = subscribers.get(uuid);
                Future.async(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < elements; i++) {
                            T element = holder.poll();
                            if (element != null) {
                                subscriber.onNext(element);
                            } else {
                                subscribers.get(uuid).ask();
                            }
                        }
                    }
                }, ec);
            }
        });
    }

    public void push(T element) {
        for (Map.Entry<String, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().push(element);
        }
    }

    public final void error(Throwable t) {
        for (Map.Entry<String, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().error(t);
        }
    }

    public final void stop() {
        for (Map.Entry<String, SubscriberHolder<T>> entry : subscribers.entrySet()) {
            entry.getValue().stop();
        }
    }

    static class SubscriberHolder<T> {
        public final Subscriber<T> subscriber;
        public final ConcurrentLinkedQueue<T> queue;
        public final AtomicLong askedElements;
        public final String uuid;
        public final ExecutorService ec;

        SubscriberHolder(Subscriber<T> subscriber, ConcurrentLinkedQueue<T> queue, AtomicLong askedElements, String uuid, ExecutorService ec) {
            this.subscriber = subscriber;
            this.queue = queue;
            this.askedElements = askedElements;
            this.uuid = uuid;
            this.ec = ec;
        }

        public void offer(T element) {
            push(element);
        }

        public void error(Throwable t) {
            subscriber.onError(t);
        }

        public void stop() {
            subscriber.onComplete();
        }

        public T poll() {
            return queue.poll();
        }

        public void ask() {
            askedElements.incrementAndGet();
        }

        public void endOfStream() {
            subscriber.onComplete();
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
}
