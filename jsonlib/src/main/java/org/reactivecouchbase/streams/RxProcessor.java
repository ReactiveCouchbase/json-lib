package org.reactivecouchbase.streams;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;
import org.reactivestreams.api.Consumer;
import org.reactivestreams.api.Processor;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;

import java.util.concurrent.ExecutorService;

public abstract class RxProcessor<I, O> implements Processor<I, O> {

    final RxPublisher<O> pub = new RxPublisher<O>(ec) {
        @Override
        public Functionnal.Option<O> nextElement() {
            return null;
        }
    };

    final RxSubscriber<I> sub = new RxSubscriber<I>() {
        @Override
        public void element(I elem) {
            pub.push(processor.apply(elem));
        }
    };

    final Function<I, O> processor;
    final ExecutorService ec;

    protected RxProcessor(Function<I, O> processor, ExecutorService ec) {
        this.processor = processor;
        this.ec = ec;
    }

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
