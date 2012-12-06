package net.aufdemrand.denizen.npc.traits;


import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class HealthTrait extends Trait implements Listener {

	private int maxHealth = 20;
	private int rememberedHealth = -1;
	
	public HealthTrait() {
		super("health");
	}
	
	@Override public void load(DataKey key) throws NPCLoadException {
		maxHealth = key.getInt("maxhealth", 20);
		rememberedHealth = key.getInt("currenthealth", maxHealth);
	}
	
	@Override public void save(DataKey key) {
		key.setInt("maxhealth", maxHealth);
		key.setInt("currenthealth", rememberedHealth);
	}
	
	@Override public void onSpawn() {
	    if (rememberedHealth > 0) setHealth(rememberedHealth);
	    else setHealth();
	}
	
	@Override public void onDespawn() {
	    if (getHealth() > 0) rememberedHealth = getHealth();
	}
	
	public void setMaxHealth(int newMax) {
		this.maxHealth = newMax;
	}
	
	public int getMaxHealth() {
	    return maxHealth;
    }

	public void setHealth() {
	    setHealth(maxHealth);
	}
	
	public void setHealth(int health) {
	    ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(health);
	    rememberedHealth = -1;
	}	
	
	public int getHealth() {
	    return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().getHealth();
	}
	
}
