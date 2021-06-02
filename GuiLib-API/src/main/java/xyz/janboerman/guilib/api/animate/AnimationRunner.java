package xyz.janboerman.guilib.api.animate;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.api.util.IntBiConsumer;

import java.util.Objects;

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

    private void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Plays the animation frames at a fixed rate.
     * @param initialDelay the number of ticks to wait before the first frame is shown
     * @param period the number of ticks between frames
     * @return whether the animation started successfully
     */
    public boolean play(long initialDelay, long period) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (task != null) throw new IllegalStateException("Animation already started");

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

    /**
     * Makes the animation stop playing.
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

    /**
     * Get the status of this AnimationRunner.
     * @return the status
     */
    public AnimationState getStatus() {
        return status;
    }
}
