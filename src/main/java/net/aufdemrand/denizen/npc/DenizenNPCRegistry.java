package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for keeping track and retrieving DenizenNPC objects which offer some Denizen-specific
 * methods when dealing with NPCs.
 *
 * @author Jeremy Schroeder
 *
 */
public class DenizenNPCRegistry implements Listener {

    private static Map<NPC, DenizenNPC> denizenNPCs = new ConcurrentHashMap<NPC, DenizenNPC>();

    private Denizen plugin;
    private ActionHandler actionHandler;

    public DenizenNPCRegistry(Denizen denizen) {
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

    private boolean isDenizenNPC (NPC npc) {
        if (denizenNPCs.containsKey(npc))
            return true ;
        else return false;
    }

    private void registerNPC(NPC npc) {
        if (npc == null) return;
        if (!denizenNPCs.containsKey(npc)) {
            denizenNPCs.put(npc, new DenizenNPC(npc));
        }
        dB.log("Constructing NPC " + getDenizen(npc).toString());
    }

    /**
     * Returns a DenizenNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     *
     * @return a DenizenNPC
     *
     */
    public DenizenNPC getDenizen(NPC npc) {
        if (npc == null) return null;
        if (!denizenNPCs.containsKey(npc))
            registerNPC(npc);
        return denizenNPCs.get(npc);
    }

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     *
     * @return map of NPC, DenizenNPC of all spawned NPCs
     *
     */
    public Map<NPC, DenizenNPC> getSpawnedNPCs() {
        Iterator<Map.Entry<NPC, DenizenNPC>> it = denizenNPCs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<NPC, DenizenNPC> npc = (Map.Entry<NPC, DenizenNPC>)it.next();
            try {
                npc.getKey().getBukkitEntity();
            } catch (NullPointerException e) {
                denizenNPCs.remove(npc.getKey());
                dB.log(ChatColor.RED + "Removed NPC from DenizenRegistry. " + ChatColor.WHITE + "The bukkit entity has been removed.");
            }
        }
        return denizenNPCs;
    }

    /**
     * Fires the 'On Spawn:' action in an NPCs Assignment, if set.
     *
     * @param event NPCSpawnEvent
     *
     */
    @EventHandler 
    public void onSpawn(NPCSpawnEvent event) {
        registerNPC(event.getNPC());
        // On Spawn action
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("spawn", null);
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
        if (isDenizenNPC(event.getNPC()))
            denizenNPCs.remove(event.getNPC());
        dB.log(ChatColor.RED + "Deconstructing Denizen NPC " + event.getNPC().getName() + "/" + event.getNPC().getId());
    }

}
