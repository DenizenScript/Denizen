package net.aufdemrand.denizen.scripts.queues.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;

public class TimedQueue extends ScriptQueue implements Delayable {

    /**
     * Gets a TimedQueue instance.
     *
     * If a queue already exists with the given id, it will return that instance,
     * which may be currently running, unless the type of queue is not a TimedQueue.
     * If a queue does not exist, a new stopped queue is created instead.
     *
     * IDs are case insensitive.  If having an easy-to-recall ID is not necessary, just
     * pass along null as the id, and it will use ScriptQueue's static method _getNextId()
     * which will return a random UUID.
     *
     * The speed node will be automatically read from the configuration,
     * and new ScriptQueues may need further information before they
     * can start(), including entries, delays, loops, and possibly context.
     *
     * @param id  unique id of the queue
     * @return  a TimedQueue
     */
    public static TimedQueue getQueue(String id) {
        // Get id if not specified.
        if (id == null) id = String.valueOf(_getNextId());
        TimedQueue scriptQueue;
        // Does the queue already exist? Get it if it does.
        if (_queueExists(id))
            scriptQueue = (TimedQueue) ScriptQueue._queues.get(id.toUpperCase());
            // If not, create a new one.
        else {
            scriptQueue = new TimedQueue(id,
                    Duration.valueOf(Settings.ScriptQueueSpeed()));
        }
        // Return the queue
        return scriptQueue;
    }


    /////////////////////
    // Private instance fields and constructors
    /////////////////////

   // Keep track of Bukkit's Scheduler taskId
    // for the engine, used when it times out.
    private int task_id;

    // The speed of the engine, the # of ticks
    // between each revolution. Use setSpeed()
    // to change this.
    private long ticks;

    // ScriptQueues can be paused mid-rotation.
    // The next entry will be held up until
    // un-paused.
    protected boolean paused = false;

    // The delay in ticks can put off the
    // start of a queue
    protected long delay_ticks = 0;

    @Override
    public void delayFor(Duration duration) {
        delay_ticks = System.currentTimeMillis() + duration.getMillis();
    }


    public TimedQueue(String id, Duration timing) {
                super(id);
    }



    /////////////////////
    // Public instance setters and getters
    /////////////////////

    /**
     * Gets the speed of the queue. This is the
     * time in between each revolution. '
     *
     * @return a Duration of the speed.
     */
    public Duration getSpeed() {
        return new Duration(ticks);
    }

    /**
     * Pauses the queue. Paused queues will check
     * to be re-resumed every 'rotation', defined
     * by the speed of the queue.
     *
     * @param paused
     */
    @Override
    public TimedQueue setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    /**
     * Checks if the queue is currently paused.
     *
     * @return  true if paused.
     */
    @Override
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the speed of a queue. Uses bukkit's 'ticks', which is
     * 20 ticks per second.
     *
     * @param ticks  the number of ticks between each rotation.
     */
    public TimedQueue setSpeed(long ticks) {
        this.ticks = ticks;
        return this;
    }

    @Override
    protected void onStart() {
        task_id = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override public void run() { revolve(); }
                }, 0, ticks == 0 ? 1 : ticks);
    }

    @Override
    protected void onStop() {
        Bukkit.getScheduler().cancelTask(task_id);
    }

    @Override
    protected boolean shouldRevolve() {
        // Check if this Queue isn't paused
        if (paused) return false;

        // If it's delayed, schedule it for later
        if (delay_ticks > System.currentTimeMillis()) return false;

        return true;
    }



}
