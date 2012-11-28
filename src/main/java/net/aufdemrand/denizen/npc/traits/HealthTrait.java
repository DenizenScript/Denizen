package net.aufdemrand.denizen.npc.traits;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class HealthTrait extends Trait implements Listener {

	private int maxHealth = 20;
		
	public HealthTrait() {
		super("health");
	}
	
	@Override public void load(DataKey key) throws NPCLoadException {
		maxHealth = key.getInt("maxhealth", 20);
	}
	
	@Override public void save(DataKey key) {
		key.setInt("maxhealth", maxHealth);
	}
	
	@EventHandler
	public void onSpawn(NPCSpawnEvent event) {
	    setHealth();
	}
	
	public void setMaxHealth(int newMax) {
		this.maxHealth = newMax;
	}
	
	public int getMaxHealth() {
	    return maxHealth;
    }

	public void setHealth() {
	    ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(maxHealth);
	}
	
	public void setHealth(int health) {
	    ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(health);
	}	
	
	public int getHealth() {
	    return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().getHealth();
	}
	
}
