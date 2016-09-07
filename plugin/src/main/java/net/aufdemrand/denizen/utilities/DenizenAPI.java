package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.objects.dNPC;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;

/**
 * Provides some static methods for working with Denizen and Denizen-enabled NPCs
 *
 * @author aufdemrand
 */
public class DenizenAPI {

    public static Denizen denizen;

    /**
     * Returns a dNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     * @return a dNPC
     */
    public static dNPC getDenizenNPC(NPC npc) {
        return dNPCRegistry.getDenizen(npc);
    }

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     *
     * @return map of NPC, dNPC of all spawned NPCs
     */
    public static Collection<dNPC> getSpawnedNPCs() {
        return dNPCRegistry.getSpawnedNPCs();
    }

    /**
     * Gets the current instance of the Denizen plugin.
     *
     * @return Denizen instance
     */
    public static Denizen getCurrentInstance() {
        if (denizen == null) {
            denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
        }
        return denizen;
    }

    public static FileConfiguration _saves() {
        return getCurrentInstance().getSaves();
    }
}
