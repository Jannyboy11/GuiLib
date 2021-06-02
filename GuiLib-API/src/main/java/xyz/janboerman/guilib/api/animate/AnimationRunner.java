package xyz.janboerman.guilib.api.animate;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import xyz.janboerman.guilib.api.util.IntBiConsumer;

import java.util.Objects;

public final class AnimationRunner<Item> {

    private final Plugin plugin;
    private final Animation animation;
    private final IntBiConsumer container;

    private AnimationState status = AnimationState.NOT_STARTED;
    private BukkitTask task = null;

    public AnimationRunner(Plugin plugin, Animation animation, IntBiConsumer<Item> container) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.animation = Objects.requireNonNull(animation, "animation cannot be null");
        this.container = Objects.requireNonNull(container, "container cannot be null");
    }

    private void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean play(long initialDelay, long period) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        task = scheduler.runTaskTimer(plugin, () -> {
            if (!animation.hasNextFrame()) {
                status = AnimationState.FINISHED;
                cancelTask();
            } else {
                Frame frame = animation.nextFrame();
                frame.apply(container);
            }
        }, initialDelay, period);

        if (animation.hasNextFrame()) {
            status = AnimationState.RUNNING;
            return true;
        } else {
            status = AnimationState.FINISHED;
            return false;
        }
    }

    public void stop() {
        if (status != AnimationState.FINISHED) {
            status = AnimationState.PAUSED;
        }

        cancelTask();
    }

    public void reset() {
        cancelTask();
        animation.reset();
        status = AnimationState.NOT_STARTED;
    }

    public AnimationState getStatus() {
        return status;
    }
}
