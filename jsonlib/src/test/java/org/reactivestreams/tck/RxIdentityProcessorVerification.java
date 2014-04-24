package org.reactivestreams.tck;

import com.google.common.base.Supplier;
import org.reactivecouchbase.common.Functionnal;
import org.reactivecouchbase.concurrent.Streams;
import org.reactivecouchbase.streams.RxProcessor;
import org.reactivecouchbase.streams.RxPublisher;
import org.reactivestreams.api.Processor;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.tck.IdentityProcessorVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Test
public class RxIdentityProcessorVerification extends IdentityProcessorVerification<Integer> {

    private final ExecutorService ec = Executors.newFixedThreadPool(50);

    public RxIdentityProcessorVerification() {
        super(new TestEnvironment(1000), 1000);
    }

    @Override
    public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
        return new RxProcessor<Integer, Integer>(ec) {
            @Override
            public Integer process(Integer input) {
                return input;
            }
        };
    }

    @Override
    public Publisher<Integer> createHelperPublisher(int elements) {
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

    @Override
    public Publisher<Integer> createCompletedStatePublisher() {
        return new RxPublisher<Integer>(RxPublisher.State.COMPLETED, ec) {
            @Override
            public Functionnal.Option<Integer> nextElement() {
                return Functionnal.Option.none();
            }
        };
    }

    @Override
    public Publisher<Integer> createErrorStatePublisher() {
        return new RxPublisher<Integer>(RxPublisher.State.ERROR, ec) {
            @Override
            public Functionnal.Option<Integer> nextElement() {
                return Functionnal.Option.none();
            }
        };
    }
}
