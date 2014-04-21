package org.reactivecouchbase.json.test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.sun.istack.internal.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.concurrent.Await;
import org.reactivecouchbase.concurrent.Future;
import org.reactivecouchbase.concurrent.Promise;
import org.reactivecouchbase.concurrent.Streams;
import org.reactivecouchbase.streams.RxPublisher;
import org.reactivecouchbase.streams.RxSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.reactivecouchbase.concurrent.Streams.Producer;

public class StreamsTest {

    @Test
    public void basicTestRxStreams() throws Exception {
        final AtomicInteger test = new AtomicInteger(0);
        final Promise<Functionnal.Unit> promise = Promise.create();
        new RxPublisher<Integer>(Executors.newFixedThreadPool(4)) {
            AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Functionnal.Option<Integer> nextElement() {
                if (counter.get() < 200) {
                    return Functionnal.Option.some(counter.incrementAndGet());
                } else {
                    stop();
                    return Functionnal.Option.none();
                }
            }
        }.consumeWith(new RxSubscriber<Integer>() {
            @Override
            public void element(Integer elem) {
                test.addAndGet(elem);
            }
        }).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals(20100, test.get());
    }

    @Test
    public void rxPushTest() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(1);
        final RxPublisher<String> pusher = RxPublisher.pusher(ec);
        pusher.consumeWith(new RxSubscriber<String>() {
            @Override
            public void element(String elem) {
                builder.append(elem);
            }
        }).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        ec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    pusher.push("Hello ");
                    Thread.sleep(200);
                    pusher.push("World");
                    Thread.sleep(200);
                    pusher.push("!");
                    pusher.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("Hello World!", builder.toString());
    }

    @Test
    public void rxCollectionTest2() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(4);
        RxPublisher.from(Lists.newArrayList("Hello ", "World", "!"), ec).composeWith(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.toUpperCase();
            }
        }).consumeWith(new RxSubscriber<String>() {
            @Override
            public void element(String elem) {
                builder.append(elem);
            }
        }).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("HELLO WORLD!", builder.toString());
    }

    @Test
    public void rxCollectionTest() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(4);
        RxPublisher.from(Lists.newArrayList("Hello ", "World", "!"), ec).consumeWith(new RxSubscriber<String>() {
            @Override
            public void element(String elem) {
                builder.append(elem);
            }
        }).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("Hello World!", builder.toString());
    }

    @Test
    public void basicTest() throws Exception {
        final AtomicInteger test = new AtomicInteger(0);
        final Promise<Functionnal.Unit> promise = Promise.create();
        new Streams.Producer<Integer>() {
            AtomicInteger counter = new AtomicInteger(0);
            @Override
            public void nextElement(final Streams.Consumer<Integer> to) {
                if (counter.get() < 200) {
                    final Streams.Producer<Integer> that = this;
                    Future.async(new Runnable() {
                        @Override
                        public void run() {
                            to.element(counter.incrementAndGet(), that);
                        }
                    }, Executors.newFixedThreadPool(1));
                } else {
                    to.end(this);
                }
            }
        }.consumeWith(Streams.Consumer.foreach(new Function<Integer, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(Integer input) {
                //System.out.println("Consumed : " + input + " => " + test.get());
                test.addAndGet(input);
                return Functionnal.Unit.unit();
            }
        })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals(20100, test.get());
    }


    @Test
    public void collectionTest() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(4);
        Streams.Producer.from(Lists.newArrayList("Hello ", "World", "!"), ec).consumeWith(Streams.Consumer.foreach(new Function<String, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(String input) {
                builder.append(input);
                return Functionnal.Unit.unit();
            }
        })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("Hello World!", builder.toString());
    }

    @Test
    public void collectionTest2() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(4);
        Streams.Producer.from(Lists.newArrayList("Hello ", "World", "!"), ec).map(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.toUpperCase();
            }
        }, ec).consumeWith(Streams.Consumer.foreach(new Function<String, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(String input) {
                builder.append(input);
                return Functionnal.Unit.unit();
            }
        })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("HELLO WORLD!", builder.toString());
    }

    @Test
    public void transformerCombine() throws Exception {
        final List<String> builder = new ArrayList<String>();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(10);
        Producer.from(Lists.newArrayList(-12, 123, 1, 2, 3, 12, 13), ec)
        .filter(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer input) {
                return input > 0;
            }
        }, ec).filterNot(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer input) {
                return input > 100;
            }
        }, ec).collect(new Function<Integer, Functionnal.Option<String>>() {
            @Override
            public Functionnal.Option<String> apply(Integer input) {
                if (input > 10) return Functionnal.Option.none();
                return Functionnal.Option.some("message : " + input);
            }
        }, ec).map(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.toUpperCase();
            }
        }, ec).consumeWith(Streams.Consumer.foreach(new Function<String, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(String input) {
                builder.add(input);
                return Functionnal.Unit.unit();
            }
        })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals(3, builder.size());
        Assert.assertTrue(builder.contains("MESSAGE : 1"));
        Assert.assertTrue(builder.contains("MESSAGE : 2"));
        Assert.assertTrue(builder.contains("MESSAGE : 3"));
    }

    @Test
    public void collectionMix() throws Exception {
        final List<String> builder = new ArrayList<String>();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(4);
        Streams.Producer.from(Lists.newArrayList("Hello ", "World", "!"), ec)
                .mergeWith(Streams.Producer.from(Lists.newArrayList("Goodbye ", "World", "!"), ec), ec)
                .consumeWith(Streams.Consumer.foreach(new Function<String, Functionnal.Unit>() {
                    @Override
                    public Functionnal.Unit apply(String input) {
                        builder.add(input);
                        return Functionnal.Unit.unit();
                    }
                })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals(6, builder.size());
    }

    @Test
    public void pushTest() throws Exception {
        final StringBuilder builder = new StringBuilder();
        final Promise<Functionnal.Unit> promise = Promise.create();
        final ExecutorService ec = Executors.newFixedThreadPool(1);
        final Streams.PushProducer<String> pusher = new Streams.PushProducer<String>(ec);
        pusher.consumeWith(Streams.Consumer.foreach(new Function<String, Functionnal.Unit>() {
            @Override
            public Functionnal.Unit apply(String input) {
                builder.append(input);
                return Functionnal.Unit.unit();
            }
        })).onComplete(new Functionnal.Action<Functionnal.Try<Functionnal.Unit>>() {
            @Override
            public void call(Functionnal.Try<Functionnal.Unit> unitTry) {
                promise.tryComplete(unitTry);
            }
        });
        ec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    pusher.push("Hello ");
                    Thread.sleep(200);
                    pusher.push("World");
                    Thread.sleep(200);
                    pusher.push("!");
                    pusher.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Await.result(promise.future(), 10L, TimeUnit.SECONDS);
        Assert.assertEquals("Hello World!", builder.toString());
    }
}
