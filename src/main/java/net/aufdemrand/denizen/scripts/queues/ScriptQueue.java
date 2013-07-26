package net.aufdemrand.denizen.scripts.queues;

import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScriptQueues hold/control ScriptEntries while being sent
 * to the CommandExecuter
 *
 * @version 1.0
 * @author Jeremy Schroeder, David Cernat
 */

public abstract class ScriptQueue {


    protected static long total_queues = 0;


    /**
     * Returns the number of queues created in the current instance
     * as well as the number of currently active queues.
     *
     * @return stats
     */
    public static String _getStats() {
        return "Total number of queues created: "
                + total_queues
                + ", currently active queues: "
                + _queues.size() +  ".";
    }


    /**
     * Gets an existing queue. Cast to the correct QueueType to
     * access further methods.
     *
     * @param id  the id of the queue
     * @return    a ScriptQueue instance, or null
     */
    public static ScriptQueue _getExistingQueue(String id) {
        if (!_queueExists(id)) return null;
        else return _queues.get(id.toUpperCase());
    }


    /**
     * Gets a random UUID for use in creating a 'nameless' queue.
     *
     * @return String value of a random UUID
     */
    public static String _getNextId() {
        return UUID.randomUUID().toString();
    }


    /**
     * Checks the type of an existing queue with the type given.
     *
     * @param queue  id of the queue
     * @param type   class of the queue type
     * @return       true if they match, false if the queue
     *               doesn't exist or does not match
     */
    public static boolean _matchesType(String queue, Class type) {
        if (_queueExists(queue.toUpperCase()))
            return _queues.get(queue.toUpperCase()).getClass() == type;
        else return false;
    }


    // Contains all currently active queues, keyed by a String id.
    protected static Map<String, ScriptQueue> _queues =
            new ConcurrentHashMap<String, ScriptQueue>(8, 0.9f, 1);


    /**
     * Returns a collection of all active queues.
     *
     * @return a collection of ScriptQueues
     */
    public static Collection<ScriptQueue> _getQueues() {
        return _queues.values();
    }


    /**
     * Checks if a queue exists with the given id.
     *
     * @param id  the String ID of the queue to check.
     * @return  true if it exists.
     */
    public static boolean _queueExists(String id) {
        return _queues.containsKey(id.toUpperCase());
    }



    /////////////////////
    // Public instance fields
    /////////////////////


    // Name of the queue -- this identifies
    // the ScriptQueue when using _getQueue()
    public String id;



    /////////////////////
    // Private instance fields and constructors
    /////////////////////


    // List of ScriptEntries in the queue
    private List<ScriptEntry>
            script_entries = new ArrayList<ScriptEntry>();


    // The last script entry that was executed
    // in this queue.
    private ScriptEntry lastEntryExecuted = null;


    // If this number is larger than Java's
    // getCurrentTimeMillis(), the queue will
    // delay execution of the next ScriptEntry
    private long delay_time = 0;


    // ScriptQueues can have a bit of context,
    // keyed by a String Id. Denizen's
    // 'Definitions' system uses this context.
    private Map<String, String>
            context = new ConcurrentHashMap<String, String>(8, 0.9f, 1);


    /**
     * Creates a ScriptQueue instance. Users of
     * the API should instead use the static members
     * of classes that extend ScriptQueue.
     *
     * @param id  the name of the ScriptQueue
     */
    protected ScriptQueue(String id) {
        // Remember the 'id'
        this.id = id.toUpperCase();
        // Save the instance to the _queues static map
        _queues.put(id.toUpperCase(), this);
        // Increment the stats
        total_queues++;
    }



    /////////////////////
    // Public instance setters and getters
    /////////////////////


    /**
     * Gets a piece of context from the queue. Denizen's
     * CommandExecuter will fetch this information
     * by using the %definition_name% format, similar
     * to 'replaceable tags'
     *
     * @param definition  The name of the context
     * @return  The value of the context, or null
     */
    public String getContext(String definition) {
        return context.get(definition.toLowerCase());
    }


    /**
     * Checks for a piece of context.
     *
     * @param definition  The name of the context
     * @return  true if the definition exists.
     */
    public boolean hasContext(String definition) {
        return context.containsKey(definition.toLowerCase());
    }


    /**
     * Adds a new piece of context to the queue. This
     * can be done with dScript as well by using the
     * 'define' command.
     *
     * @param definition  the name of the context
     * @param value  the value of the definition
     */
    public void addContext(String definition, String value) {
        context.put(definition.toLowerCase(), value);
    }


    /**
     * Returns a Map of all the current context
     * stored in the queue, keyed by 'definition id'
     *
     * @return  all current context, empty if none.
     */
    public Map<String, String> getAllContext() {
        return context;
    }


    /**
     * The last entry that was executed. Note: any
     * replaceable tags/etc. are already replaced
     * in this ScriptEntry.
     *
     * @return
     */
    public ScriptEntry getLastEntryExecuted() {
        return lastEntryExecuted;
    }


    /**
     * Clears the script queue.
     *
     * Use the 'queue clear' command in dScript to
     * access this method.
     */
    public void clear() {
        script_entries.clear();
    }


    /**
     * Will delay the start of the queue until Java's
     * System.currentTimeMillis() is less than the
     * delayTime.
     *
     * @param delayTime  the time to start the queue, in
     *                   System.currentTimeMilis() format.
     */
    public void delayUntil(long delayTime) {
        this.delay_time = delayTime;
    }



    ///////////////////
    // Public 'functional' methods
    //////////////////


    /**
     * Starts the script queue.
     *
     */
    protected abstract void onStart();


    protected boolean is_started;


    public void start() {
        if (is_started) return;

        // Set as started, and check for a valid delay_time.
        is_started = true;
        boolean is_delayed = false;
        if (delay_time > System.currentTimeMillis())
            is_delayed = true;

        dB.echoDebug("Starting " + getClass().getSimpleName() + " '" + id + "'");
        // If it's delayed, schedule it for later

        if (is_delayed) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        @Override
                        public void run() { onStart(); /* Start the engine */ }

                        // Take the delay time, find out how many milliseconds away
                        // it is, turn it into seconds, then divide by 20 for ticks.
                    },
                    (delay_time - System.currentTimeMillis()) / 1000 * 20);

        } else
            // If it's not, start the engine now!
            onStart();

        if (is_delayed) {
            dB.echoDebug("...but delaying execution for '"
                    + new Duration((delay_time - System.currentTimeMillis()) / 1000 * 20).identify() + "'.");
        }

    }


    /**
     * Stops the script_queue and breaks it down.
     *
     */
    protected abstract void onStop();


    protected boolean is_stopping = false;


    public void stop() {

        // If this is the first time this has been called, check the
        // ScriptContainer event 'on queue completes' which may have
        // a few more script entries to run.
        if (!is_stopping) {
            is_stopping = true;

            // Get the entries
            List<ScriptEntry> entries =
                    (lastEntryExecuted != null && lastEntryExecuted.getScript() != null ?
                            lastEntryExecuted.getScript().getContainer()
                            .getEntries(lastEntryExecuted.getPlayer(),
                                    lastEntryExecuted.getNPC(), "on queue completes") : new ArrayList<ScriptEntry>());
            // Add the 'finishing' entries back into the queue (if not empty)
            if (!entries.isEmpty()) {
                script_entries.addAll(entries);
                dB.log("Finishing up queue " + id + "...");
            } else /* if empty, just stop the queue like normal */ {
                _queues.remove(id);
                dB.echoDebug("Completing queue " + id + "...");
                is_started = false;
                onStop();
            }
        }

        // Else, just complete the queue.
        // 1) Remove the id from active queue list
        // 2) Cancel the corresponding task_id
        else {
            _queues.remove(id);
            dB.echoDebug("Completing queue " + id + "...");
            is_started = false;
            onStop();
        }
    }



    ////////////////////
    // Internal methods and fields
    ////////////////////


    /**
     * Sets the last entry executed by the ScriptEngine.
     *
     * @param entry  the ScriptEntry last executed.
     */
    public void setLastEntryExecuted(ScriptEntry entry) {
        lastEntryExecuted = entry;
    }


    protected abstract boolean shouldRevolve();


    protected void revolve() {
        // If entries queued up are empty, deconstruct the queue.
        if (script_entries.isEmpty()) {
            stop();
            return;
        }

        if (!shouldRevolve()) return;

        // Criteria met for a successful 'revolution' of this queue,
        // so send the next script entry to the ScriptEngine.
        DenizenAPI.getCurrentInstance().getScriptEngine().revolve(this);

        if (script_entries.isEmpty()) {
            stop();
        }
    }


    public ScriptEntry getNext() {
        if (!script_entries.isEmpty()) {
            ScriptEntry entry = script_entries.get(0);
            script_entries.remove(0);
            return entry;
        }
        else return null;
    }


    public ScriptQueue addEntries(List<ScriptEntry> entries) {
        script_entries.addAll(entries);
        return this;
    }


    public boolean hasInjectedItems = false;


    public ScriptQueue injectEntries(List<ScriptEntry> entries, int position) {
        if (position > script_entries.size() || position < 0) position = 1;
        if (script_entries.size() == 0) position = 0;
        script_entries.addAll(position, entries);
        hasInjectedItems = true;
        return this;
    }


    public boolean removeEntry(int position) {
        if (script_entries.size() < position) return false;
        script_entries.remove(position);
        return true;
    }


    public ScriptEntry getEntry(int position) {
        if (script_entries.size() < position) return null;
        return script_entries.get(position);
    }


    public ScriptQueue injectEntry(ScriptEntry entry, int position) {
        if (position > script_entries.size() || position < 0) position = 1;
        if (script_entries.size() == 0) position = 0;
        script_entries.add(position, entry);
        hasInjectedItems = true;
        return this;
    }


    public int getQueueSize() {
        return script_entries.size();
    }


}
