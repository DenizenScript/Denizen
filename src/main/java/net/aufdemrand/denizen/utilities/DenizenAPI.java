package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;

/**
 * @author Jeremy Schroeder
 *
 */
public class DenizenAPI {

	public static DenizenNPC getDenizenNPC(NPC npc) {
		return ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen")).getNPCRegistry().getDenizen(npc);
	}
	
}
