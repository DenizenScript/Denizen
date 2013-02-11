package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TaskScriptContainer extends ScriptContainer {

    public TaskScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public Duration getSpeed() {
        Duration speed = Duration.valueOf(getString("SPEED"));
        if (speed == null) speed = new Duration(Double.valueOf(Settings.InteractDelayInTicks() / 20));
        return speed;
    }

    public TaskScriptContainer setInstant() {
        set("SPEED", "INSTANT");
        return this;
    }

    public TaskScriptContainer setSpeed(Duration speed) {
        set("SPEED", speed.dScriptArgValue());
        return this;
    }

    public boolean isInstant() {
        if (getSpeed().getSeconds() <= 0
                || getString("SPEED").equalsIgnoreCase("INSTANT"))
            return true;
        else return false;
    }

    public ScriptQueue runTaskScript(Player player, dNPC npc, Map<String, String> context) {
        return runTaskScript(ScriptQueue._getNextId(), player, npc, context);
    }

    public ScriptQueue runTaskScript(String queueId, Player player, dNPC npc, Map<String, String> context) {
        ScriptQueue queue = ScriptQueue._getQueue(queueId);
        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
        for (Map.Entry<String, String> entry : context.entrySet()) {
            // TODO: Add context back
        }
        queue.addEntries(listOfEntries);
        if (isInstant()) queue.setSpeed(0);
        else
            queue.setSpeed(getSpeed().getTicksAsInt());
        queue.start();
        return queue;
    }

    public ScriptQueue runTaskScriptWithDelay(String queueId, Player player, dNPC npc, Map<String, String> context, Duration delay) {
        ScriptQueue queue = ScriptQueue._getQueue(queueId);
        List<ScriptEntry> listOfEntries = getBaseEntries(player, npc);
        if (context != null)
        for (Map.Entry<String, String> entry : context.entrySet()) {
            // TODO: Add context back
        }
        queue.addEntries(listOfEntries);
        if (isInstant()) queue.setSpeed(0);
        else
            queue.setSpeed(getSpeed().getTicksAsInt());
        queue.delayUntil(System.currentTimeMillis() + (long) (delay.getSeconds() * 1000));
        queue.start();
        return queue;
    }


}
