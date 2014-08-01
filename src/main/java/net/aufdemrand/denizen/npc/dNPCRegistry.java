package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for keeping track and retrieving dNPC objects which offer some Denizen-specific
 * methods when dealing with NPCs.
 *
 * @author Jeremy Schroeder
 *
 */
public class dNPCRegistry implements Listener {

    public static Map<Integer, dNPC> denizenNPCs = new ConcurrentHashMap<Integer, dNPC>(8, 0.9f, 1);

    public static dNPCRegistry getCurrentInstance() {
        return DenizenAPI.getCurrentInstance().getNPCRegistry();
    }

    private Denizen plugin;
    private ActionHandler actionHandler;

    public dNPCRegistry(Denizen denizen) {
        plugin = denizen;
        if (Depends.citizens != null)
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

    private static boolean _isRegistered (NPC npc) {
        return denizenNPCs.containsKey(npc.getId());
    }

    private static void _registerNPC(NPC npc) {
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
    public static dNPC getDenizen(NPC npc) {
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
    public static Set<dNPC> getSpawnedNPCs() {
        Iterator<Map.Entry<Integer, dNPC>> it = denizenNPCs.entrySet().iterator();
        Set<dNPC> npcs = new HashSet<dNPC>();
        while (it.hasNext()) {
            Map.Entry<Integer, dNPC> npc = it.next();
            if (npc.getValue().getCitizen() == null) {
                dB.log(ChatColor.RED + "Removed NPC from dRegistry.");
                try { denizenNPCs.remove(npc.getKey()); }
                catch (Exception e) { dB.echoError("Report this error to aufdemrand! Err: posconcurrency1"); }
            }
            else if (npc.getValue().isSpawned())
                npcs.add(npc.getValue());
        }
        return npcs;
    }

    // <--[action]
    // @Actions
    // spawn
    // @Triggers when the NPC is spawned.
    // This will fire whenever an NPC's chunk is loaded, or a spawn command is issued.
    //
    // @Context
    // None
    // -->
    /**
     * Fires the 'On Spawn:' action in an NPCs Assignment, if set.
     *
     * @param event NPCSpawnEvent
     *
     */
    @EventHandler
    public void onSpawn(NPCSpawnEvent event) {
        _registerNPC(event.getNPC());
        // Do world script event 'On NPC spawns'
        EventManager.doEvents(Arrays.asList
                ("npc spawns"),
                dNPC.mirrorCitizensNPC(event.getNPC()), null, null);
        // On Spawn action
        getDenizen(event.getNPC()).action("spawn", null);
    }


    // <--[action]
    // @Actions
    // despawn
    // @Triggers when the NPC is despawned.
    // This can be because a command was issued, or a chunk has been unloaded.
    //
    // @Context
    // None
    // -->

    /**
     * Fires a world script event and then NPC action when the NPC despawns.
     *
     * @param event NPCDespawnEvent
     */
    @EventHandler
    public void despawn(NPCDespawnEvent event) {
        // Do world script event 'On NPC Despawns'
        EventManager.doEvents(Arrays.asList
                ("npc despawns"),
                dNPC.mirrorCitizensNPC(event.getNPC()), null, null);

        getDenizen(event.getNPC()).action("despawn", null);
    }


    // <--[action]
    // @Actions
    // remove
    // @Triggers when the NPC is removed.
    //
    // @Context
    // None
    // -->

    /**
     * Removes an NPC from the Registry when removed from Citizens.
     *
     * @param event NPCRemoveEvent
     */
    @EventHandler
    public void onRemove(NPCRemoveEvent event) {
        getDenizen(event.getNPC()).action("remove", null);
        if (_isRegistered(event.getNPC()))
            denizenNPCs.remove(event.getNPC().getId());
        dB.log(ChatColor.RED + "Deconstructing Denizen NPC " + event.getNPC().getName() + "/" + event.getNPC().getId());
        // TODO: Delete flags / etc.
    }
}
