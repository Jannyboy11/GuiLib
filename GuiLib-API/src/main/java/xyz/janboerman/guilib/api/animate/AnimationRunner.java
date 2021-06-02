package xyz.janboerman.guilib.api.animate;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.api.util.IntBiConsumer;

import java.util.Objects;
import java.util.OptionalLong;

/**
 * A player for {@link Animation}s.
 * @param <Item> the type of item in the container this animation player is used on
 */
public final class AnimationRunner<Item> {

    private final Plugin plugin;
    private final Animation animation;
    private final IntBiConsumer<Item> container;

    private AnimationState status = AnimationState.NOT_STARTED;
    private BukkitTask task = null;

    /**
     * Creates the AnimationRunner.
     * @param plugin the plugin used to run the animation task
     * @param animation the animation
     * @param container the container. This is usually {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)} or {@link xyz.janboerman.guilib.api.menu.MenuHolder#setButton(int, MenuButton)}.
     */
    public AnimationRunner(Plugin plugin, Animation animation, IntBiConsumer<Item> container) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.animation = Objects.requireNonNull(animation, "animation cannot be null");
        this.container = Objects.requireNonNull(container, "container cannot be null");
    }

    /**
     * Get the status of this AnimationRunner.
     * @return the status
     */
    public AnimationState getStatus() {
        return status;
    }

    /**
     * Play the animation frames according to a schedule.
     * @param schedule the schedule
     * @return true if the animation started successfully, otherwise false
     */
    public boolean play(Schedule schedule) {
        //throw IllegalStateException if the AnimationRunner was already busy.
        checkStartAllowed();

        //run the schedule
        return runSchedule(schedule);
    }

    /**
     * Makes the animation stop playing.
     * This method does nothing if the animation is already paused or finished.
     */
    public void stop() {
        if (status != AnimationState.FINISHED) {
            status = AnimationState.PAUSED;
        }
        cancelTask();
    }

    /**
     * Resets the animation, making the player ready again to start over from the beginning.
     */
    public void reset() {
        cancelTask();
        animation.reset();
        status = AnimationState.NOT_STARTED;
    }

    private boolean runSchedule(Schedule schedule) {
        //try to short-circuit a few common schedule structures
        CommonRunnable sr = tryComputeCommonRunnable(schedule);
        if (sr != null) {
            //at least we got a ScheduleRunnable successfully
            //now let's try to run it (this might fail)
            task = trySpecialCaseRun(sr, schedule);
        }

        if (task == null) {
            //fallback
            task = tryFallbackRun(schedule);
        }

        return recomputeStatus() == AnimationState.RUNNING;
    }

    private BukkitTask trySpecialCaseRun(CommonRunnable sr, Schedule schedule) {
        if (schedule instanceof Once) {
            return sr.runTaskLater(plugin, ((Once) schedule).when);
        } else if (schedule instanceof FixedRateSchedule) {
            return sr.runTaskTimer(plugin, 0L, ((FixedRateSchedule) schedule).period);
        } else if (schedule instanceof StepLimitedSchedule) {
            trySpecialCaseRun(sr, ((StepLimitedSchedule) schedule).source);
        } else if (schedule instanceof TimeLimitedSchedule) {
            return trySpecialCaseRun(sr, ((TimeLimitedSchedule) schedule).source);
        } else if (schedule instanceof ConcatSchedule) {
            ConcatSchedule concatSchedule = (ConcatSchedule) schedule;
            if (concatSchedule.one instanceof Once && concatSchedule.two instanceof RunFixedRate) {
                return sr.runTaskTimer(plugin, ((Once) concatSchedule.one).when, ((RunFixedRate) concatSchedule.two).period);
            }
        }

        return null;
    }

    private CommonRunnable tryComputeCommonRunnable(Schedule schedule) {
        if (schedule instanceof Once) {
            Once s = (Once) schedule;
            return new RunOnce(s.when, this::showFrame);
        } else if (schedule instanceof FixedRateSchedule) {
            FixedRateSchedule s = (FixedRateSchedule) schedule;
            return new RunFixedRate(s.period, this::showFrame);
        } else if (schedule instanceof ConcatSchedule) {
            ConcatSchedule s = (ConcatSchedule) schedule;
            CommonRunnable one = tryComputeCommonRunnable(s.one);
            CommonRunnable two = tryComputeCommonRunnable(s.two);
            if (one != null && two != null) {
                return new RunConcat(one, two);
            }
        } else if (schedule instanceof StepLimitedSchedule) {
            StepLimitedSchedule s = (StepLimitedSchedule) schedule;
            CommonRunnable sr = tryComputeCommonRunnable(s.source);
            if (sr != null) {
                return new RunStepLimited(sr, s.stepLimit, s.stepsPassed);
            }
        } else if (schedule instanceof TimeLimitedSchedule) {
            TimeLimitedSchedule s = (TimeLimitedSchedule) schedule;
            CommonRunnable sr = tryComputeCommonRunnable(s.source);
            if (sr != null) {
                return new RunTimeLimited(sr, s.timeLimit, s.timePassed);
            }
        }

        return null;
    }

    private BukkitTask tryFallbackRun(Schedule schedule) {
        OptionalLong nextTick = schedule.next();

        if (nextTick.isEmpty()) {
            this.status = AnimationState.FINISHED;
            stop();
            return null;
        } else {
            long delay = nextTick.getAsLong();
            return getScheduler().runTaskLater(plugin, () -> {
                showFrame();
                runSchedule(schedule);
            }, delay);
        }
    }

    private void showFrame() {
        if (!animation.hasNextFrame()) {
            status = AnimationState.FINISHED;
            cancelTask();
        } else {
            Frame frame = animation.nextFrame();
            frame.apply(container);
        }
    }

    private void checkStartAllowed() {
        if (task != null) throw new IllegalStateException("Animation already started");
    }

    private AnimationState recomputeStatus() {
        if (animation.hasNextFrame()) {
            if (task != null && !task.isCancelled()) {
                status = AnimationState.RUNNING;
            } else {
                status = AnimationState.PAUSED;
            }
        } else {
            status = AnimationState.FINISHED;
        }

        return status;
    }

    private void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private BukkitScheduler getScheduler() {
        return plugin.getServer().getScheduler();
    }

    /**
     * Plays the animation frames at a fixed rate.
     * @param initialDelay the number of ticks to wait before the first frame is shown
     * @param period the number of ticks between frames
     * @return whether the animation started successfully
     * @deprecated use {@link #play(Schedule)} instead
     */
    @Deprecated(forRemoval = true)
    public boolean play(long initialDelay, long period) {
        return play(Schedule.once(initialDelay).append(Schedule.fixedRate(period)));
    }
}
