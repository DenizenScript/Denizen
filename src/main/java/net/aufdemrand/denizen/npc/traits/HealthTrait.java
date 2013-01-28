package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HealthTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("max")
    private int maxhealth = 20;
    @Persist("current")
    private int currenthealth = 20;

    @Persist("animatedeath")
    private boolean animatedeath = true;
    @Persist("animatedeathdelayinseconds")
    private String animationDelay = "3s";

    @Persist("respawnondeath")
    private boolean respawn = true;
    @Persist("respawndelayinseconds")
    private String respawnDelay = "10s";
    @Persist("respawnlocation")
    private String respawnLocation = "<npc.location>";

    // internal
    private boolean dying = false;
    private Location loc;

    public double getAnimationDelay() {
        return Duration.valueOf(animationDelay).getSeconds();
    }

    public double getRespawnDelay() {
        return (Duration.valueOf(respawnDelay).getSeconds());
    }

    public void setRespawnLocation(String string) {
        if (aH.matchesLocation("location:" + string))
            respawnLocation = string;
    }

    public void setRespawnDelay(int seconds) {
        respawnDelay = String.valueOf(seconds);
    }

    public void setRespawnDelay(String string) {
        if (aH.matchesDuration("duration:" + string))
            respawnDelay = string;
    }

    public String getRespawnLocationAsString() {
        return respawnLocation;
    }

    public Location getRespawnLocation() {
        return aH.getLocationFrom(respawnLocation);
    }

    public void setDeathAnimationDelay(int seconds) {
        animationDelay = String.valueOf(seconds);
    }

    public void setDeathAnimationDelay(String string) {
        if (aH.matchesDuration("duration:" + string))
            animationDelay = string;
    }

    public void setRespawnable(boolean respawnable) {
        respawn = respawnable;
    }

    public boolean isRespawnable() {
        return respawn;
    }

    public void animateOnDeath(boolean animate) {
        animatedeath = animate;
    }

    public boolean animatesOnDeath() {
        return animatedeath;
    }



    /**
     * Listens for spawn of an NPC and updates its health with saved
     * information from this Trait. If a respawn from death, sets health to maxHealth.
     * If any other type of respawn, sets health to the last known currentHealth.
     *
     */
    @Override public void onSpawn() {
        dying = false;
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
        if (!npc.isSpawned()) return currenthealth;
        else return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().getHealth();
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
        if (npc.getBukkitEntity() != null)
            ((CraftLivingEntity) npc.getBukkitEntity()).getHandle().setHealth(health);
        currenthealth = health;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDamageByEntityEvent event) {
        // Check if the event pertains to this NPC
        if (event.getEntity() != npc.getBukkitEntity() || dying) return;

        // Make sure this is a killing blow
        if (this.getHealth() - event.getDamage() > 0)
            return;

        dying = true;

        // It is... is killer a Player?
        if (event.getDamager() instanceof Player) {
            // Yep.. pass player to the action
            DenizenAPI.getDenizenNPC(npc).action("death", (Player) event.getDamager());
            DenizenAPI.getDenizenNPC(npc).action("death by player", (Player) event.getDamager());
        }   else {
            // Nope.. killed by entity
            DenizenAPI.getDenizenNPC(npc).action("death", null);
            DenizenAPI.getDenizenNPC(npc).action("death by monster", null);
            DenizenAPI.getDenizenNPC(npc).action("death by " + event.getDamager().getType().toString(), null);
        }

        loc = aH.getLocationFrom(DenizenAPI.getCurrentInstance().tagManager()
                .tag(null, DenizenAPI.getDenizenNPC(npc), respawnLocation, false));
        if (loc == null) loc = npc.getBukkitEntity().getLocation();

        if (animatedeath) {
            setHealth();
            npc.getBukkitEntity().playEffect(EntityEffect.DEATH);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        public void run() {
                            npc.despawn(DespawnReason.DEATH);
                            setHealth();
                        }
                    } , (long) ((Duration.valueOf(animationDelay).getSeconds() * 20)) );

        } else {
            npc.despawn(DespawnReason.DEATH);
            setHealth();
        }

        if (respawn) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        public void run() {
                            if (npc.isSpawned()) return;
                            npc.spawn(loc);
                        }
                    } , (long) ((Duration.valueOf(respawnDelay).getSeconds() * 20)) );
        }
    }

}
