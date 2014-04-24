package org.reactivestreams.tck;

import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.streams.RxPublisher;
import org.reactivecouchbase.streams.RxSubscriber;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Test
public class RxSuscriberVerification extends SubscriberVerification<Integer> {

    private final ExecutorService ec = Executors.newFixedThreadPool(50);


    public RxSuscriberVerification() {
        super(new TestEnvironment(1000));
    }

    public Subscriber<Integer> createSubscriber(final SubscriberProbe<Integer> probe) {
        return new RxSubscriber<Integer>() {

            @Override
            public void subscribe(final Subscription subscription) {
                probe.registerOnSubscribe(new SubscriberPuppet() {
                    @Override
                    public void triggerShutdown() {
                        subscription.cancel();
                    }

                    @Override
                    public void triggerRequestMore(int elements) {
                        subscription.requestMore(elements);
                    }

                    @Override
                    public void triggerCancel() {
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void element(Integer elem) {
                probe.registerOnNext(elem);
            }

            @Override
            public void error(Throwable t) {
                probe.registerOnError(t);
            }

            @Override
            public void complete() {
                probe.registerOnComplete();
            }
        };
    }

    public Publisher<Integer> createHelperPublisher(final int elements) {
        if (elements > 0) {
            List<Integer> list = new ArrayList<Integer>(elements);
            for (int i = 0; i < elements; i++) {
                list.add(i);
            }
            return RxPublisher.from(list, ec);
        } else {
            final Random random = new Random();
            return new RxPublisher<Integer>(ec) {
                @Override
                public Functionnal.Option<Integer> nextElement() {
                    return Functionnal.Option.some(random.nextInt(1000));
                }
            };
        }
    }
}
