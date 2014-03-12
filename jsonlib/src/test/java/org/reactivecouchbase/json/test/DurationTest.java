package org.reactivecouchbase.json.test;

import org.junit.Assert;
import org.junit.Test;
import org.reactivecouchbase.common.Duration;

import java.util.concurrent.TimeUnit;

public class DurationTest {

    @Test
    public void testBasicInit() throws Exception {
        Duration duration = new Duration(1, TimeUnit.DAYS);
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());
    }

    @Test
    public void testParsing() throws Exception {

        Duration duration = Duration.of("1 day");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        duration = Duration.of("1day");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        duration = Duration.of("1d");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        duration = Duration.of("1 d");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        duration = Duration.of("1days");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        duration = Duration.of("1days");
        Assert.assertEquals(1L, duration.toDays());
        Assert.assertEquals(24L, duration.toHours());
        Assert.assertEquals(1440L, duration.toMinutes());
        Assert.assertEquals(86400L, duration.toSeconds());
        Assert.assertEquals(86400000L, duration.toMillis());
        Assert.assertEquals(86400000000L, duration.toMicros());
        Assert.assertEquals(86400000000000L, duration.toNanos());

        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2h"));
        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2 h"));
        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2 hour"));
        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2hour"));
        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2 hours"));
        Assert.assertEquals(new Duration(2, TimeUnit.HOURS), Duration.of("2hours"));

        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2min"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2m"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2minute"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2minutes"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2 min"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2 m"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2 minute"));
        Assert.assertEquals(new Duration(2, TimeUnit.MINUTES), Duration.of("2 minutes"));

        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2s"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2sec"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2second"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2seconds"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2 s"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2 sec"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2 second"));
        Assert.assertEquals(new Duration(2, TimeUnit.SECONDS), Duration.of("2 seconds"));

        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2ms"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2milli"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2millisecond"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2milliseconds"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2 ms"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2 millis"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2 millisecond"));
        Assert.assertEquals(new Duration(2, TimeUnit.MILLISECONDS), Duration.of("2 milliseconds"));

    }


    @Test
    public void testHuman() {
        Assert.assertEquals("1 day", Duration.of("86400000000000 nanos").toHumanReadable());
        Assert.assertEquals("1 day 3 hours 23 minutes 20 seconds", Duration.of("98600 sec").toHumanReadable());
    }
}
