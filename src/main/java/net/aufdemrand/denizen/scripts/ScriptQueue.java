package net.aufdemrand.denizen.scripts;

import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ScriptQueue implements Listener {


    private static int totalQueues = 0;

    public static String _getStats() {
        return "Total number of queues created: '"
                + totalQueues
                + "', currently active queues: '"
                + _queues.size() +  "'.";
    }

    public static Map<String, ScriptQueue> _queues = new ConcurrentHashMap<String, ScriptQueue>();

    public static Collection<ScriptQueue> _getQueues() {
        return _queues.values();
    }

    public static ScriptQueue getNewQueue() {
        ScriptQueue newQueue = new ScriptQueue();
        _queues.put(newQueue.id, newQueue);
        return newQueue;

    }

    public static ScriptQueue getNewQueue(String id) {
        ScriptQueue newQueue = new ScriptQueue(id);
        _queues.put(newQueue.id, newQueue);
        return newQueue;    }


    protected String id = "";

    public ScriptQueue(String id) {
    this.id = id;
    }

    public ScriptQueue() {
        this.id = String.valueOf(totalQueues++);
    }



}
