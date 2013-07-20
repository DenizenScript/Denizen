package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.objects.Duration;

import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskScriptContainer extends ScriptContainer {

    public TaskScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public Duration getSpeed() {
    	Duration speed;
    	if (contains("SPEED")) {
    		if (getString("SPEED", "0").toUpperCase().equals("INSTANT"))
    			speed = Duration.valueOf("0t");
    		else
    			speed = Duration.valueOf(getString("SPEED", "0t"));

    	} else
    		speed = new Duration(Duration.valueOf(Settings.ScriptQueueSpeed()).getSeconds());
        
        return speed;
    }

    public TaskScriptContainer setSpeed(Duration speed) {
        set("SPEED", speed.getSeconds());
        return this;
    }

    public ScriptQueue runTaskScript(dPlayer player, dNPC npc, Map<String, String> context) {
        return runTaskScript(ScriptQueue._getNextId(), player, npc, context);
    }

    public ScriptQueue runTaskScript(String queueId, dPlayer player, dNPC npc, Map<String, String> context) {
        ScriptQueue queue;
        if (getSpeed().getSeconds() == 0)
            queue = InstantQueue.getQueue(queueId);
        else queue = TimedQueue.getQueue(queueId).setSpeed(getSpeed().getTicks());

        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
            ScriptBuilder.addObjectToEntries(listOfEntries, "context", context);
        queue.addEntries(listOfEntries);
        queue.start();
        return queue;
    }

    public Map<String, Integer> getContextMap() {
        if (contains("CONTEXT")) {
            Map<String, Integer> context = new HashMap<String, Integer>();
            int x = 1;
            for (String name : getString("CONTEXT").split("\\|")) {
                context.put(name.toUpperCase(), x);
                x++;
            }
            return context;
        }
        return Collections.emptyMap();
    }

    public ScriptQueue runTaskScriptWithDelay(String queueId, dPlayer player, dNPC npc, Map<String, String> context, Duration delay) {
        ScriptQueue queue;
        if (getSpeed().getSeconds() == 0)
            queue = InstantQueue.getQueue(queueId);
        else queue = TimedQueue.getQueue(queueId).setSpeed(getSpeed().getTicks());

        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
            ScriptBuilder.addObjectToEntries(listOfEntries, "context", context);
        queue.addEntries(listOfEntries);
        queue.delayUntil(System.currentTimeMillis() + (long) (delay.getSeconds() * 1000));
        queue.start();
        return queue;
    }

    public ScriptQueue injectTaskScript(String queueId, dPlayer player, dNPC npc, Map<String, String> context) {
        ScriptQueue queue = ScriptQueue._getExistingQueue(queueId);
        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
            ScriptBuilder.addObjectToEntries(listOfEntries, "context", context);
        queue.injectEntries(listOfEntries, 0);
        queue.start();
        return queue;
    }


}
