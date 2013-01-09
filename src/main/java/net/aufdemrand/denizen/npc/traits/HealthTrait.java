package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.event.Listener;

public class HealthTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("max")
    private int maxhealth = 20;
    @Persist("current")
    private int currenthealth = 20;
	
    /**
     * Listens for spawn of an NPC and updates its health with saved
     * information from this Trait. If a respawn from death, sets health to maxHealth.
     * If any other type of respawn, sets health to the last known currentHealth.
     *
     */
	@Override public void onSpawn() {
	    if (currenthealth > 0) setHealth(currenthealth);
	    else setHealth();
	}

    /**
     * Listens for a despawn to note currentHealth as the time. Will be used
     * to reset health on a respawn.
     *
     */
	@Override public void onDespawn() {
	    if (getHealth() > 0) currenthealth = getHealth();
        else currenthealth = -1;
	}

    public HealthTrait() {
        super("health");
    }

    /**
     * Gets the current health of this NPC.
     *
     * @return current health points
     *
     */
    public int getHealth() {
        return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().getHealth();
    }

    /**
     * Sets the maximum health for this NPC. Default max is 20.
     *
     * @param newMax new maximum health
     *
     */
	public void setMaxhealth(int newMax) {
		this.maxhealth = newMax;
	}

    /**
     * Gets the maximum health for this NPC.
     *
     * @return maximum health
     */
    public int getMaxhealth() {
        return maxhealth;
    }

    /**
     * Heals the NPC.
     *
     * @param health number of health points to heal
     */
	public void heal(int health) {
	    setHealth(getHealth() + health);
	}

    /**
     * Sets the NPCs health to maximum.
     *
     */
    public void setHealth() {
        setHealth(maxhealth);
    }

    /**
     * Sets the NPCs health to a specific amount.
     *
     * @param health total health points
     */
	public void setHealth(int health) {
	    ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(health);
	    currenthealth = getHealth();
	}

}
