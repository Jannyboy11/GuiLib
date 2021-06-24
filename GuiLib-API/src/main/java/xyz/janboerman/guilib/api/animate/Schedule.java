package xyz.janboerman.guilib.api.animate;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * A schedule for playing animations.
 */
public interface Schedule {

    /**
     * Resets the schedule to its initial state.
     */
    public void reset();

    /**
     * Get number of ticks until the next frame is up for display.
     * @return the empty OptionalLong if this schedule is finished, otherwise an OptionalLong containing the number of ticks until the next frame
     */
    public OptionalLong next();

    /**
     * Get a copy of this schedule.
     * @return a new Schedule
     */
    public Schedule clone();

    /**
     * A schedule that serves frames seperated by a delays.
     * @param delays the delays between frames
     * @return a new Schedule
     */
    public static Schedule of(long... delays) {
        if (delays.length == 1) {
            return once(delays[0]);
        }

        for (long delay : delays) {
            if (delay < 0) throw new IllegalArgumentException("Negative delay: " + delay);
        }

        return new ArraySchedule(Arrays.copyOf(delays, delays.length));
    }

    /**
     * A schedule that only serves one frame, after an initial delay
     * @param delay the initial delay in ticks
     * @return a new Schedule
     */
    public static Schedule once(long delay) {
        if (delay < 0L) throw new IllegalArgumentException("Negative delay: " + delay);

        return new OneTimeSchedule(delay);
    }

    /**
     * A schedule that only serves one frame
     * @return a new Schedule
     */
    public static Schedule now() {
        return once(0L);
    }

    /**
     * A schedule that serves frames at a fixed rate
     * @param period the number of ticks between frames
     * @return a new schedule
     */
    public static Schedule fixedRate(long period) {
        if (period < 0L) throw new IllegalArgumentException("Negative period: " + period);

        return new FixedRateSchedule(period);
    }

    /**
     * A schedule that serves a limited number of frames, limited by the number of ticks passed.
     * @param totalTicks the upper bound of ticks passed
     * @return a new Schedule
     */
    public default Schedule limitTime(long totalTicks) {
        if (totalTicks < 0) throw new IllegalArgumentException("Negative time limit: " + totalTicks);

        return new TimeLimitedSchedule(this, totalTicks);
    }

    /**
     * A schedule that servers a limited number of frames, limited by the number of frames passed.
     * @param totalSteps the upper bound of frames passed
     * @return a new Schedule
     */
    public default Schedule limitSteps(long totalSteps) {
        if (totalSteps < 0) throw new IllegalArgumentException("Negative step limit: " + totalSteps);

        return new StepLimitedSchedule(this, totalSteps);
    }

    /**
     * A schedule that first serves up frames according to the current schedule, and then according to the second schedule.
     * @param andThen the second schedule
     * @return a new Schedule, or the current schedule if it is infinite
     */
    public default Schedule append(Schedule andThen) {
        return new ConcatSchedule(this, andThen);
    }

    /**
     * A schedule that loops the current schedule.
     * @return a new Schedule, or the current schedule if it is infinite
     */
    public default Schedule repeat() {
        return new RepeatingSchedule(this);
    }
}

class RepeatingSchedule implements Schedule {
    private final Schedule source;

    RepeatingSchedule(Schedule source) {
        this.source = source;
    }

    @Override
    public void reset() {
        source.reset();
    }

    @Override
    public OptionalLong next() {
        OptionalLong next = source.next();
        if (next.isPresent()) return next;
        source.reset();
        return source.next();
    }

    @Override
    public Schedule clone() {
        return new RepeatingSchedule(source.clone());
    }

    @Override
    public Schedule append(Schedule andThen) {
        return this;
    }

    @Override
    public Schedule repeat() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof RepeatingSchedule)) return false;

        RepeatingSchedule that = (RepeatingSchedule) obj;
        return Objects.equals(this.source, that.source);
    }

    @Override
    public String toString() {
        return "RepeatingSchedule(source=" + source + ")";
    }
}

class ArraySchedule implements Schedule {

    private int currentIndex;
    private long[] delays;

    private ArraySchedule(long[] delays, int currentIndex) {
        this.delays = delays;
        this.currentIndex = currentIndex;
    }

    ArraySchedule(long[] delays) {
        this.delays = Objects.requireNonNull(delays, "delays cannot be null");
    }

    @Override
    public void reset() {
        currentIndex = 0;
    }

    @Override
    public OptionalLong next() {
        if (currentIndex >= delays.length) return OptionalLong.empty();

        return OptionalLong.of(delays[currentIndex++]);
    }

    @Override
    public Schedule clone() {
        return new ArraySchedule(Arrays.copyOf(delays, delays.length), currentIndex);
    }

    @Override
    public Schedule limitSteps(long totalSteps) {
        if (totalSteps < this.delays.length) {
            int length = (int) this.delays.length;
            long[] delays = new long[length];
            System.arraycopy(this.delays, 0, delays, 0, length);
            return new ArraySchedule(delays, currentIndex);
        } else {
            return this;
        }
    }

    @Override
    public Schedule append(Schedule andThen) {
        if (andThen instanceof ArraySchedule) {
            ArraySchedule that = (ArraySchedule) andThen;

            long[] delays = new long[this.delays.length + that.delays.length];
            System.arraycopy(this.delays, 0, delays, 0, this.delays.length);
            System.arraycopy(that.delays, 0, delays, this.delays.length, that.delays.length);

            return new ArraySchedule(delays, currentIndex);
        } else {
            return Schedule.super.append(andThen);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentIndex, Arrays.hashCode(delays));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ArraySchedule)) return false;

        ArraySchedule that = (ArraySchedule) obj;
        return this.currentIndex == that.currentIndex
                && Arrays.equals(this.delays, that.delays);
    }

    @Override
    public String toString() {
        return "ArraySchedule(delays=" + Arrays.toString(delays) + ",currentIndex=" + currentIndex + ")";
    }
}

class ConcatSchedule implements Schedule {

    final Schedule one, two;

    ConcatSchedule(Schedule one, Schedule two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public void reset() {
        one.reset();
        two.reset();
    }

    @Override
    public OptionalLong next() {
        OptionalLong next = one.next();
        if (next.isPresent()) return next;

        next = two.next();
        return next;
    }

    @Override
    public Schedule clone() {
        return new ConcatSchedule(one.clone(), two.clone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(one, two);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ConcatSchedule)) return false;

        ConcatSchedule that = (ConcatSchedule) obj;
        return Objects.equals(this.one, that.one)
                && Objects.equals(this.two, that.two);
    }

    @Override
    public String toString() {
        return "ConcatSchedule(one=" + one + ",two=" + two + ")";
    }
}

class OneTimeSchedule implements Schedule {
    private boolean done = false;
    final long when;

    private OneTimeSchedule(boolean done, long when) {
        this.done = done;
        this.when = when;
    }

    OneTimeSchedule(long delay) {
        this.when = delay;
    }

    @Override
    public void reset() {
        done = false;
    }

    @Override
    public OptionalLong next() {
        if (done) {
            return OptionalLong.empty();
        } else {
            done = true;
            return OptionalLong.of(when);
        }
    }

    @Override
    public Schedule clone() {
        return new OneTimeSchedule(done, when);
    }

    @Override
    public Schedule repeat() {
        return Schedule.fixedRate(when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(done, when);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof OneTimeSchedule)) return false;

        OneTimeSchedule that = (OneTimeSchedule) obj;
        return this.done == that.done
                && this.when == that.when;
    }

    @Override
    public String toString() {
        return "OneTimeSchedule(done=" + done + ",when=" + when + ")";
    }
}

class StepLimitedSchedule implements Schedule {

    final long stepLimit;
    final Schedule source;
    long stepsPassed = 0L;

    private StepLimitedSchedule(Schedule source, long stepLimit, long stepsPassed) {
        this.stepLimit = stepLimit;
        this.source = source;
        this.stepsPassed = stepsPassed;
    }

    StepLimitedSchedule(Schedule source, long stepLimit) {
        this.stepLimit = stepLimit;
        this.source = source;
    }

    @Override
    public void reset() {
        source.reset();
        stepsPassed = 0L;
    }

    @Override
    public OptionalLong next() {
        if (stepsPassed >= stepLimit) return OptionalLong.empty();
        stepsPassed += 1L;
        return source.next();
    }

    @Override
    public Schedule clone() {
        return new StepLimitedSchedule(source.clone(), stepLimit, stepsPassed);
    }

    @Override
    public Schedule limitSteps(long totalSteps) {
        if (totalSteps >= stepLimit) {
            return this;
        } else {
            return new StepLimitedSchedule(source, totalSteps, stepsPassed);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepLimit, source, stepsPassed);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof StepLimitedSchedule)) return false;

        StepLimitedSchedule that = (StepLimitedSchedule) obj;
        return this.stepLimit == that.stepLimit
                && this.stepsPassed == that.stepsPassed
                && Objects.equals(this.source, that.source);
    }

    @Override
    public String toString() {
        return "StepLimitedSchedule(source=" + source + ",stepLimit=" + stepLimit + ",stepsPassed=" + stepsPassed + ")";
    }
}

class TimeLimitedSchedule implements Schedule {

    final long timeLimit;
    final Schedule source;
    long timePassed = 0L;

    private TimeLimitedSchedule(Schedule source, long timeLimit, long timePassed) {
        this.source = source;
        this.timeLimit = timeLimit;
        this.timePassed = timePassed;
    }

    TimeLimitedSchedule(Schedule source, long timeLimit) {
        this.source = source;
        this.timeLimit = timeLimit;
    }

    @Override
    public void reset() {
        source.reset();
        timePassed = 0L;
    }

    @Override
    public OptionalLong next() {
        OptionalLong next = source.next();
        if (next.isEmpty()) return OptionalLong.empty();

        long add = next.getAsLong();
        timePassed += add;
        if (timePassed > timeLimit) return OptionalLong.empty();

        return next;
    }

    @Override
    public Schedule clone() {
        return new TimeLimitedSchedule(source.clone(), timeLimit, timePassed);
    }

    @Override
    public Schedule limitTime(long totalTicks) {
        if (totalTicks >= timeLimit) {
            return this;
        } else {
            return new TimeLimitedSchedule(source, totalTicks, timePassed);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeLimit, timePassed, source);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TimeLimitedSchedule)) return false;

        TimeLimitedSchedule that = (TimeLimitedSchedule) obj;
        return this.timeLimit == that.timeLimit
                && this.timePassed == that.timePassed
                && Objects.equals(this.source, that.source);
    }

    @Override
    public String toString() {
        return "TimeLimitedSchedule(source=" + source + ",timeLimit=" + timeLimit + ",timePassed=" + timePassed + ")";
    }
}

class FixedRateSchedule implements Schedule {

    final long period;

    FixedRateSchedule(long period) {
        this.period = period;
    }

    @Override
    public void reset() {
    }

    @Override
    public OptionalLong next() {
        return OptionalLong.of(period);
    }

    @Override
    public Schedule limitSteps(long totalSteps) {
        if (totalSteps == 1) {
            return Schedule.once(period);
        } else {
            return Schedule.super.limitSteps(totalSteps);
        }
    }

    @Override
    public Schedule clone() {
        return this;
    }

    @Override
    public Schedule append(Schedule andThen) {
        return this;
    }

    @Override
    public Schedule repeat() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(period);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FixedRateSchedule)) return false;

        FixedRateSchedule that = (FixedRateSchedule) obj;
        return this.period == that.period;
    }

    @Override
    public String toString() {
        return "FixedRateSchedule(period=" + period + ")";
    }
}
