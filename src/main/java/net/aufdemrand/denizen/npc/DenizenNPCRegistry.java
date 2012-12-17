package net.aufdemrand.denizen.npc;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.utilities.debugging.dB;

import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPCRegistry implements Listener {

    private Map<NPC, DenizenNPC> denizenNPCs = new ConcurrentHashMap<NPC, DenizenNPC>();

    private Denizen plugin;
    private ActionHandler actionHandler;
    
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    public DenizenNPCRegistry(Denizen denizen) {
        plugin = denizen;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        actionHandler = new ActionHandler(plugin);
    }

    public void registerNPC(NPC npc) {
        if (!denizenNPCs.containsKey(npc)) {
            denizenNPCs.put(npc, new DenizenNPC(npc));
        }
        dB.log("Constructing Denizen NPC " + getDenizen(npc).toString() + 
                "  List size now: " + denizenNPCs.size());
    }

    public DenizenNPC getDenizen(NPC npc) {
        if (!denizenNPCs.containsKey(npc))
            registerNPC(npc);
        return denizenNPCs.get(npc);
    }

    public boolean isDenizenNPC (NPC npc) {
        if (denizenNPCs.containsKey(npc)) 
            return true ;
        else return false; 
    }

    public Map<NPC, DenizenNPC> getDenizens() {
        Iterator<Entry<NPC, DenizenNPC>> it = denizenNPCs.entrySet().iterator();
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

    @EventHandler 
    public void onSpawn(NPCSpawnEvent event) {
        registerNPC(event.getNPC());
        // On Spawn action
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("spawn", null);
    }

    @EventHandler 
    public void onRemove(NPCRemoveEvent event) {
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("remove", null);
        if (isDenizenNPC(event.getNPC()))
            denizenNPCs.remove(event.getNPC());
        dB.log(ChatColor.RED + "Deconstructing Denizen NPC " + event.getNPC().getName() + "/" + event.getNPC().getId() + 
            "  List size now: " + denizenNPCs.size());
    }


}
