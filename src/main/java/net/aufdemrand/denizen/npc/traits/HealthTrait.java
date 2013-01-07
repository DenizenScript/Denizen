package net.aufdemrand.denizen.npc.traits;


import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.event.Listener;

public class HealthTrait extends Trait implements Listener {

    @Persist("") private int maxhealth = 20;
    @Persist("") private int currenthealth = 20;
	
	public HealthTrait() {
		super("health");
	}
	
	@Override public void load(DataKey key) throws NPCLoadException {
		maxhealth = key.getInt("maxhealth", 20);
		currenthealth = key.getInt("currenthealth", maxhealth);
	}
	
	@Override public void save(DataKey key) {
		key.setInt("maxhealth", maxhealth);
		key.setInt("currenthealth", currenthealth);
	}
	
	@Override public void onSpawn() {
	    if (currenthealth > 0) setHealth(currenthealth);
	    else setHealth();
	}
	
	@Override public void onDespawn() {
	    if (getHealth() > 0) currenthealth = getHealth();
	}
	
	public void setMaxhealth(int newMax) {
		this.maxhealth = newMax;
	}
	
	public void heal(int health) {
	    setHealth(getHealth() + health);
	}
	
	public int getMaxhealth() {
	    return maxhealth;
    }

	public void setHealth() {
	    setHealth(maxhealth);
	}
	
	public void setHealth(int health) {
	    ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(health);
	    currenthealth = -1;
	}	
	
	public int getHealth() {
	    return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().getHealth();
	}
	
}
