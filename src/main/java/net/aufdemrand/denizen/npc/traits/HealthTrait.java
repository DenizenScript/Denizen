package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class HealthTrait extends Trait implements Listener {

    // Saved to the C2 saves.yml
    @Persist("animatedeath")
    private boolean animatedeath = Settings.HealthTraitAnimatedDeathEnabled();

    @Persist("respawnondeath")
    private boolean respawn = Settings.HealthTraitRespawnEnabled();

    @Persist("respawndelayinseconds")
    private String respawnDelay = Settings.HealthTraitRespawnDelay();

    @Persist("respawnlocation")
    private String respawnLocation =  "<npc.flag[respawn_location] || <npc.location>>";

    // internal
    private dPlayer player = null;
    private boolean dying = false;
    private Location loc;
    private int entityId = -1;


    public Duration getRespawnDelay() {
        return Duration.valueOf(respawnDelay);
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
        return dLocation.valueOf(TagManager.tag(null, dNPC.mirrorCitizensNPC(npc), respawnLocation));
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


    public Integer void_watcher_task = null;

    /**
     * Listens for spawn of an NPC and updates its health with the max health
     * information for this trait.
     *
     */
    @Override public void onSpawn() {
        dying = false;
        setHealth();

        void_watcher_task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                if (!npc.isSpawned()) {
                    Bukkit.getScheduler().cancelTask(void_watcher_task);
                    return;
                }
                if (npc.getBukkitEntity().getLocation().getY() < -1000) {
                    npc.despawn(DespawnReason.DEATH);
                    if (respawn) {
                        if (npc.isSpawned()) npc.getBukkitEntity().teleport(getRespawnLocation());
                        else npc.spawn(getRespawnLocation());
                    }
                }
            }
        }, 200, 200);

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
    public double getHealth() {
        if (!npc.isSpawned()) return 0;
        else return npc.getBukkitEntity().getHealth();
    }

    /**
     * Sets the maximum health for this NPC. Default max is 20.
     *
     * @param newMax new maximum health
     *
     */
    public void setMaxhealth(int newMax) {
        npc.getBukkitEntity().setMaxHealth(newMax);
    }

    /**
     * Gets the maximum health for this NPC.
     *
     * @return maximum health
     */
    public double getMaxhealth() {
        return npc.getBukkitEntity().getMaxHealth();
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
        setHealth(npc.getBukkitEntity().getMaxHealth());
    }

    /**
     * Sets the NPCs health to a specific amount.
     *
     * @param health total health points
     */
    public void setHealth(double health) {
        if (npc.getBukkitEntity() != null)
            npc.getBukkitEntity().setHealth(health);
    }

    public void die() {
        npc.getBukkitEntity().damage(npc.getBukkitEntity().getHealth());
    }

    // Listen for deaths to clear drops
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().getEntityId() != entityId) return;

        event.getDrops().clear();
    }

    // <--[action]
    // @Actions
    // death
    // death by entity
    // death by <entity>
    // death by block
    // death by <cause>
    //
    // @Triggers when the NPC dies. (Requires Health Trait)
    //
    // @Context
    // <context.killer> returns the entity that killed the NPC (if any)
    // <context.shooter> returns the shooter of the killing projectile (if any)
    //
    // -->
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        // Don't use NPCDamageEvent because it doesn't work well

        // Check if the event pertains to this NPC
        if (event.getEntity() != npc.getBukkitEntity() || dying) return;

        // Make sure this is a killing blow
        if (this.getHealth() - event.getDamage() > 0)
            return;

        dying = true;
        player = null;

        // Save entityId for EntityDeath event
        entityId = npc.getBukkitEntity().getEntityId();

        String deathCause = event.getCause().toString().toLowerCase().replace('_', ' ');
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("damage", new Element(event.getDamage()));
        context.put("death_cause", new Element(deathCause));


        // Check if the entity has been killed by another entity
        if (event instanceof EntityDamageByEntityEvent)
        {
            Entity killerEntity = ((EntityDamageByEntityEvent) event).getDamager();
            context.put("killer", new dEntity(killerEntity));

            // Check if the damager was a player and, if so, attach
            // that player to the action's ScriptEntry
            if (killerEntity instanceof Player)
                player = dPlayer.mirrorBukkitPlayer((Player) killerEntity);

                // If the damager was a projectile, take its shooter into
                // account as well
            else if (killerEntity instanceof Projectile)
            {
                ProjectileSource shooter = ((Projectile) killerEntity).getShooter();
                if (shooter != null && shooter instanceof LivingEntity) {

                    context.put("shooter", new dEntity((LivingEntity) shooter));
                    if (shooter instanceof Player)
                        player = dPlayer.mirrorBukkitPlayer((Player) shooter);

                    DenizenAPI.getDenizenNPC(npc).action("death by " +
                            ((LivingEntity) shooter).getType().toString(), player, context);
                }
                // TODO: Handle other shooter source thingy types
            }

            DenizenAPI.getDenizenNPC(npc).action("death by entity", player, context);
            DenizenAPI.getDenizenNPC(npc).action("death by " +
                    killerEntity.getType().toString(), player, context);

        }
        // If not, check if the entity has been killed by a block
        else if (event instanceof EntityDamageByBlockEvent)
        {
            DenizenAPI.getDenizenNPC(npc).action("death by block", player, context);

            // TODO:
            // The line of code below should work, but a Bukkit bug makes the damager
            // return null. Uncomment it once the bug is fixed.

            // DenizenAPI.getDenizenNPC(npc).action("death by " +
            // ((EntityDamageByBlockEvent) event).getDamager().getType().name(), null);
        }

        DenizenAPI.getDenizenNPC(npc).action("death", player, context);
        DenizenAPI.getDenizenNPC(npc).action("death by " + deathCause, player, context);

        // One of the actions above may have removed the NPC, so check if the
        // NPC's entity still exists before proceeding
        if (npc.getBukkitEntity() == null)
            return;

        loc = dLocation.valueOf(TagManager.tag(null,
                DenizenAPI.getDenizenNPC(npc),
                respawnLocation, false));

        if (loc == null) loc = npc.getBukkitEntity().getLocation();

        if (animatedeath) {
            // Cancel navigation to keep the NPC from damaging players
            // while the death animation is being carried out.
            npc.getNavigator().cancelNavigation();
            // Reset health now to avoid the death from happening instantly
            //setHealth();
            // Play animation (TODO)
            // playDeathAnimation(npc.getBukkitEntity());

        }

        die();

        if (respawn) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                    new Runnable() {
                        public void run() {
                            if (CitizensAPI.getNPCRegistry().getById(npc.getId()) == null || npc.isSpawned()) return;
                            else npc.spawn(loc);
                        }
                    } , (Duration.valueOf(respawnDelay).getTicks()));
        }

    }

//  TODO: Figure something out here. Minecraft 'death effect' is too buggy to use anymore.
//
//    public void playDeathAnimation(LivingEntity entity) {
//        entity.playEffect(EntityEffect.DEATH);
//        dMaterial mat = new dMaterial(Material.WOOL, 14);
//
//        for (dPlayer player : Utilities.getClosestPlayers(entity.getLocation(), 10)) {
//            for (Block block : Utilities.getRandomSolidBlocks(entity.getLocation(), 3, 65))
//                new FakeBlock(player, new dLocation(block.getLocation()),
//                        mat, Duration.valueOf("10-20s"));
//        }
//
//        ParticleEffect.CRIT.play(entity.getEyeLocation(), .2f, .2f, .2f, 0, 3500);
//
//        for (Block block : Utilities.getRandomSolidBlocks(entity.getLocation(), 2, 5)) {
//            entity.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BONE)).setPickupDelay(Integer.MAX_VALUE);
//            entity.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.REDSTONE, 1, (short) 14)).setPickupDelay(Integer.MAX_VALUE);
//        }
//    }
}
