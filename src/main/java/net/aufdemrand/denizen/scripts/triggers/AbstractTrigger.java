package net.aufdemrand.denizen.scripts.triggers;

import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;
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
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     */
    @Override
    public void onDisable() {
        // Nothing to do here on this level of abstraction.
    }


    /**
     * Part of the Plugin enable sequence.
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onEnable() to Denizen. (ie. Server shuts down or restarts)
     */
    @Override
    public void onEnable() {
        // Nothing to do here on this level of abstraction.
    }


    /**
     * Part of the Plugin enable sequence.
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when being enabled via
     * the Trigger Registry, usually upon startup.
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

        List<ScriptEntry> entries = script.getEntriesFor(this.getClass(), player, npc, id, true);
        if (entries.isEmpty()) return false;

        dB.echoDebug(script, DebugElement.Header, "Parsing " + name + " trigger: n@" + npc.getName() + "/p@" + player.getName());
        // Create Queue
        TimedQueue queue = TimedQueue.getQueue(ScriptQueue.getNextId(script.getName()));
        // Add all entries to set it up
        queue.addEntries(entries);
        // Add context
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        // Start it
        queue.start();

        return true;
    }

    /**
     * This method will find all NPCs within a certain range of a location that
     * have a trigger, and the trigger is enabled.
     *
     * @param location the location to search around
     * @param maxRange how far to search
     * @return The Set of NPCs that are
     */
    // TODO: Delete?
    public Set<NPC> getActiveNPCsWithinRangeWithTrigger(Location location, int maxRange) {
        Set<NPC> closestNPCs = new HashSet<NPC>();

        Iterator<NPC> it = CitizensAPI.getNPCRegistry().iterator();
        while (it.hasNext()) {
            NPC npc = it.next();
            if (npc.isSpawned()
                    && npc.getEntity().getLocation().getWorld().equals(location.getWorld())
                    && npc.getEntity().getLocation().distance(location) < maxRange
                    && npc.hasTrait(TriggerTrait.class)
                    && npc.getTrait(TriggerTrait.class).isEnabled(name)) {
                closestNPCs.add(npc);
            }
        }

        return closestNPCs;
    }
}
