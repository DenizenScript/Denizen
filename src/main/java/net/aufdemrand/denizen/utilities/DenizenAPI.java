package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * Provides some static methods for working with Denizen-enabled NPCs
 *
 * @author Jeremy Schroeder
 *
 */
public class DenizenAPI {

    /**
     * Returns a DenizenNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     *
     * @return a DenizenNPC
     *
     */
	public static DenizenNPC getDenizenNPC(NPC npc) {
		return ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen")).getNPCRegistry().getDenizen(npc);
	}

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     *
     * @return map of NPC, DenizenNPC of all spawned NPCs
     *
     */
    public static Map<NPC, DenizenNPC> getSpawnedNPCs() {
        return ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen")).getNPCRegistry().getSpawnedNPCs();
    }

}
