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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamsTest {

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
                System.out.println("Consumed : " + input + " => " + test.addAndGet(input));
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
                    Thread.sleep(1000);
                    pusher.push("Hello ");
                    Thread.sleep(1000);
                    pusher.push("World");
                    Thread.sleep(1000);
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
