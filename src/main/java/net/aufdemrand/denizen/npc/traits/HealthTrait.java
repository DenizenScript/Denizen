package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(NPCDamageByEntityEvent event) {
        // Check if the event pertains to this NPC
        if (event.getNPC() != npc) return;

        // Make sure this is a killing blow
        if (this.getHealth() - event.getDamage() > 0)
            return;

        // It does... is killer a Player?
        if (event.getDamager() instanceof Player) {
            // Yep.. pass player to the action
            DenizenAPI.getDenizenNPC(npc).action("death", (Player) event.getDamager());
            DenizenAPI.getDenizenNPC(npc).action("death by player", (Player) event.getDamager());
        }   else {
            // Nope.. killed by entity
            DenizenAPI.getDenizenNPC(npc).action("death", null);
            DenizenAPI.getDenizenNPC(npc).action("death by monster", null);
            DenizenAPI.getDenizenNPC(npc).action("death by " + event.getDamager().getType().name(), null);
        }
    }

}
