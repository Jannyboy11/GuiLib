package xyz.janboerman.guilib.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

public interface Scheduler {

    public Task runTaskLater(Plugin plugin, HumanEntity viewer, Runnable task);

    public Task runTaskLater(Plugin plugin, Runnable task);

    public Task runTaskLater(Plugin plugin, Runnable task, long ticksDelay);

    public Task runTaskTimer(Plugin plugin, Runnable task, long ticksInitialDelay, long ticksPeriod);

    public static Scheduler get() {
        return SchedulerAccess.SCHEDULER;
    }

}

class SchedulerAccess {
    static final Scheduler SCHEDULER = FoliaSupport.isFolia() ? new FoliaScheduler() : new BukkitScheduler();

    private SchedulerAccess() {}
}

class FoliaScheduler implements Scheduler {

    FoliaScheduler() {}

    @Override
    public FoliaTask runTaskLater(Plugin plugin, HumanEntity viewer, Runnable task) {
        return new FoliaTask(viewer.getScheduler().run(plugin, scheduledTask -> task.run(), null));
    }

    @Override
    public FoliaTask runTaskLater(Plugin plugin, Runnable task) {
        return new FoliaTask(plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run()));
    }

    @Override
    public FoliaTask runTaskLater(Plugin plugin, Runnable task, long ticksDelay) {
        return new FoliaTask(plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), ticksDelay));
    }

    @Override
    public FoliaTask runTaskTimer(Plugin plugin, Runnable task, long ticksInitialDelay, long ticksPeriod) {
        return new FoliaTask(plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), ticksInitialDelay, ticksPeriod));
    }
}

class BukkitScheduler implements Scheduler {

    BukkitScheduler() {}

    @Override
    public BukkitTask runTaskLater(Plugin plugin, HumanEntity viewer, Runnable task) {
        return new BukkitTask(plugin.getServer().getScheduler().runTask(plugin, task));
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task) {
        return new BukkitTask(plugin.getServer().getScheduler().runTask(plugin, task));
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task, long ticksDelay) {
        return new BukkitTask(plugin.getServer().getScheduler().runTaskLater(plugin, task, ticksDelay));
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long ticksInitialDelay, long ticksPeriod) {
        return new BukkitTask(plugin.getServer().getScheduler().runTaskTimer(plugin, task, ticksInitialDelay, ticksPeriod));
    }
}