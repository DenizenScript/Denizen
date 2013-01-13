package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.traits.ConstantsTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ConstantTags implements Listener {

	public ConstantTags(Denizen denizen) {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	@EventHandler
	public void constantTags(ReplaceableTagEvent event) {
		if (!event.matches("CONS")) return;

		NPC npc = null;
		if (event.getType() != null
				&& event.getType().matches("\\d+"))
			npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(event.getType()));
		else if (event.getNPC() != null)
			npc = event.getNPC().getCitizen();
		if (npc == null) return;

		if (npc.hasTrait(ConstantsTrait.class)
				&& npc.getTrait(ConstantsTrait.class).getConstant(event.getValue()) != null)
			event.setReplaced(npc.getTrait(ConstantsTrait.class).getConstant(event.getValue()));
	}


}