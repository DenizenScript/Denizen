package net.aufdemrand.denizen.scripts.triggers;

import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

import java.util.*;

public abstract class AbstractTrigger implements RegistrationableInstance {

    protected String name;

    @Override
    public AbstractTrigger as(String triggerName) {
        this.name = triggerName.toUpperCase();
        // Register command with Registry
        DenizenAPI.getCurrentInstance().getTriggerRegistry().register(triggerName, this);
        onEnable();
        return this;
    }


    @Override
    public String getName() {
        // Return the name of the trigger specified upon registration.
        return name;
    }


    /**
     * Part of the Plugin disable sequence.
     * 
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     * 
     */
    @Override
    public void onDisable() {
        // Nothing to do here on this level of abstraction.
    }


    /**
     * Part of the Plugin enable sequence.
     *
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onEnable() to Denizen. (ie. Server shuts down or restarts)
     *
     */
    @Override
    public void onEnable() {
        // Nothing to do here on this level of abstraction.
    }


    /**
     * Part of the Plugin enable sequence.
     *
     * Can be '@Override'n by a Trigger which requires a method when being enabled via
     * the Trigger Registry, usually upon startup.
     *
     */
    @Override
    public AbstractTrigger activate() {
        // Nothing to do here on this level of abstraction.
        return this;
    }


    public boolean parse(dNPC npc, dPlayer player, InteractScriptContainer script) {
        return parse(npc, player, script, null, null);
    }


    public boolean parse(dNPC npc, dPlayer player, InteractScriptContainer script, String id) {
        return parse(npc, player, script, id, null);
    }

    public boolean parse(dNPC npc, dPlayer player, InteractScriptContainer script, String id, Map<String, dObject> context) {
        if (npc == null || player == null || script == null) return false;

        List<ScriptEntry> entries = script.getEntriesFor(this.getClass(), player, npc, id);
        if (entries.isEmpty()) return false;

        dB.echoDebug(DebugElement.Header, "Parsing " + name + " trigger: " + npc.getName() + "/" + player.getName());
        // Create Queue
        TimedQueue queue = TimedQueue.getQueue(ScriptQueue._getNextId());
        // Add all entries to set it up
        queue.addEntries(entries);
        // Add context
        if (context != null) {
            for (Map.Entry<String, dObject> entry : context.entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
        }
        // Start it
        queue.start();

        return true;
    }

    /**
     * This method will find all NPCs within a certain range of a location that
     * have a trigger, and the trigger is enabled.
     * 
     * @param location
     * @param maxRange
     * 
     * @return    The Set of NPCs that are 
     */
    public Set<NPC> getActiveNPCsWithinRangeWithTrigger (Location location, int maxRange) {
        Set<NPC> closestNPCs = new HashSet<NPC> ();

        Iterator<NPC>    it = CitizensAPI.getNPCRegistry().iterator();
        while (it.hasNext ()) {
            NPC    npc = it.next ();
            if (npc.isSpawned()
                    && npc.getBukkitEntity().getLocation().getWorld().equals(location.getWorld())
                    && npc.getBukkitEntity().getLocation().distance(location) < maxRange
                    && npc.hasTrait(TriggerTrait.class)
                    && npc.getTrait(TriggerTrait.class).isEnabled(name)) {
                closestNPCs.add (npc);
            }
        }
        
        return closestNPCs;        
    }
}
