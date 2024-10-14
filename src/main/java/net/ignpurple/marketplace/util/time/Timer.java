package net.ignpurple.marketplace.util.time;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Timer {
    private final long actualMilliseconds;
    private final TimeUnit truncate;
    private final List<String> format;
    private long milliseconds;

    public Timer(Builder builder) {
        this.milliseconds = builder.milliseconds;
        this.actualMilliseconds = builder.milliseconds;
        this.truncate = builder.truncate;
        this.format = builder.format;
    }

    public long getMilliseconds() {
        return this.actualMilliseconds;
    }

    public Timer addTime(long amount, TimeUnit type) {
        this.milliseconds += type.toMillis(amount);
        return this;
    }

    public String build() {
        int counter = 0;
        final StringBuilder result = new StringBuilder();

        this.append(result, this.negateAmount(TimeUnit.MILLISECONDS.toDays(this.milliseconds), TimeUnit.DAYS), TimeUnit.DAYS, counter++);
        this.append(result, this.negateAmount(TimeUnit.MILLISECONDS.toHours(this.milliseconds), TimeUnit.HOURS), TimeUnit.HOURS, counter++);
        this.append(result, this.negateAmount(TimeUnit.MILLISECONDS.toMinutes(this.milliseconds), TimeUnit.MINUTES), TimeUnit.MINUTES, counter++);
        this.append(result, this.negateAmount(TimeUnit.MILLISECONDS.toSeconds(this.milliseconds), TimeUnit.SECONDS), TimeUnit.SECONDS, counter++);
        this.append(result, this.negateAmount(TimeUnit.MILLISECONDS.toMillis(this.milliseconds), TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, counter);

        if (result.isEmpty()) {
            return "now";
        }

        return result.toString();
    }

    private void append(StringBuilder result, long amount, TimeUnit time, int counter) {
        if (amount != 0 && this.truncate.ordinal() <= time.ordinal()) {
            result.append(result.isEmpty() ? "" : ", ").append(amount).append(this.getSuffix(counter));
        }
    }

    private long negateAmount(long amount, TimeUnit type) {
        this.milliseconds -= type.toMillis(amount);
        return amount;
    }

    public String getSuffix(int count) {
        return this.format.size() > count ? this.format.get(count) : "";
    }

    public enum Format {
        SHORT(List.of("d", "h", "m", "s")),
        SHORT_WITH_MILLIS(List.of("d", "h", "m", "s", "ms")),
        LONG(List.of(" days", " hours", " minutes", " seconds")),
        LONG_WITH_MILLIS(List.of(" days", " hours", " minutes", " seconds", " milliseconds"));

        private final List<String> units;

        private Format(List<String> units) {
            this.units = units;
        }

        public List<String> getUnits() {
            return this.units;
        }
    }

    public static class Builder {
        private long milliseconds;
        private TimeUnit truncate = TimeUnit.SECONDS;
        private List<String> format = Format.SHORT.getUnits();

        public Builder withMilliseconds(long milliseconds) {
            this.milliseconds = milliseconds;
            return this;
        }

        public Builder timeSince(Instant instant) {
            this.milliseconds = System.currentTimeMillis() - (instant.getEpochSecond() * 1_000);
            return this;
        }

        public Builder timeSince(long milliseconds) {
            this.milliseconds = System.currentTimeMillis() - milliseconds;
            return this;
        }

        public Builder timeTo(Instant instant) {
            this.milliseconds = instant.getEpochSecond() * 1_000 - System.currentTimeMillis();
            return this;
        }

        public Builder timeTo(long milliseconds) {
            this.milliseconds = milliseconds - System.currentTimeMillis();
            return this;
        }

        public Builder truncateTo(TimeUnit truncate) {
            this.truncate = truncate;
            return this;
        }

        public Builder format(Format format) {
            this.format = format.getUnits();
            return this;
        }

        public Builder format(List<String> format) {
            this.format = format;
            return this;
        }

        public Timer build() {
            return new Timer(this);
        }
    }
}