package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for keeping track and retrieving dNPC objects which offer some Denizen-specific
 * methods when dealing with NPCs.
 *
 * @author Jeremy Schroeder
 *
 */
public class dNPCRegistry implements Listener {

    private static Map<Integer, dNPC> denizenNPCs = new ConcurrentHashMap<Integer, dNPC>(8, 0.9f, 1);

    public static dNPCRegistry getCurrentInstance() {
        return DenizenAPI.getCurrentInstance().getNPCRegistry();
    }

    private Denizen plugin;
    private ActionHandler actionHandler;

    public dNPCRegistry(Denizen denizen) {
        plugin = denizen;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        actionHandler = new ActionHandler(plugin);
    }

    /**
     * Gets the currently loaded instance of the ActionHandler
     *
     * @return ActionHandler
     *
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    private boolean _isRegistered (NPC npc) {
        if (denizenNPCs.containsKey(npc))
            return true;
        else return false;
    }

    private void _registerNPC(NPC npc) {
        if (npc == null) return;
        if (!denizenNPCs.containsKey(npc.getId())) {
            denizenNPCs.put(npc.getId(), new dNPC(npc));
        }
        // dB.log("Constructing NPC " + getDenizen(npc).toString());
    }

    /**
     * Returns a dNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     *
     * @return a dNPC
     *
     */
    public dNPC getDenizen(NPC npc) {
        if (npc == null) return null;
        if (!denizenNPCs.containsKey(npc.getId()))
            _registerNPC(npc);
        return denizenNPCs.get(npc.getId());
    }

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     *
     * @return map of NPC, dNPC of all spawned NPCs
     *
     */
    public Set<dNPC> getSpawnedNPCs() {
        Iterator<Map.Entry<Integer, dNPC>> it = denizenNPCs.entrySet().iterator();
        Set<dNPC> npcs = new HashSet<dNPC>();
        while (it.hasNext()) {
            Map.Entry<Integer, dNPC> npc = it.next();
            if (npc.getValue().getCitizen() == null) {
                dB.log(ChatColor.RED + "Removed NPC from DenizenRegistry.");
                try { denizenNPCs.remove(npc.getKey()); } catch (Exception e) { dB.echoDebug("Report this error to aufdemrand! Err: posconcurrency1"); }
            }
            else if (npc.getValue().isSpawned())
                npcs.add(npc.getValue());
        }
        return npcs;
    }

    /**
     * Fires the 'On Spawn:' action in an NPCs Assignment, if set.
     *
     * @param event NPCSpawnEvent
     *
     */
    @EventHandler
    public void onSpawn(NPCSpawnEvent event) {
        _registerNPC(event.getNPC());
        // On Spawn action
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("spawn", null);
    }

    @EventHandler
    public void despawn(NPCDespawnEvent event) {
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("despawn", null);
    }

    /**
     * Removes an NPC from the Registry when removed from Citizens.
     *
     * @param event NPCRemoveEvent
     *
     */
    @EventHandler
    public void onRemove(NPCRemoveEvent event) {
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("remove", null);
        if (_isRegistered(event.getNPC()))
            denizenNPCs.remove(event.getNPC());
        dB.log(ChatColor.RED + "Deconstructing Denizen NPC " + event.getNPC().getName() + "/" + event.getNPC().getId());
    }


}
