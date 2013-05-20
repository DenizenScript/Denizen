package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.arguments.Duration;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_5_R2.EntityHuman;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class HealthTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("max")
    private int maxhealth = 20;
    @Persist("current")
    private int currenthealth = 20;

    @Persist("animatedeath")
    private boolean animatedeath = Settings.HealthTraitAnimatedDeathEnabled();
    @Persist("animatedeathdelayinseconds")
    private String animationDelay = "3s";

    @Persist("respawnondeath")
    private boolean respawn = Settings.HealthTraitRespawnEnabled();
    @Persist("respawndelayinseconds")
    private String respawnDelay = Settings.HealthTraitRespawnDelay();;
    @Persist("respawnlocation")
    private String respawnLocation = "<npc.location>";

    // internal
	private Player player = null;
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
    
    public void die()
    {
        try {
    	// Set the player as the killer of the NPC, for listeners
    	if (player != null)
    		((CraftLivingEntity) npc.getBukkitEntity())
    			.getHandle().killer = (EntityHuman) ((CraftLivingEntity) player).getHandle();
        } catch (Exception e) {
            dB.echoError("Report this error to aufdemrand! Err: HealthTraitDie");
        }

    	setHealth();
        
    	EntityDeathEvent entityDeath = new EntityDeathEvent(npc.getBukkitEntity(), null);
    	NPCDeathEvent npcDeath = new NPCDeathEvent(npc, entityDeath);
    	
        DenizenAPI.getCurrentInstance().getServer()
			.getPluginManager().callEvent(npcDeath);
        DenizenAPI.getCurrentInstance().getServer()
			.getPluginManager().callEvent(entityDeath);
        
        npc.despawn(DespawnReason.DEATH);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDamageEvent event) {
    	// Don't use NPCDamageEvent because it doesn't work well
    	
        // Check if the event pertains to this NPC
        if (event.getEntity() != npc.getBukkitEntity() || dying) return;

        // Make sure this is a killing blow
        if (this.getHealth() - event.getDamage() > 0)
            return;

        dying = true;
        player = null;
        
    	String deathCause = event.getCause().toString().toLowerCase().replace('_', ' ');
    	
        // Check if the entity has been killed by another entity
        if (event instanceof EntityDamageByEntityEvent)
        {
        	Entity killerEntity = ((EntityDamageByEntityEvent) event).getDamager();
        	
        	// Check if the damager was a player and, if so, attach
        	// that player to the action's ScriptEntry
        	if (killerEntity instanceof Player)
        		player = (Player) killerEntity;
        	
        	// If the damager was a projectile, take its shooter into
        	// account as well
        	else if (killerEntity instanceof Projectile)
        	{
        		LivingEntity shooter = ((Projectile) killerEntity).getShooter();
        		
        		if (shooter instanceof Player)
        			player = (Player) shooter;
        		
        		DenizenAPI.getDenizenNPC(npc).action("death by " +
        	        	shooter.getType().toString(), player);
        	}
        	
        	DenizenAPI.getDenizenNPC(npc).action("death by entity", player);
        	DenizenAPI.getDenizenNPC(npc).action("death by " +
        	killerEntity.getType().toString(), player);
        	
        }
        // If not, check if the entity has been killed by a block
        else if (event instanceof EntityDamageByBlockEvent)
        {
        	DenizenAPI.getDenizenNPC(npc).action("death by block", player);
        	
        	// The line of code below should work, but a Bukkit bug makes the damager
        	// return null. Uncomment it once the bug is fixed.
        	
        	//DenizenAPI.getDenizenNPC(npc).action("death by " +
    		//		((EntityDamageByBlockEvent) event).getDamager().getType().name(), null);
        }
        
        DenizenAPI.getDenizenNPC(npc).action("death", player);
        DenizenAPI.getDenizenNPC(npc).action("death by " + deathCause, player);

        // One of the actions above may have removed the NPC, so check if the
        // NPC's entity still exists before proceeding
        if (npc.getBukkitEntity() == null)
        	return;

        loc = aH.getLocationFrom(
                TagManager.tag(null, DenizenAPI.getDenizenNPC(npc), respawnLocation, false));
        if (loc == null) loc = npc.getBukkitEntity().getLocation();
        
        if (animatedeath) {
            // Cancel navigation to keep the NPC from damaging players
            // while the death animation is being carried out.
            npc.getNavigator().cancelNavigation();
            // Reset health now to avoid the death from happening instantly
            setHealth();
            // Play animation
            npc.getBukkitEntity().playEffect(EntityEffect.DEATH);

            // Schedule the delayed task to carry out the death after the animation
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        public void run() { die(); }
                    }, 60);
        }

        // No animated death? Then just die now.
        else die();

        if (respawn) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        public void run() {
                            if (npc.isSpawned()) return;
                            else npc.spawn(loc);
                        }
                    } , (Duration.valueOf(respawnDelay).getTicks()));
        }

    }

}
