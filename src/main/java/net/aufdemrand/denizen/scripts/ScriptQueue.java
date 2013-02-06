package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ScriptQueue implements Listener {

    protected static int totalQueues = 0;

    @Override
    public String toString() {
        return "ScriptQueue(" + id + ")";
    }

    public static String _getStats() {
        return "Total number of queues created: '"
                + totalQueues
                + "', currently active queues: '"
                + _queues.size() +  "'.";
    }

    public static String _getNextId() {
        return String.valueOf(totalQueues + 1);
    }

    public static Map<String, ScriptQueue> _queues = new ConcurrentHashMap<String, ScriptQueue>();

    public static Collection<ScriptQueue> _getQueues() {
        return _queues.values();
    }

    public static ScriptQueue _getQueue(String id) {
        // Get id if not specified.
        if (id == null) id = String.valueOf(_getNextId());
        ScriptQueue scriptQueue;
        // Does the queue already exist?
        if (_queueExists(id))
            scriptQueue = _queues.get(id.toUpperCase());
            // If not, create a new one.
        else {
            scriptQueue = new ScriptQueue(id, Settings.InteractDelayInTicks());
        }
        // Return the queue
        return scriptQueue;
    }

    public static ScriptQueue _getInstantQueue(String id) {
        // Get id if not specified.
        if (id == null) id = String.valueOf(_getNextId());
        ScriptQueue scriptQueue;
        // Does the queue already exist?
        if (_queueExists(id))
            scriptQueue = _queues.get(id.toUpperCase());
            // If not, create a new one.
        else {
            scriptQueue = new ScriptQueue(id, 0);
        }
        return scriptQueue;
    }

    public static boolean _queueExists(String id) {
        return _queues.containsKey(id.toUpperCase());
    }




    protected String id;
    protected int timeout = 10;

    // Keep track of Bukit's Scheduler taskId for the engine, for when it times out.
    protected int taskId;

    // This is the speed of the engine, the # of ticks between each revolution.
    protected int ticks;

    // List of ScriptEntries in the queue
    protected List<ScriptEntry> scriptEntries = new ArrayList<ScriptEntry>();

    // If this number is larger than getCurrentTimeMillis, the queues will delay execution
    protected long delay = 0;

    protected String context = null;

    protected ScriptEntry lastEntryExecuted = null;

    public void setLastEntryExecuted(ScriptEntry entry) {
        lastEntryExecuted = entry;
    }

    public ScriptEntry getLastEntryExecuted() {
        return lastEntryExecuted;
    }

    public void clear() {
        scriptEntries.clear();
    }

    protected boolean paused = false;

    protected ScriptQueue(String id, int ticks) {
        this.id = id.toUpperCase();
        _queues.put(id.toUpperCase(), this);
        totalQueues++;
        this.ticks = ticks;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setSpeed(int ticks) {
        this.ticks = ticks;
    }

    public void delayUntil(long delay) {
        this.delay = delay;
    }

    public void stop() {
        Bukkit.getServer().getScheduler().cancelTask(taskId);
        _queues.remove(id);
    }

    public void start() {
        if (ticks > 0)
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        @Override
                        public void run() {
                        // Turn the engine
                        revolve();
                        }
                    }, ticks, ticks);
        revolve();
    }

    private void revolve() {
        // Check timeout
        if (timeout == 0) stop();
        if (scriptEntries.isEmpty() && timeout > 0) timeout--;
        // Check if this Queue is able to revolve:
        // 1) Isn't paused
        if (paused) return;
        // 2) Isn't delayed/waiting
        if (delay > System.currentTimeMillis()) return;

        DenizenAPI.getCurrentInstance().getScriptEngine().revolve(this);
    }

    public ScriptQueue addEntries(List<ScriptEntry> entries) {
        for (ScriptEntry entry : entries) {
            entry.setSendingQueue(this);
            this.scriptEntries.add(entry);
        }
        return this;
    }

    public ScriptQueue injectEntries(List<ScriptEntry> entries, int position) {
        if (position > scriptEntries.size() || position < 0) position = 1;
        if (scriptEntries.size() == 0) position = 0;
        scriptEntries.addAll(position, entries);
        return this;
    }

    public boolean removeEntry(int position) {
        if (scriptEntries.size() < position) return false;
        scriptEntries.remove(position);
        return true;
    }

    public ScriptEntry getEntry(int position) {
        if (scriptEntries.size() < position) return null;
        return scriptEntries.get(position);
    }

    public ScriptQueue injectEntry(ScriptEntry entry, int position) {
        if (position > scriptEntries.size() || position < 0) position = 1;
        if (scriptEntries.size() == 0) position = 0;
        scriptEntries.add(position, entry);
        return this;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getQueueSize() {
        return scriptEntries.size();
    }
}
