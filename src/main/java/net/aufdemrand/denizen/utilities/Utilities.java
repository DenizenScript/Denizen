package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Utilities {

	private Denizen plugin;

	public Utilities(Denizen plugin) {
		this.plugin = plugin;
	}

	private enum ValidEntities {
		BLAZE, BOAT, CAVE_SPIDER, CHICKEN, COW, CREEPER, ENDER_DRAGON, ENDERMAN, GHAST, GIANT, IRON_GOLEM, MAGMA_CUBE,
		MUSHROOM_COW, MINECART, OCELOT, PIG, PIG_ZOMBIE, PRIMED_TNT, SHEEP, SILVERFISH, SKELETON, SLIME, SNOWMAN,
		SQUID, VILLAGER, WOLF, ZOMBIE
	}

	public boolean isEntity(String theString) {

		for (ValidEntities entity : ValidEntities.values()) {
			if (entity.name().equals(theString.toUpperCase()))
				return true;
		}

		return false;
	}

	public List<NPC> getDenizens() {

		List<NPC> denizenList = new ArrayList<NPC>();
		
		for(NPC npc : CitizensAPI.getNPCRegistry()) {
			if(npc.hasTrait(DenizenTrait.class)) {
				denizenList.add(npc);
			}
		}
		return denizenList;

	}


}
