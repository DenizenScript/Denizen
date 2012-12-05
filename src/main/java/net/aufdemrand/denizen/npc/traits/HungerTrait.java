package net.aufdemrand.denizen.npc.traits;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class HungerTrait extends Trait implements Listener {
	
	public HungerTrait() {
		super("hunger");
	}
	
	@Override public void load(DataKey key) throws NPCLoadException {
	}
	
	@Override public void save(DataKey key) {
	}
	
	

}
