package xyz.janboerman.guilib.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public interface Task {

    public void cancel();

    public boolean isCancelled();
}

class FoliaTask implements Task {

    private final ScheduledTask task;

    FoliaTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }
}

class BukkitTask implements Task {

    private final org.bukkit.scheduler.BukkitTask task;

    BukkitTask(org.bukkit.scheduler.BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

}