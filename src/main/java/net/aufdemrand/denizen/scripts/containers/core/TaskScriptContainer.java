package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.Duration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskScriptContainer extends ScriptContainer {

    public TaskScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public Duration getSpeed() {
    	Duration speed = null;
    	
    	if (getString("SPEED", null) != null)
    	{
    		if (getString("SPEED", null).toUpperCase() == "INSTANT")
    			speed = Duration.valueOf("0");
    		else
    			speed = Duration.valueOf(getString("SPEED", null));
    	}
    	else
    		speed = new Duration(((double) Settings.InteractDelayInTicks() / 20));
        
        return speed;
    }

    public TaskScriptContainer setInstant() {
        set("SPEED", "INSTANT");
        return this;
    }

    public TaskScriptContainer setSpeed(Duration speed) {
        set("SPEED", speed.getSeconds());
        return this;
    }

    public ScriptQueue runTaskScript(Player player, dNPC npc, Map<String, String> context) {
        return runTaskScript(ScriptQueue._getNextId(), player, npc, context);
    }

    public ScriptQueue runTaskScript(String queueId, Player player, dNPC npc, Map<String, String> context) {
        ScriptQueue queue = ScriptQueue._getQueue(queueId);
        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
            ScriptBuilder.addObjectToEntries(listOfEntries, "context", context);
        queue.addEntries(listOfEntries);
        queue.setSpeed(getSpeed().getTicksAsInt());
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

    public ScriptQueue runTaskScriptWithDelay(String queueId, Player player, dNPC npc, Map<String, String> context, Duration delay) {
        ScriptQueue queue = ScriptQueue._getQueue(queueId);
        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
            ScriptBuilder.addObjectToEntries(listOfEntries, "context", context);
        queue.addEntries(listOfEntries);
        queue.setSpeed(getSpeed().getTicksAsInt());
        queue.delayUntil(System.currentTimeMillis() + (long) (delay.getSeconds() * 1000));
        queue.start();
        return queue;
    }


}
