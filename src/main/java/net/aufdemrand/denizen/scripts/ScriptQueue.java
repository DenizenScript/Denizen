package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ScriptQueue implements Listener {

    protected static int totalQueues = 0;

    /**
     * Returns the number of queues created in the current instance
     * as well as the number of currently active queues.
     *
     * @return stats
     */
    public static String _getStats() {
        return "Total number of queues created: '"
                + totalQueues
                + "', currently active queues: '"
                + _queues.size() +  "'.";
    }


    /**
     * Gets a random UUID for use in creating a 'nameless' queue.
     *
     * @return String value of a random UUID
     */
    public static String _getNextId() {
        return UUID.randomUUID().toString();
    }

    // Contains all currently active queues, keyed by a String id.
    public static Map<String, ScriptQueue> _queues = new ConcurrentHashMap<String, ScriptQueue>(8, 0.9f, 1);


    /**
     * Returns a collection of all active queues.
     *
     * @return a collection of ScriptQueues
     */
    public static Collection<ScriptQueue> _getQueues() {
        return _queues.values();
    }


    /**
     * <p>Gets a ScriptQueue instance. If a queue already exists with the
     * given id, it will return that instance as opposed to creating a
     * new one. IDs are case insensitive.  If having an easy-to-recall
     * ID is not necessary, use the static method _getNextId() which
     * will return a random UUID.</p>
     *
     * <p>New ScriptQueues will need further information before they
     * can start(), including </p>
     * @param id  unique id of the queue
     * @return
     */
    public static ScriptQueue _getQueue(String id) {
        // Get id if not specified.
        if (id == null) id = String.valueOf(_getNextId());
        ScriptQueue scriptQueue;
        // Does the queue already exist?
        if (_queueExists(id))
            scriptQueue = _queues.get(id.toUpperCase());
            // If not, create a new one.
        else {
            scriptQueue = new ScriptQueue(id,
                    Duration.valueOf(Settings.ScriptQueueSpeed()).getTicksAsInt());
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


    // Name of the queue -- this identifies the ScriptQueue when using _getQueue
    // or a dScript Queue:xxx argument, whereas xxx = id
    public String id;

    // Keep track of Bukit's Scheduler taskId for the engine, for when it times out.
    protected int taskId;

    // The speed of the engine, the # of ticks between each revolution.
    protected int ticks;
    
    // How many times the queue should loop
    protected int loopQty = 1;

    // List of ScriptEntries in the queue
    List<ScriptEntry> scriptEntries = new ArrayList<ScriptEntry>();

    // If this number is larger than getCurrentTimeMillis, the queues will delay execution
    protected long delayTime = 0;

    // The delay in ticks
    protected long delayTicks = 0;

    // ScriptQueues can have a bit of context, keyed by a String Id. All that can be accessed by either getContext()
    // or a dScript replaceable tag <context.id>
    private ConcurrentHashMap<String, String> context = new ConcurrentHashMap<String, String>(8, 0.9f, 1);

    public String getContext(String key) {
        return context.get(key.toLowerCase());
    }

    public boolean hasContext(String key) {
        return context.containsKey(key.toLowerCase());
    }

    public void addContext(String key, String value) {
        context.put(key.toLowerCase(), value);
    }

    public ConcurrentHashMap<String, String> getAllContext() {
        return context;
    }

    protected ScriptEntry lastEntryExecuted = null;


    public void setLastEntryExecuted(ScriptEntry entry) {
        lastEntryExecuted = entry;
    }

    public Duration getSpeed() {
        return Duration.valueOf(ticks + "t");
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
    
    public void setLoop(int loopQty) {
    	this.loopQty = loopQty;
    }


    public void delayFor(long delayTicks) {
        this.delayTicks = delayTicks;
    }


    public void delayUntil(long delayTime) {
        this.delayTime = delayTime;
    }

    boolean is_stopping = false;

    public void stop() {
        loopTimes++;
        if (!is_stopping) {
            is_stopping = true;
            List<ScriptEntry> entries = lastEntryExecuted.getScript()
                    .getContainer().getEntries(lastEntryExecuted.getPlayer(), lastEntryExecuted.getNPC(), "on queue completes");
            if (!entries.isEmpty()) {
                scriptEntries.addAll(entries);
                dB.log("Finishing up queue " + id + "...");
            }
            else {
                _queues.remove(id);
                dB.echoDebug("Completing queue " + id + "...");
                Bukkit.getServer().getScheduler().cancelTask(taskId);
            }
        } else {
            _queues.remove(id);
            dB.echoDebug("Completing queue " + id + "...");
            Bukkit.getServer().getScheduler().cancelTask(taskId);
            isStarted = false;
        }
    }


    private boolean isStarted = false;
    private int loopTimes = 0;

    List<ScriptEntry> scriptEntriesClone = new ArrayList<ScriptEntry>();

    public void start() {
        // If already started, no need to restart.
        if (isStarted)
        	return;
        if (loopQty > 1) {
            if (loopTimes == 0)
                scriptEntriesClone.addAll(scriptEntries);
            else {
                scriptEntries.clear();
                scriptEntries.addAll(scriptEntriesClone);
            }
        }
        // Start the queue
        dB.echoDebug("Starting queue " + id + ". (Speed=" + ticks + "tpr)");
        isStarted = true;

        // If not an instant queue, set a bukkit repeating task with the speed
        if (ticks > 0)
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    // Turn the engine
                    revolve();
                }
            }, delayTicks, ticks);
            // If the ticks are 0, this is an 'instant queue'
        else
        {
            // If it's delayed, schedule it for later
            if (delayTime > System.currentTimeMillis())
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                        new Runnable() {
                            @Override
                            public void run() {
                                // revolve
                                while (isStarted) revolve();
                            }
                        }, delayTicks + 1);
            }
            else while (isStarted) revolve();
        }
    }


    private void revolve() {
        // If entries queued up are empty, deconstruct the queue.
        if (scriptEntries.isEmpty()) {
            stop();
            is_stopping = false;
            isStarted = false;
            if (loopTimes < loopQty)
            	Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
        			public void run() {
        				start();
        			}
        		}, 10);
            return;
        }
        // Check if this Queue isn't paused
        if (paused) return;
        // If it's delayed, schedule it for later
        if (delayTime > System.currentTimeMillis()) return;

        // Criteria met for a successful 'revolution' of this queue...
        DenizenAPI.getCurrentInstance().getScriptEngine().revolve(this);
        if (scriptEntries.isEmpty()) {
            stop();
            is_stopping = false;
            isStarted = false;
            if (loopTimes < loopQty)
            	Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
        			public void run() {
        				start();
        			}
        		}, delayTicks + 1);
        }
    }


    public ScriptEntry getNext() {
        if (!scriptEntries.isEmpty()) {
            ScriptEntry entry = scriptEntries.get(0);
            scriptEntries.remove(0);
            return entry;
        }
        else return null;
    }


    public ScriptQueue addEntries(List<ScriptEntry> entries) {
        scriptEntries.addAll(entries);
        return this;
    }

    protected boolean hasInjectedItems = false;

    public ScriptQueue injectEntries(List<ScriptEntry> entries, int position) {
        if (position > scriptEntries.size() || position < 0) position = 1;
        if (scriptEntries.size() == 0) position = 0;
        scriptEntries.addAll(position, entries);
        hasInjectedItems = true;
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
        hasInjectedItems = true;
        return this;
    }


    public int getQueueSize() {
        return scriptEntries.size();
    }

}
