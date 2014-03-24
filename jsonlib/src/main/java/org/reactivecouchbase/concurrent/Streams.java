package org.reactivecouchbase.concurrent;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Streams {

    public static abstract class Producer<E> {
        private final Promise<Functionnal.Unit> promise = Promise.create();
        public abstract void nextElement(Consumer<E> to);
        public void done(Consumer<E> to) {
            promise.trySuccess(Functionnal.Unit.unit());
        }
        public void error(Throwable t, Consumer<E> to) {
            promise.tryFailure(t);
        }
        public final Producer<E> mergeWith(Producer<E> otherProducer, ExecutorService ec) {
            final CountDownLatch latch = new CountDownLatch(2);
            final PushProducer<E> finalProducer = new PushProducer<E>(ec);
            consumeWith(new Consumer<E>() {
                @Override
                public void element(E e, Producer<E> from) {
                    from.nextElement(this);
                    finalProducer.push(e);
                }

                @Override
                public void end(Producer<E> from) {
                    latch.countDown();
                    if (latch.getCount() == 0) finalProducer.stop();
                    super.end(from);
                }

                @Override
                public void empty(Producer<E> from) {
                    latch.countDown();
                    if (latch.getCount() == 0) finalProducer.stop();
                    super.empty(from);
                }
            });
            otherProducer.consumeWith(new Consumer<E>() {
                @Override
                public void element(E e, Producer<E> from) {
                    from.nextElement(this);
                    finalProducer.push(e);
                }

                @Override
                public void end(Producer<E> from) {
                    latch.countDown();
                    if (latch.getCount() == 0) finalProducer.stop();
                    super.end(from);
                }

                @Override
                public void empty(Producer<E> from) {
                    latch.countDown();
                    if (latch.getCount() == 0) finalProducer.stop();
                    super.empty(from);
                }
            });
            return finalProducer;
        }
        public final Producer<E> filterNot(final Function<E, Boolean> transformer, ExecutorService ec) {
            return transform(Transformer.filterNot(transformer), ec);
        }
        public final Producer<E> filter(final Function<E, Boolean> transformer, ExecutorService ec) {
            return transform(Transformer.filter(transformer), ec);
        }
        public final <B> Producer<B> map(final Function<E, B> transformer, ExecutorService ec) {
            return transform(Transformer.map(transformer), ec);
        }
        public final <B> Producer<B> collect(Function<E, Functionnal.Option<B>> transformer, ExecutorService ec) {
            return transform(transformer, ec);
        }
        public final <B> Producer<B> transform(Function<E, Functionnal.Option<B>> transformer, ExecutorService ec) {
            return new Transformer<E, B>(this, transformer, ec);
        }
        public final Future<Functionnal.Unit> consumeWith(final Consumer<E> consumer) {
            nextElement(consumer);
            return consumer.promise.future();
        }
        public final boolean isDone() { return promise.isCompleted(); }
        public final Future<Functionnal.Unit> future() { return promise.future(); }

        public static <A, T extends Iterable<A>> Producer<A> from(T iterable, ExecutorService ec) {
            return new IterableProducer<A>(iterable, ec);
        }
    }

    public static abstract class Consumer<E> {
        private final Promise<Functionnal.Unit> promise = Promise.create();
        public abstract void element(E e, Producer<E> from);
        public void empty(Producer<E> from) {
            from.nextElement(this);
        }
        public void end(Producer<E> from) {
            promise.trySuccess(Functionnal.Unit.unit());
        }
        public static <T> Consumer<T> foreach(Function<T, Functionnal.Unit> each) {
            return new ForeachConsumer<T>(each);
        }
    }

    public static class Transformer<FROM, TO> extends Producer<TO> {
        private final ExecutorService ec;
        private final Producer<FROM> fromProducer;
        private final Function<FROM, Functionnal.Option<TO>> transformer;

        public Transformer(Producer<FROM> fromProducer, Function<FROM, Functionnal.Option<TO>> transformer, ExecutorService executorService) {
            this.fromProducer = fromProducer;
            this.transformer = transformer;
            this.ec = executorService;
        }

        public <TO2> Transformer<FROM, TO2> compose(final Transformer<TO, TO2> transformer1) {
            return new Transformer<FROM, TO2>(fromProducer, new Function<FROM, Functionnal.Option<TO2>>() {
                @Override
                public Functionnal.Option<TO2> apply(FROM input) {
                    return transformer.apply(input).flatMap(new Function<TO, Functionnal.Option<TO2>>() {
                        @Override
                        public Functionnal.Option<TO2> apply(TO input2) {
                            return transformer1.transformer.apply(input2);
                        }
                    });
                }
            }, ec);
        }

        @Override
        public void nextElement(final Consumer<TO> consumer) {
            final Producer<TO> that = this;
            fromProducer.nextElement(new Consumer<FROM>() {
                @Override
                public void element(final FROM element, Producer<FROM> from) {
                    Functionnal.Option<TO> optEl = transformer.apply(element);
                    if (optEl != null && optEl.isDefined()) {
                        consumer.element(optEl.get(), that);
                    } else {
                        consumer.empty(that);
                    }
                }

                @Override
                public void empty(Producer<FROM> from) {
                    consumer.empty(that);
                    super.empty(from);
                }

                @Override
                public void end(Producer<FROM> from) {
                    consumer.end(that);
                    super.end(from);
                }
            });
        }
        public static <FROM> Functionnal.ComposableFunction<FROM, Functionnal.Option<FROM>> filterNot(final Function<FROM, Boolean> transformer) {
            return Functionnal.chain(new Function<FROM, Functionnal.Option<FROM>>() {
                @Override
                public Functionnal.Option<FROM> apply(FROM input) {
                    if (!transformer.apply(input)) {
                        return Functionnal.Option.some(input);
                    } else {
                        return Functionnal.Option.none();
                    }
                }
            });
        }
        public static <FROM> Functionnal.ComposableFunction<FROM, Functionnal.Option<FROM>> filter(final Function<FROM, Boolean> transformer) {
            return Functionnal.chain(new Function<FROM, Functionnal.Option<FROM>>() {
                @Override
                public Functionnal.Option<FROM> apply(FROM input) {
                    if (transformer.apply(input)) {
                        return Functionnal.Option.some(input);
                    } else {
                        return Functionnal.Option.none();
                    }
                }
            });
        }
        public static <FROM, TO> Functionnal.ComposableFunction<FROM, Functionnal.Option<TO>> map(final Function<FROM, TO> transformer) {
            return Functionnal.chain(new Function<FROM, Functionnal.Option<TO>>() {
                @Override
                public Functionnal.Option<TO> apply(FROM input) {
                    return Functionnal.Option.some(transformer.apply(input));
                }
            });
        }
        public static <FROM, TO> Functionnal.ComposableFunction<FROM, Functionnal.Option<TO>> collect(final Function<FROM, Functionnal.Option<TO>> transformer) {
            return Functionnal.chain(transformer);
        }
    }

    public static class ForeachConsumer<T> extends Consumer<T> {
        private final Function<T, Functionnal.Unit> each;
        public ForeachConsumer(Function<T, Functionnal.Unit> each) {
            this.each = each;
        }
        @Override
        public void element(T t, Producer<T> from) {
            try {
                each.apply(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
            from.nextElement(this);
        }
    }

    public static class PushProducer<T> extends Producer<T> {
        private final AtomicReference<Consumer<T>> toRef = new AtomicReference<Consumer<T>>();
        private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private final ExecutorService ec;
        private final Producer<T> that = this;
        public PushProducer(ExecutorService ec) {
            this.ec = ec;
        }
        @Override
        public void nextElement(Consumer<T> to) {
            toRef.set(to);
            if (toRef.get() != null) {
                if (stopped.get()) {
                    toRef.get().end(that);
                } else {
                    send();
                }
            }
        }
        private void send() {
            if (toRef.get() != null && !queue.isEmpty()) {
                toRef.get().element(queue.poll(), that);
            }
        }
        public void push(final T elem) {
            queue.offer(elem);
            Future.async(new Runnable() {
                @Override
                public void run() {
                    send();
                }
            }, ec);

        }
        public void stop() {
            stopped.set(true);
            if (toRef.get() != null) {
                toRef.get().end(that);
            }
        }
    }

    public static class IterableProducer<T> extends Producer<T> {
        private final Iterator<T> iterable;
        private final ExecutorService ec;
        public IterableProducer(Iterable<T> iterable, ExecutorService ec) {
            this.iterable = iterable.iterator();
            this.ec = ec;
        }
        @Override
        public void nextElement(final Consumer<T> to) {
            final Producer<T> that = this;
            try {
                if (iterable.hasNext()) {
                    Future.async(new Runnable() {
                        @Override
                        public void run() {
                            to.element(iterable.next(), that);
                        }
                    }, ec);
                } else {
                    to.end(that);
                }
            } catch (Exception e) {
                to.end(that);
            }
        }
    }
}
