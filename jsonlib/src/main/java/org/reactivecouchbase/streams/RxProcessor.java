package org.reactivecouchbase.streams;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;
import org.reactivestreams.api.Consumer;
import org.reactivestreams.api.Processor;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;

import java.util.concurrent.ExecutorService;

public abstract class RxProcessor<I, O> implements Processor<I, O> {

    private final RxPublisher<O> pub;
    private final RxSubscriber<I> sub;

    public RxProcessor(final ExecutorService ec) {
        this.pub = new RxPublisher<O>(ec) {
            @Override
            public Functionnal.Option<O> nextElement() {
                return null;
            }
        };
        this.sub = new RxSubscriber<I>() {
            @Override
            public void element(I elem) {
                pub.push(process(elem));
            }
        };
    }

    public abstract O process(I input);

    @Override
    public Subscriber<I> getSubscriber() {
        return sub;
    }

    @Override
    public Publisher<O> getPublisher() {
        return pub;
    }

    @Override
    public void produceTo(Consumer<O> consumer) {
        pub.produceTo(consumer);
    }
}
