package net.aufdemrand.denizen.scripts.triggers;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        return parse(npc, player, script, null);
    }


    public boolean parse(dNPC npc, dPlayer player, InteractScriptContainer script, String id) {
        if (npc == null || player == null || script == null) return false;

        List<ScriptEntry> entries = script.getEntriesFor(this.getClass(), player, npc, id);
        if (entries.isEmpty()) return false;

        dB.echoDebug(DebugElement.Header, "Parsing " + name + " trigger: " + npc.getName() + "/" + player.getName());
        ScriptQueue._getQueue(ScriptQueue._getNextId()).addEntries(entries).start();

        return true;
    }

	/**
	 * This method will find all NPCs within a certain range of a location that
	 * have a trigger, and the trigger is enabled.
	 * 
	 * @param location
	 * @param maxRange
	 * 
	 * @return	The Set of NPCs that are 
	 */
	public Set<NPC> getActiveNPCsWithinRangeWithTrigger (Location location, int maxRange) {
		Set<NPC> closestNPCs = new HashSet<NPC> ();

		Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
		while (it.hasNext ()) {
			NPC	npc = it.next ();
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
