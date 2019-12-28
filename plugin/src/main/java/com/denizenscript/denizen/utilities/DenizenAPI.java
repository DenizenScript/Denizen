package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.npc.DenizenNPCHelper;
import com.denizenscript.denizen.objects.NPCTag;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Provides some static methods for working with Denizen and Denizen-enabled NPCs
 */
public class DenizenAPI {

    private static Denizen denizen;

    /**
     * Returns a NPCTag object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     * @return a NPCTag
     */
    public static NPCTag getDenizenNPC(NPC npc) {
        if (npc == null) {
            return null;
        }
        return DenizenNPCHelper.getDenizen(npc);
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

    public static FileConfiguration getSaves() {
        return getCurrentInstance().getSaves();
    }
}
