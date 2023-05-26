package xyz.janboerman.guilib.api.animate;

import xyz.janboerman.guilib.util.Task;

abstract class CommonRunnable implements Runnable {

    protected Task task;
    private boolean cancelled;

    abstract long stepDelay();

    abstract boolean isDone();

    protected void cancel() {
        if (task != null) task.cancel();
        cancelled = true;
    }

    protected boolean isCancelled() {
        return cancelled || (task != null && task.isCancelled());
    }

}

class RunOnce extends CommonRunnable {

    final long delay;
    final Runnable runnable;
    private boolean done;

    RunOnce(long delay, Runnable runnable) {
        this.delay = delay;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
        done = true;
    }

    @Override
    long stepDelay() {
        return delay;
    }

    @Override
    boolean isDone() {
        return done || isCancelled();
    }

}

class RunFixedRate extends CommonRunnable {

    final long period;
    final Runnable runnable;

    RunFixedRate(long period, Runnable runnable) {
        this.period = period;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

    @Override
    long stepDelay() {
        return period;
    }

    @Override
    boolean isDone() {
        return isCancelled();
    }

}

class RunStepLimited extends CommonRunnable {

    final long stepLimit;
    final CommonRunnable source;
    long stepsPassed;

    RunStepLimited(CommonRunnable source, long stepLimit, long stepsPassed) {
        this.stepLimit = stepLimit;
        this.source = source;
        this.stepsPassed = stepsPassed;
    }

    @Override
    public void run() {
        if (source.isDone()) {
            cancel();
        }
        else if (stepsPassed >= stepLimit) {
            cancel();
        }
        else {
            stepsPassed += 1;
            source.run();
        }
    }

    @Override
    long stepDelay() {
        return source.stepDelay();
    }

    @Override
    boolean isDone() {
        return source.isDone() || stepsPassed >= stepLimit || isCancelled();
    }

}

class RunTimeLimited extends CommonRunnable {

    final long timeLimit;
    final CommonRunnable source;
    long timePassed;

    RunTimeLimited(CommonRunnable source, long timeLimit, long timePassed) {
        this.timeLimit = timeLimit;
        this.source = source;
        this.timePassed = timePassed;
    }

    @Override
    public void run() {
        if (source.isCancelled()) {
            cancel();
        }
        else if (timePassed > timeLimit) {
            cancel();
        }
        else {
            timePassed += source.stepDelay();
            source.run();
        }
    }

    @Override
    long stepDelay() {
        return source.stepDelay();
    }

    @Override
    boolean isDone() {
        return source.isDone() || timePassed > timeLimit || isCancelled();
    }
}

class RunConcat extends CommonRunnable {

    final CommonRunnable one, two;

    RunConcat(CommonRunnable one, CommonRunnable two) {
        this.one = one;
        this.two = two;
    }

    @Override
    long stepDelay() {
        if (one.isDone()) {
            return two.stepDelay();
        } else {
            return one.stepDelay();
        }
    }

    @Override
    boolean isDone() {
        return (one.isDone() && two.isDone()) || isCancelled();
    }

    @Override
    public void run() {
        if (!one.isDone()) {
            one.run();
        } else if (!two.isDone()) {
            two.run();
        } else {
            cancel();
        }
    }
}

