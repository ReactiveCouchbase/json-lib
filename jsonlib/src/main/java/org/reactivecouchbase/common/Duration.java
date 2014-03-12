package org.reactivecouchbase.common;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Duration {

    public final Long value;
    public final TimeUnit unit;
    private static final Pattern DAYS = Pattern.compile("([0-9]+)");

    public Duration(Long value, TimeUnit unit) {
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(unit);
        this.value = value;
        this.unit = unit;
    }

    public Duration(Integer value, TimeUnit unit) {
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(unit);
        this.value = value.longValue();
        this.unit = unit;
    }

    public Duration(String expression) {
        Preconditions.checkNotNull(expression);
        Duration d = parse(expression);
        this.value = d.value;
        this.unit = d.unit;
    }

    public static Duration of(Long value, TimeUnit unit) {
        return new Duration(value, unit);
    }
    public static Duration of(String expression) {
        return Duration.parse(expression);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Duration)) return false;

        Duration duration = (Duration) o;

        if (unit != duration.unit) return false;
        if (!value.equals(duration.value)) return false;

        return true;
    }

    @Override
    public final int hashCode() {
        int result = value.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }

    public final String toHumanReadable() {
        if (toMillis() == 0L) {
            if (toMicros() == 0L) {
                return value + " nanos";
            } else {
                return value + " microsec";
            }
        }
        if (toMillis() < 1000L) {
            return value + " millis";
        }
        Long seconds = toSeconds();
        Double numyears = Math.floor(seconds / 31536000);
        Double numdays = Math.floor((seconds % 31536000) / 86400);
        Double numhours = Math.floor(((seconds % 31536000) % 86400) / 3600);
        Double numminutes = Math.floor((((seconds % 31536000) % 86400) % 3600) / 60);
        Long numseconds = (((seconds % 31536000) % 86400) % 3600) % 60;
        StringBuilder builder = new StringBuilder();
        if (numyears > 0)   {
            builder.append(numyears.intValue()).append(" year");
            if (numyears > 1) builder.append("s "); else builder.append(" ");
        }
        if (numdays > 0)    {
            builder.append(numdays.intValue()).append(" day");
            if (numdays > 1) builder.append("s "); else builder.append(" ");
        }
        if (numhours > 0)   {
            builder.append(numhours.intValue()).append(" hour");
            if (numhours > 1) builder.append("s "); else builder.append(" ");
        }
        if (numminutes > 0) {
            builder.append(numminutes.intValue()).append(" minute");
            if (numminutes > 1) builder.append("s "); else builder.append(" ");
        }
        if (numseconds > 0) {
            builder.append(numseconds).append(" second");
            if (numseconds > 1) builder.append("s "); else builder.append(" ");
        }
        return builder.toString().trim();
    }

    @Override
    public final String toString() {
        return "Duration{" +
                "value=" + value +
                ", unit=" + unit +
                '}';
    }

    public static final Map<String, TimeUnit> UNITS = Collections.unmodifiableMap(new HashMap <String, TimeUnit >() {{
        put("milli", TimeUnit.MILLISECONDS);
        put("millis", TimeUnit.MILLISECONDS);
        put("millisecond", TimeUnit.MILLISECONDS);
        put("milliseconds", TimeUnit.MILLISECONDS);
        put("sec", TimeUnit.SECONDS);
        put("min", TimeUnit.MINUTES);
        put("minute", TimeUnit.MINUTES);
        put("minutes", TimeUnit.MINUTES);
        put("hours", TimeUnit.HOURS);
        put("hour", TimeUnit.HOURS);
        put("day", TimeUnit.DAYS);
        put("days", TimeUnit.DAYS);
        put("micro", TimeUnit.MICROSECONDS);
        put("micros", TimeUnit.MICROSECONDS);
        put("microsecond", TimeUnit.MICROSECONDS);
        put("microseconds", TimeUnit.MICROSECONDS);
        put("nano", TimeUnit.NANOSECONDS);
        put("nanos", TimeUnit.NANOSECONDS);
        put("nanosecond", TimeUnit.NANOSECONDS);
        put("nanoseconds", TimeUnit.NANOSECONDS);
        put("seconds", TimeUnit.SECONDS);
        put("second", TimeUnit.SECONDS);
        put("ns", TimeUnit.NANOSECONDS);
        put("ms", TimeUnit.MILLISECONDS);
        put("s", TimeUnit.SECONDS);
        put("m", TimeUnit.MINUTES);
        put("h", TimeUnit.HOURS);
        put("d", TimeUnit.DAYS);
    }});

    public static Duration parse(String expression) {
        if (expression.contains(" ")) {
            List<String> parts = Arrays.asList(expression.trim().toLowerCase().split(" "));
            Preconditions.checkArgument(parts.size() == 2);
            Long value = Long.valueOf(parts.get(0));
            String unitOfTime = parts.get(1);
            if (UNITS.containsKey(unitOfTime)) {
                return new Duration(value, UNITS.get(unitOfTime.toLowerCase()));
            } else {
                throw new RuntimeException("Can't parse expression " + expression + ". The format is 'value unit'.");
            }
        } else {
            String unitExpression = expression.replaceAll("([0-9]+)", "").trim().toLowerCase();
            String valueExpression = expression.toLowerCase().replace(unitExpression, "").trim();
            if (UNITS.containsKey(unitExpression)) {
                Long value = Long.valueOf(valueExpression);
                return new Duration(value, UNITS.get(unitExpression));
            }
            throw new RuntimeException("Can't parse expression " + expression + ". The format is 'value unit'.");
        }
    }

    public final long toNanos() {
        return unit.toNanos(value);
    }

    public final long toMicros() {
        return unit.toMicros(value);
    }

    public final long toMillis() {
        return unit.toMillis(value);
    }

    public final long toSeconds() {
        return unit.toSeconds(value);
    }

    public final long toMinutes() {
        return unit.toMinutes(value);
    }

    public final long toHours() {
        return unit.toHours(value);
    }

    public final long toDays() {
        return unit.toDays(value);
    }
}
