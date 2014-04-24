package org.reactivestreams.tck;


import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.streams.RxPublisher;
import org.reactivestreams.spi.Publisher;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Test
public class RxPublisherVerification extends PublisherVerification<Integer> {

    private final ExecutorService ec = Executors.newFixedThreadPool(10);

    public RxPublisherVerification() {
        super(new TestEnvironment(5000), 5000);
    }

    @Override
    public Publisher<Integer> createPublisher(int elements) {
        List<Integer> list = new ArrayList<Integer>(elements);
        for (int i = 0; i < elements; i++) {
            list.add(i);
        }
        return RxPublisher.from(list, ec);
    }

    @Override
    public Publisher<Integer> createCompletedStatePublisher() {
        return new RxPublisher<Integer>(ec) {
            @Override
            public Functionnal.Option<Integer> nextElement() {
                return Functionnal.Option.none();
            }
        };
    }

    @Override
    public Publisher<Integer> createErrorStatePublisher() {
        return new RxPublisher<Integer>(ec) {
            @Override
            public Functionnal.Option<Integer> nextElement() {
                return Functionnal.Option.none();
            }
        };
    }
}
